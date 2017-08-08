package com.lonebytesoft.hamster.accounting.view

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "item")
@XmlAccessorType(XmlAccessType.FIELD)
class SummaryItemView {

    @XmlElement
    var name: String? = null

    @XmlElement
    var amount: Double = 0.toDouble()

}
