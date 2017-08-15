package com.lonebytesoft.hamster.accounting.controller.view

class ActionResultView {

    var status: Status = Status.ERROR

    var info: String = ""

    enum class Status(val value: String) {
        ERROR("error"),
        SUCCESS("success"),
    }

}
