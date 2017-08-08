package com.lonebytesoft.hamster.accounting.model

import java.util.Date
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Transaction : HasId<Long> {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    override var id: Long = 0

    @Column(nullable = false)
    var time: Long = 0

    @Column(name = "category_id", nullable = false)
    var categoryId: Long = 0

    @Column
    var comment: String? = null

    override fun toString(): String = "Transaction(id=$id, time='" + Date(time) + "', categoryId=$categoryId, comment='$comment')"

}
