package com.lonebytesoft.hamster.accounting.controller.view.output

data class CurrenciesView(
        var currencies: Collection<CurrencyView> = emptyList()
)

data class CurrencyView(
        var id: Long = 0,
        var code: String = "",
        var name: String = "",
        var symbol: String = "",
        var value: Double = 0.0,
        var isDefault: Boolean = false
)
