package com.lonebytesoft.hamster.accounting.controller.view.output

data class ActionResultView(
        var status: ActionStatus = ActionStatus.ERROR,
        var info: String = ""
)

enum class ActionStatus(val value: String) {
    ERROR("error"),
    SUCCESS("success"),
}
