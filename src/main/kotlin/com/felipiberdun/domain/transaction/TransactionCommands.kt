package com.felipiberdun.domain.transaction

import java.util.*

data class CreateDepositCommand(
        val destination: UUID,
        val amount: Float
)

data class CreateTransferCommand(
        val origin: UUID,
        val destination: UUID,
        val amount: Float
)

data class CreateWithdrawCommand(
        val origin: UUID,
        val amount: Float
)
