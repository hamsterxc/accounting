package com.lonebytesoft.hamster.accounting.model

import java.util.Date
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
class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0

    @Column(nullable = false)
    var time: Long = 0

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    lateinit var category: Category

    @Column
    var comment: String = ""

    @Column(nullable = false)
    var visible: Boolean = true

    @OneToMany(cascade = arrayOf(CascadeType.ALL), mappedBy = "transaction", orphanRemoval = true, fetch = FetchType.EAGER)
    lateinit var operations: Collection<Operation>

    override fun toString(): String = "Transaction(id=$id, time='" + Date(time) +
            "', category=$category, comment='$comment', visible=$visible, operations=$operations)"

}
