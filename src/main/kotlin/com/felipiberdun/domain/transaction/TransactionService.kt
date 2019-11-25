package com.felipiberdun.domain.transaction

import com.felipiberdun.domain.accounts.AccountService
import io.reactivex.Single
import sun.reflect.generics.reflectiveObjects.NotImplementedException
import java.time.LocalDateTime
import java.util.*
import javax.inject.Singleton

@Singleton
class TransactionService(private val transactionRepository: TransactionRepository,
                         private val accountService: AccountService) {

    fun processTransaction(createTransactionCommand: CreateTransactionCommand): Single<Transaction> {
        return when (createTransactionCommand) {
            is CreateDepositCommand -> processDeposit(createTransactionCommand)
            is CreateTransferCommand -> processTransfer(createTransactionCommand)
            is CreateWithdrawCommand -> processWithdraw(createTransactionCommand)
        }
    }

    private fun processDeposit(createDepositCommand: CreateDepositCommand): Single<Transaction> {
        if (createDepositCommand.amount <= 0) {
            return Single.error(InvalidTransactionAmmountException)
        }

        return accountService.findById(createDepositCommand.to)
                .map {
                    val deposit = Deposit(UUID.randomUUID(), it, createDepositCommand.amount, LocalDateTime.now())
                    transactionRepository.createTransaction(deposit)
                }
                .switchIfEmpty(Single.error(AccountNotFoundException))
    }

    private fun processTransfer(createTransferCommand: CreateTransferCommand): Single<Transaction> {
        //TODO create logic for processing trasnfers
        return Single.error(NotImplementedException())
    }

    private fun processWithdraw(createWithdrawCommand: CreateWithdrawCommand): Single<Transaction> {
        //TODO create logic for processing withdraws
        return Single.error(NotImplementedException())
    }

}