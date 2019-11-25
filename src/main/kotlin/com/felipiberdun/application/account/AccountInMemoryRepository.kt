package com.felipiberdun.application.account

import com.felipiberdun.domain.accounts.Account
import com.felipiberdun.domain.accounts.AccountAlreadyExistsException
import com.felipiberdun.domain.accounts.AccountRepository
import io.reactivex.Maybe
import io.reactivex.Single
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import javax.inject.Singleton

@Singleton
class AccountInMemoryRepository : AccountRepository {

    private val accounts: ConcurrentMap<UUID, Account> = ConcurrentHashMap()

    override fun findById(id: UUID): Maybe<Account> {
        return accounts[id]?.let { Maybe.just(it) } ?: Maybe.empty<Account>()
    }

    override fun createAccount(account: Account): Single<Account> {
        return findById(account.id)
                .flatMap<Account> { Maybe.error(AccountAlreadyExistsException) }
                .switchIfEmpty(persist(account))
    }

    private fun persist(account: Account): Single<Account> {
        accounts[account.id] = account
        return Single.just(account)
    }
}