package com.lonebytesoft.hamster.accounting.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Operation : HasId<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    override var id: Long = 0

    @Column(name = "transaction_id", nullable = false)
    var transactionId: Long = 0

    @Column(name = "account_id", nullable = false)
    var accountId: Long = 0

    @Column
    var amount: Double = 0.toDouble()

    override fun toString(): String = "Operation(id=$id, transactionId=$transactionId, accountId=$accountId, amount=$amount)"

}
