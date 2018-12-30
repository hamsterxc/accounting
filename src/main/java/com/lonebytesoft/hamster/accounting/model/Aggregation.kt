package com.lonebytesoft.hamster.accounting.model

data class Aggregation(
        val items: Map<Long, Double>,
        val total: Double
)
