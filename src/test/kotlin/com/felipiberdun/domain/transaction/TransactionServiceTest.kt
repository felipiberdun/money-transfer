package com.felipiberdun.domain.transaction

import com.felipiberdun.domain.accounts.Account
import com.felipiberdun.domain.accounts.AccountService
import io.mockk.clearMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import io.reactivex.Maybe
import io.reactivex.Single
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDateTime
import java.util.*

object TransactionServiceTest : Spek({

    describe("TransactionService") {
        val repository = mockk<TransactionRepository>()
        val accountService = mockk<AccountService>()
        val transactionService = TransactionService(repository, accountService)

        beforeEachTest { clearMocks(repository) }

        describe("Searching") {
            val accountId = UUID.randomUUID()
            val account = Account(id = accountId, owner = "Felipi", creationData = LocalDateTime.now())
            val deposit = Deposit(id = UUID.randomUUID(), destination = account, amount = 10f, date = LocalDateTime.now())

            describe("All transactions") {
                it("if account exists returns all transactions") {
                    every { accountService.findById(accountId) } returns Single.just(account)
                    every { repository.findByAccountId(accountId) } returns Single.just(listOf(deposit))

                    transactionService.findByAccountId(accountId)
                            .test()
                            .assertNoErrors()
                            .assertValue(listOf(deposit))
                            .assertComplete()
                            .dispose()

                    verify { accountService.findById(accountId) }
                    verify { repository.findByAccountId(accountId) }
                }

                it("if account doesn't exist returns AccountNotFoundException error") {
                    every { accountService.findById(any()) } returns Single.error(AccountNotFoundException(accountId))

                    transactionService.findByAccountId(accountId)
                            .test()
                            .assertNoValues()
                            .assertError(AccountNotFoundException::class.java)
                            .assertError { e -> (e as AccountNotFoundException).accountId == accountId }
                            .dispose()

                    verify { accountService.findById(accountId) }
                    verify(exactly = 0) { repository.findByAccountId(any()) }
                }
            }

            describe("Single transaction") {
                it("if account and transaction exists returns transaction") {
                    every { accountService.findById(accountId) } returns Single.just(account)
                    every { repository.findByAccountAndId(accountId, deposit.id) } returns Maybe.just(deposit)

                    transactionService.findByAccountAndId(accountId, deposit.id)
                            .test()
                            .assertNoErrors()
                            .assertValue(deposit)
                            .assertComplete()
                            .dispose()

                    verify { accountService.findById(accountId) }
                    verify { repository.findByAccountAndId(accountId, deposit.id) }
                }

                it("if account exist but transaction doesn't returns nothing") {
                    every { accountService.findById(accountId) } returns Single.just(account)
                    every { repository.findByAccountAndId(accountId, deposit.id) } returns Maybe.empty()

                    transactionService.findByAccountAndId(accountId, deposit.id)
                            .test()
                            .assertNoErrors()
                            .assertNoValues()
                            .assertComplete()
                            .dispose()

                    verify { accountService.findById(accountId) }
                    verify { repository.findByAccountAndId(accountId, deposit.id) }
                }

                it("if account doesn't exist returns AccountNotFoundException error") {
                    every { accountService.findById(any()) } returns Single.error(AccountNotFoundException(accountId))

                    transactionService.findByAccountAndId(accountId, UUID.randomUUID())
                            .test()
                            .assertNoValues()
                            .assertError(AccountNotFoundException::class.java)
                            .assertError { e -> (e as AccountNotFoundException).accountId == accountId }
                            .dispose()

                    verify { accountService.findById(accountId) }
                    verify(exactly = 0) { repository.findByAccountAndId(any(), any()) }
                }
            }
        }

        describe("Creating") {
            describe("Deposit") {
                val accountId = UUID.randomUUID()
                val account = Account(id = accountId, owner = "Felipi", creationData = LocalDateTime.now())
                val deposit = Deposit(id = UUID.randomUUID(), destination = account, amount = 10f, date = LocalDateTime.now())

                it("if account doesn't exist returns AccountNotFoundException error") {
                    every { accountService.findById(any()) } returns Single.error(AccountNotFoundException(accountId))

                    val createDepositCommand = CreateDepositCommand(destination = accountId, amount = deposit.amount)
                    transactionService.createDeposit(createDepositCommand)
                            .test()
                            .assertNoValues()
                            .assertError(AccountNotFoundException::class.java)
                            .assertError { e -> (e as AccountNotFoundException).accountId == accountId }
                            .dispose()

                    verify { accountService.findById(accountId) }
                    verify(exactly = 0) { repository.createTransaction(any()) }
                }

                it("if account exist creates successfully") {
                    every { accountService.findById(accountId) } returns Single.just(account)
                    every { repository.createTransaction(any<Deposit>()) } returns Single.just(deposit)

                    val createDepositCommand = CreateDepositCommand(destination = accountId, amount = deposit.amount)
                    transactionService.createDeposit(createDepositCommand)
                            .test()
                            .assertNoErrors()
                            .assertValue { dep -> dep.destination.id == accountId && dep.amount == deposit.amount }
                            .assertComplete()
                            .dispose()

                    verify { accountService.findById(accountId) }
                    verify { repository.createTransaction(ofType(Deposit::class)) }
                }

                listOf(-23f, -1f, 0f)
                        .forEach { value ->
                            it("if amount is $value returns InvalidTransactionAmmountException error") {
                                val createDepositCommand = CreateDepositCommand(destination = accountId, amount = value)

                                transactionService.createDeposit(createDepositCommand)
                                        .test()
                                        .assertNoValues()
                                        .assertError(InvalidTransactionAmmountException::class.java)
                                        .dispose()
                            }
                        }
            }

            describe("Transfer") {
                val originAccount = Account(id = UUID.randomUUID(), owner = "Origin account", creationData = LocalDateTime.now())
                val destinationAccount = Account(id = UUID.randomUUID(), owner = "Destination Account", creationData = LocalDateTime.now())

                it("if origin account doesn't exist returns AccountNotFoundException error") {
                    every { accountService.findById(destinationAccount.id) } returns Single.just(destinationAccount)
                    every { accountService.findById(not(destinationAccount.id)) } returns Single.error(AccountNotFoundException(originAccount.id))
                    every { repository.findByAccountId(originAccount.id) } returns Single.just(emptyList())

                    val createTransferCommand = CreateTransferCommand(origin = originAccount.id, destination = destinationAccount.id, amount = 10f)
                    transactionService.createTransfer(createTransferCommand)
                            .test()
                            .assertNoValues()
                            .assertError(AccountNotFoundException::class.java)
                            .assertError { e -> (e as AccountNotFoundException).accountId == originAccount.id }
                            .dispose()

                    verify { accountService.findById(originAccount.id) }
                    verify { accountService.findById(destinationAccount.id) }
                    verify(exactly = 0) { repository.createTransaction(any()) }
                }

                it("if destination account doesn't exist returns AccountNotFoundException error") {
                    every { accountService.findById(originAccount.id) } returns Single.just(originAccount)
                    every { accountService.findById(not(originAccount.id)) } returns Single.error(AccountNotFoundException(destinationAccount.id))
                    every { repository.findByAccountId(originAccount.id) } returns Single.just(emptyList())

                    val createTransferCommand = CreateTransferCommand(origin = originAccount.id, destination = destinationAccount.id, amount = 10f)
                    transactionService.createTransfer(createTransferCommand)
                            .test()
                            .assertNoValues()
                            .assertError(AccountNotFoundException::class.java)
                            .assertError { e -> (e as AccountNotFoundException).accountId == destinationAccount.id }
                            .dispose()

                    verify { accountService.findById(originAccount.id) }
                    verify { accountService.findById(destinationAccount.id) }
                    verify(exactly = 0) { repository.createTransaction(any()) }
                }

                val deposit9dols = Deposit(id = UUID.randomUUID(), destination = originAccount, amount = 9f, date = LocalDateTime.now())
                val withdraw3dols = Withdraw(id = UUID.randomUUID(), origin = originAccount, amount = 3f, date = LocalDateTime.now())

                mapOf(
                        listOf(deposit9dols) to 10f,
                        listOf(deposit9dols, withdraw3dols) to 6.2f,
                        listOf(deposit9dols, withdraw3dols, withdraw3dols, withdraw3dols) to 1f,
                        emptyList<Transaction>() to 0.02f
                )
                        .forEach { (transactions, transferAmount) ->
                            it("if origin doesn't have $transferAmount returns InsufficientAmountException error") {
                                every { accountService.findById(originAccount.id) } returns Single.just(originAccount)
                                every { accountService.findById(destinationAccount.id) } returns Single.just(destinationAccount)
                                every { repository.findByAccountId(originAccount.id) } returns Single.just(transactions)

                                val createTransferCommand = CreateTransferCommand(origin = originAccount.id, destination = destinationAccount.id, amount = transferAmount)
                                transactionService.createTransfer(createTransferCommand)
                                        .test()
                                        .assertNoValues()
                                        .assertError(InsufficientAmountException::class.java)
                                        .assertError { e -> (e as InsufficientAmountException).accountId == originAccount.id && e.amount == transferAmount }
                                        .dispose()

                                verify { accountService.findById(originAccount.id) }
                                verify { accountService.findById(destinationAccount.id) }
                                verify { repository.findByAccountId(originAccount.id) }
                                verify(exactly = 0) { repository.createTransaction(any()) }
                            }
                        }

                listOf(-23f, -1f, 0f)
                        .forEach { value ->
                            it("if amount is $value returns InvalidTransactionAmmountException error") {
                                val createTransferCommand = CreateTransferCommand(origin = originAccount.id, destination = destinationAccount.id, amount = value)

                                transactionService.createTransfer(createTransferCommand)
                                        .test()
                                        .assertNoValues()
                                        .assertError(InvalidTransactionAmmountException::class.java)
                                        .dispose()
                            }
                        }

                it("if origin has enough balance creates successfully") {
                    val transfer = Transfer(id = UUID.randomUUID(), origin = originAccount, destination = destinationAccount, amount = 8f, date = LocalDateTime.now())
                    every { repository.createTransaction(any()) } returns Single.just(transfer)
                    every { repository.findByAccountId(originAccount.id) } returns Single.just(listOf(deposit9dols))
                    every { accountService.findById(originAccount.id) } returns Single.just(originAccount)
                    every { accountService.findById(destinationAccount.id) } returns Single.just(destinationAccount)

                    val createTransferCommand = CreateTransferCommand(origin = originAccount.id, destination = destinationAccount.id, amount = transfer.amount)
                    transactionService.createTransfer(createTransferCommand)
                            .test()
                            .assertNoErrors()
                            .assertValue { t ->
                                (t as Transfer).origin == originAccount
                                        && t.destination == destinationAccount
                                        && t.amount == transfer.amount
                            }
                            .assertComplete()
                            .dispose()

                    verify { accountService.findById(originAccount.id) }
                    verify { accountService.findById(destinationAccount.id) }
                    verify { repository.findByAccountId(originAccount.id) }
                    verify { repository.createTransaction(ofType(Transfer::class)) }
                }
            }

            describe("Withdraw") {
                val originAccount = Account(id = UUID.randomUUID(), owner = "Origin account", creationData = LocalDateTime.now())

                it("if origin account doesn't exist returns AccountNotFoundException error") {
                    every { accountService.findById(originAccount.id) } returns Single.error(AccountNotFoundException(originAccount.id))
                    every { repository.findByAccountId(originAccount.id) } returns Single.just(emptyList())

                    val createWithdrawCommand = CreateWithdrawCommand(origin = originAccount.id, amount = 10f)
                    transactionService.createWithdraw(createWithdrawCommand)
                            .test()
                            .assertNoValues()
                            .assertError(AccountNotFoundException::class.java)
                            .assertError { e -> (e as AccountNotFoundException).accountId == originAccount.id }
                            .dispose()

                    verify { accountService.findById(originAccount.id) }
                    verify(exactly = 0) { repository.createTransaction(any()) }
                }

                val deposit9dols = Deposit(id = UUID.randomUUID(), destination = originAccount, amount = 9f, date = LocalDateTime.now())
                val withdraw3dols = Withdraw(id = UUID.randomUUID(), origin = originAccount, amount = 3f, date = LocalDateTime.now())

                mapOf(
                        listOf(deposit9dols) to 10f,
                        listOf(deposit9dols, withdraw3dols) to 6.2f,
                        listOf(deposit9dols, withdraw3dols, withdraw3dols, withdraw3dols) to 1f,
                        emptyList<Transaction>() to 0.02f
                )
                        .forEach { (transactions, transferAmount) ->
                            it("if origin doesn't have $transferAmount returns InsufficientAmountException error") {
                                every { accountService.findById(originAccount.id) } returns Single.just(originAccount)
                                every { repository.findByAccountId(originAccount.id) } returns Single.just(transactions)

                                val createWithdrawCommand = CreateWithdrawCommand(origin = originAccount.id, amount = transferAmount)
                                transactionService.createWithdraw(createWithdrawCommand)
                                        .test()
                                        .assertNoValues()
                                        .assertError(InsufficientAmountException::class.java)
                                        .assertError { e -> (e as InsufficientAmountException).accountId == originAccount.id && e.amount == transferAmount }
                                        .dispose()

                                verify { accountService.findById(originAccount.id) }
                                verify { repository.findByAccountId(originAccount.id) }
                                verify(exactly = 0) { repository.createTransaction(any()) }
                            }
                        }

                listOf(-23f, -1f, 0f)
                        .forEach { value ->
                            it("if amount is $value returns InvalidTransactionAmmountException error") {
                                val createWithdrawCommand = CreateWithdrawCommand(origin = originAccount.id, amount = value)

                                transactionService.createWithdraw(createWithdrawCommand)
                                        .test()
                                        .assertNoValues()
                                        .assertError(InvalidTransactionAmmountException::class.java)
                                        .dispose()
                            }
                        }

                it("if origin has enough balance creates successfully") {
                    val withdraw = Withdraw(id = UUID.randomUUID(), origin = originAccount, amount = 8f, date = LocalDateTime.now())
                    every { repository.createTransaction(any()) } returns Single.just(withdraw)
                    every { repository.findByAccountId(originAccount.id) } returns Single.just(listOf(deposit9dols))
                    every { accountService.findById(originAccount.id) } returns Single.just(originAccount)

                    val createWithdrawCommand = CreateWithdrawCommand(origin = originAccount.id, amount = withdraw.amount)
                    transactionService.createWithdraw(createWithdrawCommand)
                            .test()
                            .assertNoErrors()
                            .assertValue { t ->
                                (t as Withdraw).origin == originAccount
                                        && t.amount == withdraw.amount
                            }
                            .assertComplete()
                            .dispose()

                    verify { accountService.findById(originAccount.id) }
                    verify { repository.findByAccountId(originAccount.id) }
                    verify { repository.createTransaction(ofType(Withdraw::class)) }
                }
            }

        }
    }

})