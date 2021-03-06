package com.lonebytesoft.hamster.accounting.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
data class Currency(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,

    @Column(unique = true, nullable = false)
    var code: String = "",

    @Column(unique = true, nullable = false)
    var name: String = "",

    @Column(unique = true, nullable = false)
    var symbol: String = "",

    @Column(name = "value_default")
    var value: Double = 0.toDouble()

)
