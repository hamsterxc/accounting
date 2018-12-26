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
class Account: Ordered {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0

    @Column(nullable = false)
    var name: String = ""

    @ManyToOne(optional = false)
    @JoinColumn(name = "currency_id", nullable = false)
    lateinit var currency: Currency

    @Column(nullable = false)
    override var ordering: Long = 0
    
    @Column(nullable = false)
    var visible: Boolean = true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Account

        if (id != other.id) return false

        return true
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    override fun toString(): String = "Account(id=$id, name='$name', currency=$currency, ordering=$ordering, visible=$visible)"

    fun toUserString(): String = "#$id '$name'"

    companion object {
        @JvmStatic
        fun toUserString(accounts: Collection<Account>): String =
                accounts.stream().map { it.toUserString() }.collect(Collectors.joining(", "))
    }

}
