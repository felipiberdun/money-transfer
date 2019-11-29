package com.felipiberdun.application.transaction

import com.felipiberdun.domain.accounts.Account
import com.felipiberdun.domain.accounts.AccountRepository
import com.felipiberdun.domain.transaction.*
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpRequest.GET
import io.micronaut.http.HttpRequest.POST
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.HttpStatus.NOT_FOUND
import io.micronaut.http.MediaType.APPLICATION_JSON_TYPE
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class TransactionControllerTest : Spek({

    describe("/accounts/{accountId}") {
        val server = ApplicationContext.run(EmbeddedServer::class.java)
        val client = server.applicationContext.createBean(RxHttpClient::class.java, server.url)
        var accountRepository = server.applicationContext.findBean(AccountRepository::class.java).orElseThrow { RuntimeException() }
        var transactionRepository = server.applicationContext.findBean<TransactionRepository>(TransactionRepository::class.java).orElseThrow { RuntimeException() }

        val originAccount = Account(id = UUID.randomUUID(), owner = "Origin Account", creationData = LocalDateTime.now())
        val destinationAccount = Account(id = UUID.randomUUID(), owner = "Destination Account", creationData = LocalDateTime.now())

        val depositOrigin = Deposit(id = UUID.randomUUID(), to = originAccount, amount = 11f, date = LocalDateTime.now())
        val withdrawOrigin = Withdraw(id = UUID.randomUUID(), from = originAccount, amount = 3f, date = LocalDateTime.now())
        val depositDestination = Deposit(id = UUID.randomUUID(), to = destinationAccount, amount = 10f, date = LocalDateTime.now())

        describe("/transactions") {
            beforeGroup {
                accountRepository.createAccount(originAccount).blockingGet()
                accountRepository.createAccount(destinationAccount).blockingGet()

                transactionRepository.createTransaction(depositOrigin).blockingGet()
                transactionRepository.createTransaction(withdrawOrigin).blockingGet()
                transactionRepository.createTransaction(depositDestination).blockingGet()
            }

            it("GET on ${originAccount.id}/transactions should list all transactions from account") {
                val response: List<*>? = client.toBlocking().retrieve("/accounts/${originAccount.id}/transactions", List::class.java)

                assertNotNull(response)
                assertTrue { response.size == 2 }
            }

            it("GET on ${originAccount.id}/transactions/${depositOrigin.id} should return deposit from origin account") {
                val response: TransactionQuery = client.toBlocking()
                        .retrieve("/accounts/${originAccount.id}/transactions/${depositOrigin.id}", DepositQuery::class.java)

                assertNotNull(response)

                val deposit = (response as DepositQuery)
                assertEquals(deposit.id, depositOrigin.id)
                assertEquals(deposit.to, originAccount.id)
                assertEquals(deposit.amount, depositOrigin.amount)
                assertEquals(deposit.date, depositOrigin.date)
            }

            it("GET on wrong id should return 404 Not Found") {
                val getRequest = GET<TransactionQuery>("/accounts/${originAccount.id}/transactions/${UUID.randomUUID()}")
                try {
                    val response: HttpResponse<TransactionQuery> = client.toBlocking().exchange(getRequest)
                } catch (e: HttpClientResponseException) {
                    assertTrue { e.status == NOT_FOUND }
                }
            }

            it("POST on ${originAccount.id}/transactions/deposits should create a new deposit") {
                val createDepositPayload = CreateDepositPayload(11f)
                val postRequest = POST("/accounts/${originAccount.id}/deposits", createDepositPayload).contentType(APPLICATION_JSON_TYPE)
                val response: HttpResponse<TransactionQuery> = client.toBlocking().exchange(postRequest)

                assertEquals(response.status, HttpStatus.CREATED)
            }

            it("POST on ${originAccount.id}/transactions/transfers should create a new transfer") {
                val createTransferPayload = CreateTransferPayload(5f, destinationAccount.id)
                val postRequest = POST("/accounts/${originAccount.id}/transfers", createTransferPayload).contentType(APPLICATION_JSON_TYPE)
                val response: HttpResponse<TransactionQuery> = client.toBlocking().exchange(postRequest)

                assertEquals(response.status, HttpStatus.CREATED)
            }

            it("POST on ${originAccount.id}/transactions/withdraws should create a new transfer") {
                val createWithdrawPayload = CreateWithdrawPayload(1f)
                val postRequest = POST("/accounts/${originAccount.id}/withdraws", createWithdrawPayload).contentType(APPLICATION_JSON_TYPE)
                val response: HttpResponse<TransactionQuery> = client.toBlocking().exchange(postRequest)

                assertEquals(response.status, HttpStatus.CREATED)
            }

            it("GET account balance should return balance") {
                val response: Float = client.toBlocking().retrieve("/accounts/${originAccount.id}/balance", Float::class.java)

                assertNotNull(response)
                assertTrue { response > 0f }
            }
        }

        afterGroup {
            client.close()
            server.close()
        }
    }
})
