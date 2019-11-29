package com.felipiberdun.domain.transaction

import com.felipiberdun.domain.accounts.Account
import com.felipiberdun.domain.accounts.AccountService
import io.reactivex.Maybe
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import io.reactivex.functions.Function3
import java.time.LocalDateTime
import java.util.*
import javax.inject.Singleton

@Singleton
class TransactionService(private val transactionRepository: TransactionRepository,
                         private val accountService: AccountService) {

    fun createDeposit(createDepositCommand: CreateDepositCommand): Single<Deposit> {
        if (createDepositCommand.amount <= 0) {
            return Single.error(InvalidTransactionAmmountException)
        }

        return accountService.findById(createDepositCommand.to)
                .flatMap {
                    val deposit = Deposit(
                            id = UUID.randomUUID(),
                            to = it,
                            amount = createDepositCommand.amount,
                            date = LocalDateTime.now())
                    transactionRepository.createTransaction(deposit)
                            .cast(Deposit::class.java)
                }
    }

    fun createTransfer(createTransferCommand: CreateTransferCommand): Single<Transaction> {
        if (createTransferCommand.amount <= 0) {
            return Single.error(InvalidTransactionAmmountException)
        }

        val accountFrom = accountService.findById(createTransferCommand.from)
        val accountTo = accountService.findById(createTransferCommand.to)

        return Single.zip(accountFrom,
                accountTo,
                getCurrentBalance(createTransferCommand.from),
                Function3<Account, Account, Float, Single<Transaction>> { from, to, balance ->
                    if (createTransferCommand.amount > balance) {
                        Single.error(InsufficientAmountException(createTransferCommand.from, createTransferCommand.amount))
                    } else {
                        val transfer = Transfer(
                                id = UUID.randomUUID(),
                                from = from,
                                to = to,
                                amount = createTransferCommand.amount,
                                date = LocalDateTime.now())

                        transactionRepository.createTransaction(transfer)
                    }
                })
                .flatMap { it }
    }

    fun createWithdraw(createWithdrawCommand: CreateWithdrawCommand): Single<Transaction> {
        if (createWithdrawCommand.amount <= 0) {
            return Single.error(InvalidTransactionAmmountException)
        }

        return accountService.findById(createWithdrawCommand.from)
                .zipWith(getCurrentBalance(createWithdrawCommand.from),
                        BiFunction<Account, Float, Account> { account, balance ->
                            if (createWithdrawCommand.amount > balance) {
                                throw InsufficientAmountException(createWithdrawCommand.from, createWithdrawCommand.amount)
                            }

                            account
                        })
                .flatMap {
                    val withdraw = Withdraw(
                            id = UUID.randomUUID(),
                            from = it,
                            amount = createWithdrawCommand.amount,
                            date = LocalDateTime.now())

                    transactionRepository.createTransaction(withdraw)
                }
    }

    fun getCurrentBalance(accountId: UUID): Single<Float> {
        return transactionRepository.findByAccountId(accountId)
                .map { list ->
                    list.map {
                        when (it) {
                            is Deposit -> it.amount
                            is Transfer -> it.amount * (if (it.from.id == accountId) -1 else 1)
                            is Withdraw -> it.amount * -1
                        }
                    }
                            .fold(0f) { a, b -> a + b }
                }
    }

    fun findByAccountId(accountId: UUID): Single<List<Transaction>> {
        return accountService.findById(accountId)
                .flatMap { transactionRepository.findByAccountId(it.id) }
    }

    fun findByAccountAndId(accountId: UUID, transactionId: UUID): Maybe<Transaction> {
        return accountService.findById(accountId)
                .flatMapMaybe { transactionRepository.findByAccountAndId(it.id, transactionId) }
    }

}