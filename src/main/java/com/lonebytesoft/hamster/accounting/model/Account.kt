package com.lonebytesoft.hamster.accounting.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Account : HasId<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    override var id: Long = 0

    @Column(unique = true, nullable = false)
    var name: String = ""

    @Column(name = "currency_id", nullable = false)
    var currencyId: Long = 0

    override fun toString(): String = "Account(id=$id, name='$name', currencyId=$currencyId)"

}
