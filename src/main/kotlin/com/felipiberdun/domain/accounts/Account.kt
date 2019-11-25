package com.felipiberdun.domain.accounts

import java.time.LocalDateTime
import java.util.*

object AccountAlreadyExistsException: Exception()

data class Account(
        val id: UUID,
        val owner: String,
        val creationData: LocalDateTime
)