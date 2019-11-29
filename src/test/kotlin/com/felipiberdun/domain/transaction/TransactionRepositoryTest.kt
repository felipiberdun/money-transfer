package com.felipiberdun.domain.transaction

import com.felipiberdun.application.transaction.TransactionInMemoryRepository
import com.felipiberdun.domain.accounts.Account
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

object TransactionRepositoryTest : Spek({

    describe("TransactionInMemoryRepository") {
        val account = Account(id = UUID.randomUUID(), owner = "Felipi", creationData = LocalDateTime.now())

        val deposit = Deposit(id = UUID.randomUUID(), to = account, amount = 10f, date = LocalDateTime.now())
        val withdraw = Withdraw(id = UUID.randomUUID(), from = account, amount = 7f, date = LocalDateTime.now())

        describe("When account with id $account.id already exists") {
            var repository = TransactionInMemoryRepository(ConcurrentHashMap())

            beforeEachTest {
                repository = TransactionInMemoryRepository(ConcurrentHashMap(mapOf(
                        account.id to CopyOnWriteArrayList(listOf(deposit, withdraw))
                )))
            }

            describe("Searching by account") {
                it("with id ${account.id} returns all transactions") {
                    repository.findByAccountId(account.id)
                            .test()
                            .assertNoErrors()
                            .assertValue(listOf(deposit, withdraw))
                            .assertComplete()
                            .dispose()
                }

                it("with other id returns empty list") {
                    repository.findByAccountId(UUID.randomUUID())
                            .test()
                            .assertNoErrors()
                            .assertValue(emptyList())
                            .assertComplete()
                            .dispose()
                }
            }

            describe("Searching by account and transaction id") {
                it("with id ${account.id} and transaction id ${withdraw.id} returns withdraw") {
                    repository.findByAccountAndId(account.id, withdraw.id)
                            .test()
                            .assertNoErrors()
                            .assertValue(withdraw)
                            .assertComplete()
                            .dispose()
                }

                it("with account ${account.id} and not existent transaction returns nothing") {
                    repository.findByAccountAndId(account.id, UUID.randomUUID())
                            .test()
                            .assertNoErrors()
                            .assertNoValues()
                            .assertComplete()
                            .dispose()
                }

                it("with not existent account ${account.id} returns nothing") {
                    repository.findByAccountAndId(UUID.randomUUID(), withdraw.id)
                            .test()
                            .assertNoErrors()
                            .assertNoValues()
                            .assertComplete()
                            .dispose()
                }
            }

            describe("Creating new transaction") {
                describe("Deposit") {
                    it("for account ${account.id} creates it") {
                        val newDeposit = Deposit(id = UUID.randomUUID(), to = account, amount = 10f, date = LocalDateTime.now())

                        repository.createTransaction(newDeposit)
                                .test()
                                .assertValue(newDeposit)
                                .assertNoErrors()
                                .assertComplete()
                                .dispose()

                        repository.findByAccountId(account.id)
                                .test()
                                .assertValue(listOf(deposit, withdraw, newDeposit))
                                .assertNoErrors()
                                .assertComplete()
                                .dispose()
                    }
                }

                describe("Transfer") {
                    it("for account ${account.id} creates it ") {
                        val accountTo = Account(id = UUID.randomUUID(), owner = "Destination", creationData = LocalDateTime.now())
                        val newTransfer = Transfer(id = UUID.randomUUID(), from = account, to = accountTo, amount = 3f, date = LocalDateTime.now())

                        repository.createTransaction(newTransfer)
                                .test()
                                .assertValue(newTransfer)
                                .assertNoErrors()
                                .assertComplete()
                                .dispose()

                        repository.findByAccountId(account.id)
                                .test()
                                .assertValue(listOf(deposit, withdraw, newTransfer))
                                .assertNoErrors()
                                .assertComplete()
                                .dispose()
                    }
                }

                describe("Withdraw") {
                    it("for account ${account.id} creates it ") {
                        val newWithdraw = Withdraw(id = UUID.randomUUID(), from = account, amount = 3f, date = LocalDateTime.now())

                        repository.createTransaction(newWithdraw)
                                .test()
                                .assertValue(newWithdraw)
                                .assertNoErrors()
                                .assertComplete()
                                .dispose()

                        repository.findByAccountId(account.id)
                                .test()
                                .assertValue(listOf(deposit, withdraw, newWithdraw))
                                .assertNoErrors()
                                .assertComplete()
                                .dispose()
                    }
                }

            }
        }

    }

})