package com.lonebytesoft.hamster.accounting.controller.view

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue
import com.lonebytesoft.hamster.accounting.controller.exception.UnsupportedAggregationException

enum class TransactionAggregationField {

    CATEGORY,
    ACCOUNT,
    ;

    @JsonValue
    fun jsonValue(): String = name.toLowerCase()

    companion object {
        private val BY_NAME = values().associateBy { it.name.toLowerCase() }

        @JsonCreator
        fun getByName(name: String): TransactionAggregationField = BY_NAME[name.toLowerCase()]
                ?: throw UnsupportedAggregationException(name)
    }

}
