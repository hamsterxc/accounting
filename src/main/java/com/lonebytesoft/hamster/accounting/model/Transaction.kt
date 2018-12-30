package com.lonebytesoft.hamster.accounting.model

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.stream.Collectors
import javax.persistence.CascadeType
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.FetchType
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id
import javax.persistence.JoinColumn
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
data class Transaction(

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0,

    @Column(nullable = false)
    var time: Long = 0,

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    var category: Category = Category(),

    @Column
    var comment: String = "",

    @Column(nullable = false)
    var visible: Boolean = true

) {

    @OneToMany(cascade = [CascadeType.ALL], mappedBy = "transaction", orphanRemoval = true, fetch = FetchType.EAGER)
    var operations: Collection<Operation> = emptyList()

    fun toUserString(): String = "#$id ${TRANSACTION_DATE_FORMAT.get().format(Date(time))} '$comment'"

    companion object {
        val TRANSACTION_DATE_FORMAT: ThreadLocal<DateFormat> = ThreadLocal.withInitial { SimpleDateFormat("dd MMM yyyy") }

        @JvmStatic
        fun toUserString(transactions: Collection<Transaction>): String =
                transactions.stream().map { it.toUserString() }.collect(Collectors.joining(", "))
    }

}
