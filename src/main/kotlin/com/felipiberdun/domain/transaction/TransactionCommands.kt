package com.felipiberdun.domain.transaction

import java.util.*

data class CreateDepositCommand(
        val to: UUID,
        val amount: Float
)

data class CreateTransferCommand(
        val from: UUID,
        val to: UUID,
        val amount: Float
)

data class CreateWithdrawCommand(
        val from: UUID,
        val amount: Float
)
