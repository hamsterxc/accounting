package com.lonebytesoft.hamster.accounting.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.Id

@Entity
class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long = 0

    @Column(unique = true, nullable = false)
    var name: String = ""

    @Column(nullable = false)
    var ordering: Long = 0

    override fun toString(): String = "Category(id=$id, name='$name', ordering=$ordering)"

}
