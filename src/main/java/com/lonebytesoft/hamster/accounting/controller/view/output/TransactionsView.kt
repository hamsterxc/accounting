package com.lonebytesoft.hamster.accounting.controller.view.output

data class TransactionsView(
        var from: Long = 0,
        var to: Long = 0,
        var transactions: Collection<TransactionView> = emptyList()
)

data class TransactionView(
        var id: Long = 0,
        var time: Long = 0,
        var categoryId: Long = 0,
        var comment: String = "",
        var visible: Boolean = true,
        var operations: Collection<OperationView> = emptyList(),
        var total: Double = 0.0
)
