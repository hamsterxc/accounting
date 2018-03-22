package com.lonebytesoft.hamster.accounting.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.Table
import javax.persistence.UniqueConstraint

@Entity
@Table(uniqueConstraints = arrayOf(UniqueConstraint(columnNames = arrayOf("name", "currency_id"))))
class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0

    @Column(nullable = false)
    var name: String = ""

    @ManyToOne(optional = false)
    @JoinColumn(name = "currency_id", nullable = false)
    lateinit var currency: Currency
    
    @Column(nullable = false)
    var visible: Boolean = true

    override fun toString(): String = "Account(id=$id, name='$name', currency=$currency, visible=$visible)"

}
