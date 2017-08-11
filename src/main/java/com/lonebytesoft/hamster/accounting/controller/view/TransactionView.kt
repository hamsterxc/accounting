package com.lonebytesoft.hamster.accounting.controller.view

class TransactionView {

    var id: Long = 0

    var time: Long = 0

    var categoryId: Long = 0

    var comment: String = ""

    var operations: Collection<OperationView>? = null

}
