package com.lonebytesoft.hamster.accounting.model

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "config")
class ConfigEntry {

    @Id
    var key: String = ""

    @Column
    var value: String? = null

    override fun toString(): String = "ConfigEntry(key='$key', value='$value')"

}
