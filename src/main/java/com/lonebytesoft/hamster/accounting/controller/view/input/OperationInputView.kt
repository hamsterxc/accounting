package com.lonebytesoft.hamster.accounting.controller.view.input

data class OperationInputView(
        var accountId: Long = 0,
        var amount: String = "",
        var isActive: Boolean = true
)
