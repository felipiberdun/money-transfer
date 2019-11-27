package com.felipiberdun.domain.accounts

import com.felipiberdun.domain.transaction.AccountNotFoundException
import io.reactivex.Single
import java.time.LocalDateTime
import java.util.*
import javax.inject.Singleton

@Singleton
class AccountService(private val repository: AccountRepository) {

    fun findById(id: UUID): Single<Account> {
        return repository.findById(id)
                .switchIfEmpty(Single.error(AccountNotFoundException(id)))
    }

    fun createAccount(createAccountCommand: CreateAccountCommand): Single<Account> {
        val account = Account(UUID.randomUUID(), createAccountCommand.owner, LocalDateTime.now())
        return repository.createAccount(account)
    }

}