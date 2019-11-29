package com.felipiberdun.domain.account

import com.felipiberdun.domain.accounts.*
import com.felipiberdun.domain.transaction.AccountNotFoundException
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

object AccountServiceTest : Spek({

    describe("AccountService") {
        val repository = mockk<AccountRepository>()
        val accountService = AccountService(repository)

        beforeEachTest { clearMocks(repository) }

        describe("Searching by id") {
            val accountId = UUID.randomUUID()
            val foundAccount = Account(id = accountId, owner = "Felipi", creationData = LocalDateTime.now())

            it("with existing account returns account") {
                every { repository.findById(accountId) }.returns(Maybe.just(foundAccount))

                accountService.findById(accountId)
                        .test()
                        .assertNoErrors()
                        .assertValue(foundAccount)
                        .assertComplete()
                        .dispose()

                verify { repository.findById(accountId) }
            }

            it("with no existing account returns AccountNotFoundException") {
                every { repository.findById(any()) } returns Maybe.empty()

                accountService.findById(accountId)
                        .test()
                        .assertNoValues()
                        .assertError(AccountNotFoundException::class.java)
                        .assertError { t -> (t as AccountNotFoundException).accountId == accountId }
                        .dispose()

                verify { repository.findById(accountId) }
            }
        }

        describe("Creating account") {
            val accountId = UUID.randomUUID()
            val account = Account(id = accountId, owner = "Felipi", creationData = LocalDateTime.now())

            it("if doesn't exist, creates successfully") {
                every { repository.findById(any()) } returns Maybe.empty()
                every { repository.createAccount(any()) } returns Single.just(account)

                val createAccountCommand = CreateAccountCommand(owner = account.owner)

                accountService.createAccount(createAccountCommand)
                        .test()
                        .assertNoErrors()
                        .assertValueCount(1)
                        .assertValue { acc -> acc.owner == createAccountCommand.owner }
                        .assertComplete()
                        .dispose()

                verify { repository.findById(any()) }
                verify(exactly = 1) { repository.createAccount(any()) }
            }

            it("if exists, returns AccountAlreadyExistsException") {
                every { repository.findById(any()) } returns Maybe.just(account)
                every { repository.createAccount(any()) } returns Single.just(account)

                val createAccountCommand = CreateAccountCommand(owner = account.owner)

                accountService.createAccount(createAccountCommand)
                        .test()
                        .assertNoValues()
                        .assertError(AccountAlreadyExistsException::class.java)
                        .dispose()

                verify { repository.findById(any()) }
                verify(exactly = 1) { repository.createAccount(any()) }
            }
        }
    }

})