package com.felipiberdun.application

import com.felipiberdun.domain.accounts.CreateAccountCommand
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Post

@Controller("/accounts")
class AccountController {

    @Post(consumes = [MediaType.APPLICATION_JSON])
    fun createAccount(@Body createAccountCommand: CreateAccountCommand): HttpStatus {
        return HttpStatus.CREATED
    }
}