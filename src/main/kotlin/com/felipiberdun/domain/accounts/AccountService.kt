package com.felipiberdun.domain.accounts

import io.reactivex.Maybe
import io.reactivex.Single
import java.time.LocalDateTime
import java.util.*
import javax.inject.Singleton

@Singleton
class AccountService(private val repository: AccountRepository) {

    fun findById(id: UUID): Maybe<Account> {
        return repository.findById(id)
    }

    fun createAccount(createAccountCommand: CreateAccountCommand): Single<Account> {
        val account = Account(UUID.randomUUID(), createAccountCommand.owner, LocalDateTime.now())
        return repository.createAccount(account)
    }

}