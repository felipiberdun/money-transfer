package com.felipiberdun.domain.account

import com.felipiberdun.application.account.AccountInMemoryRepository
import com.felipiberdun.domain.accounts.Account
import com.felipiberdun.domain.accounts.AccountAlreadyExistsException
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDateTime
import java.util.*
import java.util.concurrent.ConcurrentHashMap

object AccountRepositoryTest : Spek({

    describe("AccountInMemoryRepository") {
        val accountId = UUID.randomUUID()
        val account = Account(id = accountId, owner = "Felipi", creationData = LocalDateTime.now())

        describe("When account with id $accountId already exists") {
            val repository = AccountInMemoryRepository(ConcurrentHashMap(mapOf(accountId to account)))

            describe("Searching by id") {
                it("with id $accountId returns existing account") {
                    repository.findById(accountId)
                            .test()
                            .assertNoErrors()
                            .assertValue(account)
                            .assertComplete()
                            .dispose()
                }

                it("with other id returns nothing") {
                    repository.findById(UUID.randomUUID())
                            .test()
                            .assertNoErrors()
                            .assertNoValues()
                            .assertComplete()
                            .dispose()
                }
            }

            describe("Creating new account") {
                it("Creating account with id $accountId returns AccountAlreadyExists error") {
                    repository.createAccount(account)
                            .test()
                            .assertNoValues()
                            .assertError(AccountAlreadyExistsException::class.java)
                            .dispose()
                }

                it("Creating account with other id executes successfully") {
                    val newAccount = Account(id = UUID.randomUUID(), owner = "Felipi B", creationData = LocalDateTime.now())

                    repository.createAccount(newAccount)
                            .test()
                            .assertNoErrors()
                            .assertValue(newAccount)
                            .assertComplete()
                            .dispose()
                }
            }
        }

    }

})