package com.felipiberdun.application.account

import com.felipiberdun.domain.accounts.Account
import com.felipiberdun.domain.accounts.AccountRepository
import io.reactivex.Maybe
import io.reactivex.Single
import java.util.*
import java.util.concurrent.ConcurrentMap
import java.util.concurrent.CopyOnWriteArrayList
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class AccountInMemoryRepository(@Inject @Named("accountDataSource") private val accounts: ConcurrentMap<UUID, Account>) : AccountRepository {

    override fun findById(id: UUID): Maybe<Account> {
        return accounts[id]?.let { Maybe.just(it) } ?: Maybe.empty<Account>()
    }

    override fun createAccount(account: Account): Single<Account> {
        return persist(account)
    }

    override fun findAll(): Single<List<Account>> {
        return Single.just(CopyOnWriteArrayList(accounts.values))
    }

    private fun persist(account: Account): Single<Account> {
        accounts[account.id] = account
        return Single.just(account)
    }
}