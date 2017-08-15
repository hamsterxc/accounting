package com.lonebytesoft.hamster.accounting.controller.view

class TransactionInputView {

    var date: String = ""

    var categoryId: Long = 0

    var comment: String = ""

    var operations: Collection<OperationView>? = null

}
