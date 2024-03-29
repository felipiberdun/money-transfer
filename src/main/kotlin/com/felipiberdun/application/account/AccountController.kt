package com.felipiberdun.application.account

import com.felipiberdun.domain.accounts.*
import com.felipiberdun.domain.transaction.AccountNotFoundException
import com.felipiberdun.domain.transaction.TransactionQuery
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.*
import io.micronaut.http.HttpResponseFactory
import io.micronaut.http.HttpStatus.CONFLICT
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.http.uri.UriBuilder
import io.micronaut.runtime.server.EmbeddedServer
import io.reactivex.Single
import java.net.URI
import java.util.*
import javax.inject.Singleton

@Singleton
@Controller("/accounts")
class AccountController(private val accountService: AccountService,
                        private val embeddedServer: EmbeddedServer) {

    @Get(value = "/", produces = [MediaType.APPLICATION_JSON])
    fun findAll(): Single<MutableHttpResponse<List<AccountQuery>>> {
        return accountService.findAll()
                .map { ok(it.map { accs -> accs.toQuery() }) }
    }

    @Get(value = "/{accountId}", produces = [MediaType.APPLICATION_JSON])
    fun findById(@PathVariable accountId: UUID): Single<MutableHttpResponse<AccountQuery>> {
        return accountService.findById(accountId)
                .map { ok(it.toQuery()) }
                .onErrorResumeNext { throwable ->
                    when (throwable) {
                        is AccountNotFoundException -> Single.just(notFound())
                        else -> Single.error(throwable)
                    }
                }
    }

    @Post(consumes = [MediaType.APPLICATION_JSON])
    fun createAccount(@Body createAccountCommand: Single<CreateAccountCommand>): Single<MutableHttpResponse<Void>> {
        return createAccountCommand.flatMap {
            accountService.createAccount(it)
                    .map { acc -> created<Void>(buildAccountCreatedUri(acc.id)) }
                    .onErrorResumeNext { throwable ->
                        when (throwable) {
                            is AccountAlreadyExistsException -> Single.just(HttpResponseFactory.INSTANCE.status<Void>(CONFLICT))
                            else -> Single.error(throwable)
                        }
                    }
        }
    }

    private fun buildAccountCreatedUri(accountId: UUID): URI {
        return UriBuilder.of(embeddedServer.uri)
                .path("accounts")
                .path(accountId.toString())
                .build()
    }
}