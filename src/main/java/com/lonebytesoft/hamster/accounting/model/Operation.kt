package com.lonebytesoft.hamster.accounting.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne

@Entity
data class Operation(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    var account: Account = Account(),

    @ManyToOne(optional = true)
    @JoinColumn(name = "currency_id")
    var currency: Currency? = null,

    @Column
    var amount: Double = 0.toDouble(),

    @Column
    var isActive: Boolean = true

) {

    @ManyToOne(optional = false)
    @JoinColumn(name = "transaction_id", nullable = false)
    var transaction: Transaction = Transaction()

}
