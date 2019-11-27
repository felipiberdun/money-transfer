package com.felipiberdun.application.transaction

import com.felipiberdun.domain.transaction.*
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
                .map { ok(it.map { transaction -> transaction.toQuery() }) }
    }

    @Get("/transactions/{transactionId}", produces = [APPLICATION_JSON])
    fun findTransactionById(@PathVariable accountId: UUID,
                            @PathVariable transactionId: UUID): Single<MutableHttpResponse<TransactionQuery>> {
        return transactionService.findByAccountAndId(accountId, transactionId)
                .map { ok(it.toQuery()) }
                .switchIfEmpty(Single.just(notFound()))
    }

    @Post("/deposits", consumes = [APPLICATION_JSON])
    fun createDeposit(@PathVariable accountId: UUID,
                      @Body createDepositPayload: CreateDepositPayload): Single<HttpResponse<Void>> {
        val createDepositCommand = CreateDepositCommand(
                to = accountId,
                amount = createDepositPayload.amount
        )

        return transactionService.createDeposit(createDepositCommand)
                .map { deposit -> created<Void>(buildResourceLocationUri(accountId, deposit.id)) }
    }

    @Post("/transfers", consumes = [APPLICATION_JSON])
    fun createTransfer(@PathVariable accountId: UUID,
                       @Body createTransferPayload: CreateTransferPayload): Single<HttpResponse<Void>> {
        val createTransferCommand = CreateTransferCommand(
                from = accountId,
                to = createTransferPayload.to,
                amount = createTransferPayload.amount
        )

        return transactionService.createTransfer(createTransferCommand)
                .map { transfer -> created<Void>(buildResourceLocationUri(accountId, transfer.id)) }
    }

    @Post("/withdraws", consumes = [APPLICATION_JSON])
    fun createWithdraw(@PathVariable accountId: UUID,
                       @Body createWithdrawPayload: CreateWithdrawPayload): Single<HttpResponse<Void>> {
        val createWithdrawCommand = CreateWithdrawCommand(
                from = accountId,
                amount = createWithdrawPayload.amount
        )

        return transactionService.createWithdraw(createWithdrawCommand)
                .map { withdraw -> created<Void>(buildResourceLocationUri(accountId, withdraw.id)) }
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
