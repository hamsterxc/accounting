package com.lonebytesoft.hamster.accounting.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "config")
class ConfigEntry : HasId<String> {

    @Id
    var key: String = ""

    @Column
    var value: String? = null

    override val id: String
        get() = key

    override fun toString(): String = "ConfigEntry(key='$key', value='$value')"

}
