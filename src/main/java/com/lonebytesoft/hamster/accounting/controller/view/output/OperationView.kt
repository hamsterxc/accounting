package com.lonebytesoft.hamster.accounting.controller.view.output

data class OperationView(
        var accountId: Long = 0,
        var currencyId: Long? = null,
        var amount: Double = 0.0,
        var isActive: Boolean = true
)
