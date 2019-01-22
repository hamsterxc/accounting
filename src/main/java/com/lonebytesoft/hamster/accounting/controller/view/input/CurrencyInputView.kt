package com.lonebytesoft.hamster.accounting.controller.view.input

data class CurrencyInputView(
        var code: String = "",
        var name: String = "",
        var symbol: String = "",
        var value: Double = 0.0,
        var isDefault: Boolean = false
)
