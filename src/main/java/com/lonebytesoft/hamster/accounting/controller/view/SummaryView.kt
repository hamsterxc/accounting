package com.lonebytesoft.hamster.accounting.controller.view

class SummaryView {

    var from: Long = 0

    var to: Long = 0

    var items: Collection<OperationView>? = null

    var total: Double = 0.toDouble()

}
