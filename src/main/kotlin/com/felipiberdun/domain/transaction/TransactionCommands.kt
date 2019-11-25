package com.felipiberdun.domain.transaction

import java.util.*

sealed class CreateTransactionCommand

data class CreateDepositCommand(
        val to: UUID,
        val amount: Float
) : CreateTransactionCommand()

data class CreateTransferCommand(
        val from: UUID,
        val to: UUID,
        val amount: Float
) : CreateTransactionCommand()

data class CreateWithdrawCommand(
        val from: UUID,
        val amount: Float
) : CreateTransactionCommand()
