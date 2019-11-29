package com.felipiberdun.application.account

import com.felipiberdun.domain.accounts.Account
import com.felipiberdun.domain.accounts.AccountQuery
import com.felipiberdun.domain.accounts.AccountRepository
import com.felipiberdun.domain.accounts.CreateAccountCommand
import io.micronaut.context.ApplicationContext
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest.POST
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType.APPLICATION_JSON_TYPE
import io.micronaut.http.client.RxHttpClient
import io.micronaut.runtime.server.EmbeddedServer
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class AccountControllerTest : Spek({

    describe("/accounts") {
        val server = ApplicationContext.run(EmbeddedServer::class.java)
        val client = server.applicationContext.createBean(RxHttpClient::class.java, server.url)
        val repository = server.applicationContext.findBean(AccountRepository::class.java).orElseThrow { RuntimeException() }

        it("POST on /accounts should create a new account") {
            val createAccountCommand = CreateAccountCommand("Felipi")
            val request = POST("/accounts", createAccountCommand).contentType(APPLICATION_JSON_TYPE)

            val response: HttpResponse<AccountQuery> = client.toBlocking().exchange(request)

            assertEquals(HttpStatus.CREATED, response.status)

            val locationHeader = response.header(HttpHeaders.LOCATION)
            assertNotNull(locationHeader)
        }

        it("GET on /accounts/ should return all accounts") {
            val createAccountCommand = CreateAccountCommand("Felipi")
            val createRequest = POST("/accounts", createAccountCommand).contentType(APPLICATION_JSON_TYPE)

            val createResponse: HttpResponse<AccountQuery> = client.toBlocking().exchange(createRequest)

            val response: List<*>? = client.toBlocking().retrieve("/accounts", List::class.java)

            assertNotNull(response)
            assertTrue(response.isNotEmpty())
        }

        it("GET on /accounts/accountId should return acount") {
            val account = Account(id = UUID.randomUUID(), owner = "Felipi B", creationData = LocalDateTime.now())
            repository.createAccount(account).blockingGet()

            val response: AccountQuery = client.toBlocking().retrieve("/accounts/${account.id}", AccountQuery::class.java)

            assertEquals(account.id, response.id)
            assertEquals(account.owner, response.owner)
            assertEquals(account.creationData, response.creationDate)
        }

        afterGroup {
            client.close()
            server.close()
        }
    }
})
