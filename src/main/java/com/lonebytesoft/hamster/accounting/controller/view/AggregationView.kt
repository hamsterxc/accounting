package com.lonebytesoft.hamster.accounting.controller.view

class AggregationView(
    var items: Collection<AggregationItemView>
)

class AggregationItemView(
        var from: Long,
        var to: Long,
        var field: TransactionAggregationField,
        var aggregation: Map<Long, Double>,
        var total: Double
)

class AggregationInputView {
    lateinit var filters: Collection<AggregationInputFilter>
    lateinit var field: TransactionAggregationField
    var includeHidden: Boolean = false
}

class AggregationInputFilter {
    var from: Long? = null
    var fromDate: String? = null
    var to: Long? = null
    var toDate: String? = null
}
