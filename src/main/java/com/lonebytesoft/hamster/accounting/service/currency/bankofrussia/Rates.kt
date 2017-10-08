package com.lonebytesoft.hamster.accounting.service.currency.bankofrussia

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "ValCurs")
@XmlAccessorType(XmlAccessType.FIELD)
class Rates {

    @XmlAttribute(name = "Date", required = true)
    lateinit var date: String

    @XmlAttribute(name = "name")
    var name: String? = null

    @XmlElement(name = "Valute", required = true)
    lateinit var rates: Collection<Rate>

    override fun toString(): String {
        return "Rates(date='$date', name=$name, rates=$rates)"
    }

}
