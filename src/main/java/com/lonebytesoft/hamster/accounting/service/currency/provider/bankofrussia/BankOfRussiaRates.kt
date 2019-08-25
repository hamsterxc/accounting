package com.lonebytesoft.hamster.accounting.service.currency.provider.bankofrussia

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "ValCurs")
@XmlAccessorType(XmlAccessType.FIELD)
internal data class BankOfRussiaRates(

    @field:XmlAttribute(name = "Date")
    var date: String? = null,

    @field:XmlAttribute(name = "name")
    var name: String? = null,

    @field:XmlElement(name = "Valute")
    var rates: Collection<BankOfRussiaRate> = ArrayList()

)

@XmlRootElement(name = "Valute")
@XmlAccessorType(XmlAccessType.FIELD)
internal data class BankOfRussiaRate(

        @field:XmlAttribute(name = "ID")
        var id: String? = null,

        @field:XmlElement(name = "NumCode")
        var code: String? = null,

        @field:XmlElement(name = "CharCode")
        var isoCode: String? = null,

        @field:XmlElement(name = "Nominal")
        var multiplier: String? = null,

        @field:XmlElement(name = "Value")
        var value: String? = null,

        @field:XmlElement(name = "Name")
        var name: String? = null

)
