package com.felipiberdun.domain.transaction

import java.util.*

sealed class CreateTransactionCommand(open val amount: Float)

data class CreateDepositCommand(
        val to: UUID,
        override val amount: Float
) : CreateTransactionCommand(amount)

data class CreateTransferCommand(
        val from: UUID,
        val to: UUID,
        override val amount: Float
) : CreateTransactionCommand(amount)

data class CreateWithdrawCommand(
        val from: UUID,
        override val amount: Float
) : CreateTransactionCommand(amount)
