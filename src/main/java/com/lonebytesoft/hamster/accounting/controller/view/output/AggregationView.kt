package com.lonebytesoft.hamster.accounting.controller.view.output

import com.lonebytesoft.hamster.accounting.controller.view.TransactionAggregationField

data class AggregationView(
        var items: Collection<AggregationItemView> = emptyList()
)

data class AggregationItemView(
        var from: Long = 0,
        var to: Long = 0,
        var field: TransactionAggregationField = TransactionAggregationField.CATEGORY,
        var aggregation: Map<Long, Double> = emptyMap(),
        var total: Double = 0.0
)
