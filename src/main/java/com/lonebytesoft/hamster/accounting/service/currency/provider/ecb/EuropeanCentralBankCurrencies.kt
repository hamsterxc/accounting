package com.lonebytesoft.hamster.accounting.service.currency.provider.ecb

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlAttribute
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "Envelope", namespace = "http://www.gesmes.org/xml/2002-08-01")
@XmlAccessorType(XmlAccessType.FIELD)
internal data class EuropeanCentralBankEnvelope(

        @field:XmlElement(name = "Cube", namespace = "http://www.ecb.int/vocabulary/2002-08-01/eurofxref")
        var data: EuropeanCentralBankDataWrapper? = null

)

@XmlRootElement(name = "Cube")
@XmlAccessorType(XmlAccessType.FIELD)
internal data class EuropeanCentralBankDataWrapper(

        @field:XmlElement(name = "Cube", namespace = "http://www.ecb.int/vocabulary/2002-08-01/eurofxref")
        var currencies: EuropeanCentralBankCurrencies? = null

)

@XmlRootElement(name = "Cube")
@XmlAccessorType(XmlAccessType.FIELD)
internal data class EuropeanCentralBankCurrencies(

        @field:XmlAttribute(name = "time")
        var date: String? = null,

        @field:XmlElement(name = "Cube", namespace = "http://www.ecb.int/vocabulary/2002-08-01/eurofxref")
        var rates: Collection<EuropeanCentralBankCurrency> = ArrayList()

)

@XmlRootElement(name = "Cube")
@XmlAccessorType(XmlAccessType.FIELD)
internal data class EuropeanCentralBankCurrency(

        @field:XmlAttribute(name = "currency")
        var code: String? = null,

        @field:XmlAttribute(name = "rate")
        var value: String? = null

)
