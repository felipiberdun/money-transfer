package com.felipiberdun.domain.accounts

import io.reactivex.Maybe
import io.reactivex.Single
import java.util.*

interface AccountRepository {

    fun findById(id: UUID): Maybe<Account>

    fun createAccount(account: Account): Single<Account>

    fun findAll(): Single<List<Account>>

}