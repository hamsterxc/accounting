package com.lonebytesoft.hamster.accounting.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class Account : HasId<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    override var id: Long = 0

    @Column(unique = true, nullable = false)
    var name: String = ""

    @ManyToOne(optional = false)
    @JoinColumn(name = "currency_id", nullable = false)
    lateinit var currency: Currency

    override fun toString(): String = "Account(id=$id, name='$name', currency=$currency)"

}
