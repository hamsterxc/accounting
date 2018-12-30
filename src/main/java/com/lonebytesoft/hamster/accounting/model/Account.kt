package com.lonebytesoft.hamster.accounting.model

import java.util.stream.Collectors
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
@Table(uniqueConstraints = [UniqueConstraint(columnNames = ["name", "currency_id"])])
data class Account(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    @ManyToOne(optional = false)
    @JoinColumn(name = "currency_id", nullable = false)
    var currency: Currency = Currency(),

    @Column(nullable = false)
    override var ordering: Long = 0,
    
    @Column(nullable = false)
    var visible: Boolean = true

): Ordered {

    fun toUserString(): String = "#$id '$name'"

    companion object {
        @JvmStatic
        fun toUserString(accounts: Collection<Account>): String =
                accounts.stream().map { it.toUserString() }.collect(Collectors.joining(", "))
    }

}
