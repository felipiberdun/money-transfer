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

        return accountService.findById(createDepositCommand.destination)
                .flatMap {
                    val deposit = Deposit(
                            id = UUID.randomUUID(),
                            destination = it,
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

        val originAccount = accountService.findById(createTransferCommand.origin)
        val destinationAccount = accountService.findById(createTransferCommand.destination)

        return Single.zip(originAccount,
                destinationAccount,
                getCurrentBalance(createTransferCommand.origin),
                Function3<Account, Account, Float, Single<Transaction>> { from, to, balance ->
                    if (createTransferCommand.amount > balance) {
                        Single.error(InsufficientAmountException(createTransferCommand.origin, createTransferCommand.amount))
                    } else {
                        val transfer = Transfer(
                                id = UUID.randomUUID(),
                                origin = from,
                                destination = to,
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

        return accountService.findById(createWithdrawCommand.origin)
                .zipWith(getCurrentBalance(createWithdrawCommand.origin),
                        BiFunction<Account, Float, Account> { account, balance ->
                            if (createWithdrawCommand.amount > balance) {
                                throw InsufficientAmountException(createWithdrawCommand.origin, createWithdrawCommand.amount)
                            }

                            account
                        })
                .flatMap {
                    val withdraw = Withdraw(
                            id = UUID.randomUUID(),
                            origin = it,
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
                            is Transfer -> it.amount * (if (it.origin.id == accountId) -1 else 1)
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