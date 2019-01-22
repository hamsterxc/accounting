package com.lonebytesoft.hamster.accounting.controller.view.output

data class AccountsView(
        var accounts: Collection<AccountView> = emptyList()
)

data class AccountView(
        var id: Long = 0,
        var name: String = "",
        var currencyId: Long = 0,
        var ordering: Long = 0,
        var visible: Boolean = true
)
