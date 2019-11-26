package com.felipiberdun.domain.transaction

import com.felipiberdun.domain.accounts.Account
import com.felipiberdun.domain.accounts.AccountService
import io.reactivex.Single
import io.reactivex.functions.BiFunction
import java.time.LocalDateTime
import java.util.*
import javax.inject.Singleton

@Singleton
class TransactionService(private val transactionRepository: TransactionRepository,
                         private val accountService: AccountService) {

    fun processTransaction(createTransactionCommand: CreateTransactionCommand): Single<Transaction> {
        if (createTransactionCommand.amount <= 0) {
            return Single.error(InvalidTransactionAmmountException)
        }

        return when (createTransactionCommand) {
            is CreateDepositCommand -> processDeposit(createTransactionCommand)
            is CreateTransferCommand -> processTransfer(createTransactionCommand)
            is CreateWithdrawCommand -> processWithdraw(createTransactionCommand)
        }
    }

    private fun processDeposit(createDepositCommand: CreateDepositCommand): Single<Transaction> {
        return accountService.findById(createDepositCommand.to)
                .switchIfEmpty(Single.error(AccountNotFoundException(createDepositCommand.to)))
                .flatMap {
                    val deposit = Deposit(
                            id = UUID.randomUUID(),
                            to = it,
                            amount = createDepositCommand.amount,
                            date = LocalDateTime.now())
                    transactionRepository.createTransaction(deposit)
                }
    }

    private fun processTransfer(createTransferCommand: CreateTransferCommand): Single<Transaction> {
        val accountFrom = accountService
                .findById(createTransferCommand.from)
                .switchIfEmpty(Single.error(AccountNotFoundException(createTransferCommand.from)))

        val accountTo = accountService
                .findById(createTransferCommand.to)
                .switchIfEmpty(Single.error(AccountNotFoundException(createTransferCommand.to)))

        return Single.zip(accountFrom, accountTo,
                BiFunction<Account, Account, Single<Transaction>> { from, to ->
                    if (createTransferCommand.amount >= 0) {
                        throw InsufficientAmountException(createTransferCommand.from, createTransferCommand.amount)
                    }

                    val transfer = Transfer(
                            id = UUID.randomUUID(),
                            from = from,
                            to = to,
                            amount = createTransferCommand.amount,
                            date = LocalDateTime.now())

                    transactionRepository.createTransaction(transfer)
                })
                .flatMap { it }
    }

    private fun processWithdraw(createWithdrawCommand: CreateWithdrawCommand): Single<Transaction> {
        return accountService.findById(createWithdrawCommand.from)
                .switchIfEmpty(Single.error(AccountNotFoundException(createWithdrawCommand.from)))
                .flatMap {
                    if (createWithdrawCommand.amount >= 0) {
                        throw InsufficientAmountException(createWithdrawCommand.from, createWithdrawCommand.amount)
                    }

                    val withdraw = Withdraw(
                            id = UUID.randomUUID(),
                            from = it,
                            amount = createWithdrawCommand.amount,
                            date = LocalDateTime.now())

                    transactionRepository.createTransaction(withdraw)
                }
    }

}