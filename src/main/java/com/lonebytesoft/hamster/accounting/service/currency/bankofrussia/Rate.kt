package com.lonebytesoft.hamster.accounting.service.currency.bankofrussia

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "Valute")
@XmlAccessorType(XmlAccessType.FIELD)
class Rate {

    @XmlAttribute(name = "ID")
    var id: String? = null

    @XmlElement(name = "NumCode")
    var code: String? = null

    @XmlElement(name = "CharCode", required = true)
    lateinit var isoCode: String

    @XmlElement(name = "Nominal", required = true)
    lateinit var multiplier: String

    @XmlElement(name = "Value", required = true)
    lateinit var value: String

    @XmlElement(name = "Name", required = true)
    lateinit var name: String

    override fun toString(): String {
        return "Rate(id=$id, code=$code, isoCode='$isoCode', multiplier='$multiplier', value='$value', name='$name')"
    }

}
