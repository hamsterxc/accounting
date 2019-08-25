package com.lonebytesoft.hamster.accounting.service.currency.provider.bankofrussia

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "Valuta")
@XmlAccessorType(XmlAccessType.FIELD)
internal data class BankOfRussiaCurrencies(

        @field:XmlAttribute(name = "Date")
        var date: String? = null,

        @field:XmlElement(name = "Item")
        var currencies: Collection<BankOfRussiaCurrency> = ArrayList()

)

@XmlRootElement(name = "Item")
@XmlAccessorType(XmlAccessType.FIELD)
internal data class BankOfRussiaCurrency(

        @field:XmlAttribute(name = "ID")
        var id: String? = null,

        @field:XmlElement(name = "Name")
        var nameRu: String? = null,

        @field:XmlElement(name = "EngName")
        var nameEn: String? = null,

        @field:XmlElement(name = "Nominal")
        var multiplier: String? = null,

        @field:XmlElement(name = "ParentCode")
        var code: String? = null,

        @field:XmlElement(name = "ISO_Num_Code")
        var numCode: String? = null,

        @field:XmlElement(name = "ISO_Char_Code")
        var isoCode: String? = null

)
