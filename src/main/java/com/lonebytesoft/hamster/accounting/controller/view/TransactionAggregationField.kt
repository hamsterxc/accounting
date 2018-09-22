package com.lonebytesoft.hamster.accounting.controller.view

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class TransactionAggregationField {

    CATEGORY,
    ACCOUNT,
    ;

    @JsonValue
    fun jsonValue(): String = name.toLowerCase()

    companion object {
        private val BY_NAME = values().associateBy { it.name.toLowerCase() }

        @JsonCreator
        fun getByName(name: String): TransactionAggregationField = BY_NAME[name]
                ?: throw IllegalArgumentException("Field $name not found")
    }

}
