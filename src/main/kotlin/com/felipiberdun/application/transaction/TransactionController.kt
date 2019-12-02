package com.felipiberdun.application.transaction

import com.felipiberdun.domain.transaction.*
import com.felipiberdun.domain.transaction.TransactionQuery.Companion.fromTransaction
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.*
import io.micronaut.http.MediaType.APPLICATION_JSON
import io.micronaut.http.MutableHttpResponse
import io.micronaut.http.annotation.*
import io.micronaut.http.uri.UriBuilder
import io.micronaut.runtime.server.EmbeddedServer
import io.reactivex.Single
import java.net.URI
import java.util.*

@Controller("/accounts/{accountId}")
class TransactionController(private val transactionService: TransactionService,
                            private val embeddedServer: EmbeddedServer) {

    @Get("/transactions", produces = [APPLICATION_JSON])
    fun findTransactionsById(@PathVariable accountId: UUID): Single<HttpResponse<List<TransactionQuery>>> {
        return transactionService.findByAccountId(accountId)
                .map { ok(it.map { transaction -> fromTransaction(transaction) }) }
    }

    @Get("/transactions/{transactionId}", produces = [APPLICATION_JSON])
    fun findTransactionById(@PathVariable accountId: UUID,
                            @PathVariable transactionId: UUID): Single<MutableHttpResponse<TransactionQuery>> {
        return transactionService.findByAccountAndId(accountId, transactionId)
                .map { ok(fromTransaction(it)) }
                .switchIfEmpty(Single.just(notFound()))
    }

    @Get("/balance", produces = [APPLICATION_JSON])
    fun findCurrentBalance(@PathVariable accountId: UUID): Single<MutableHttpResponse<Float>> {
        return transactionService.getCurrentBalance(accountId)
                .map { ok(it) }
    }

    @Post("/deposits", consumes = [APPLICATION_JSON])
    fun createDeposit(@PathVariable accountId: UUID,
                      @Body createDepositPayload: CreateDepositPayload): Single<MutableHttpResponse<Void>> {
        val createDepositCommand = CreateDepositCommand(
                destination = accountId,
                amount = createDepositPayload.amount
        )

        return transactionService.createDeposit(createDepositCommand)
                .map { deposit -> created<Void>(buildResourceLocationUri(accountId, deposit.id)) }
                .onErrorResumeNext { handleErros(it) }
    }

    @Post("/transfers", consumes = [APPLICATION_JSON])
    fun createTransfer(@PathVariable accountId: UUID,
                       @Body createTransferPayload: CreateTransferPayload): Single<MutableHttpResponse<Void>> {
        val createTransferCommand = CreateTransferCommand(
                origin = accountId,
                destination = createTransferPayload.to,
                amount = createTransferPayload.amount
        )

        return transactionService.createTransfer(createTransferCommand)
                .map { transfer -> created<Void>(buildResourceLocationUri(accountId, transfer.id)) }
                .onErrorResumeNext { handleErros(it) }
    }

    @Post("/withdraws", consumes = [APPLICATION_JSON])
    fun createWithdraw(@PathVariable accountId: UUID,
                       @Body createWithdrawPayload: CreateWithdrawPayload): Single<MutableHttpResponse<Void>> {
        val createWithdrawCommand = CreateWithdrawCommand(
                origin = accountId,
                amount = createWithdrawPayload.amount
        )

        return transactionService.createWithdraw(createWithdrawCommand)
                .map { withdraw -> created<Void>(buildResourceLocationUri(accountId, withdraw.id)) }
                .onErrorResumeNext { handleErros(it) }
    }

    private fun <T> handleErros(throwable: Throwable): Single<MutableHttpResponse<T>> {
        return when (throwable) {
            is AccountNotFoundException -> Single.just(notFound())
            is InsufficientAmountException -> Single.just(badRequest())
            else -> Single.error(throwable)
        }
    }

    private fun buildResourceLocationUri(accountId: UUID, transactionId: UUID): URI {
        return UriBuilder.of(embeddedServer.uri)
                .path("accounts")
                .path(accountId.toString())
                .path("/transactions")
                .path(transactionId.toString())
                .build()
    }

}

data class CreateDepositPayload(val amount: Float)
data class CreateTransferPayload(val amount: Float, val to: UUID)
data class CreateWithdrawPayload(val amount: Float)
