package com.felipiberdun.domain.accounts

import java.time.LocalDateTime
import java.util.*

data class AccountQuery(
        val id: UUID,
        val owner: String,
        val creationDate: LocalDateTime
)

fun Account.toQuery() = AccountQuery(
        id = this.id,
        owner = this.owner,
        creationDate = this.creationData
)