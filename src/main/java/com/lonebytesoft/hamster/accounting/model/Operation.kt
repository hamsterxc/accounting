package com.lonebytesoft.hamster.accounting.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
class Operation {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0

    @ManyToOne(optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    lateinit var transaction: Transaction

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    lateinit var account: Account

    @Column
    var amount: Double = 0.toDouble()

    override fun toString(): String = "Operation(id=$id, transactionId=" + transaction.id + ", account=$account, amount=$amount)"

}
