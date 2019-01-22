package com.lonebytesoft.hamster.accounting.controller.view.input

import com.lonebytesoft.hamster.accounting.controller.view.TransactionAggregationField

data class AggregationInputView(
        var filters: Collection<AggregationInputFilter> = emptyList(),
        var field: TransactionAggregationField = TransactionAggregationField.CATEGORY,
        var includeHidden: Boolean = false
)

data class AggregationInputFilter(
        var from: Long? = null,
        var fromDate: String? = null,
        var to: Long? = null,
        var toDate: String? = null
)
