package com.felipiberdun.application.transaction

import com.felipiberdun.domain.transaction.TransactionQuery
import com.felipiberdun.domain.transaction.TransactionService
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpResponse.ok
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.reactivex.Single
import java.util.*

@Controller("/accounts/{accountId}")
class TransactionController(private val transactionService: TransactionService) {

    @Get("/")
    fun findTransactionsById(@PathVariable accountId: UUID): Single<HttpResponse<List<TransactionQuery>>> {
        return transactionService.findByAccountId(accountId)
                .map { ok(it) }
    }

}
