package com.lonebytesoft.hamster.accounting.view

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "operation")
@XmlAccessorType(XmlAccessType.FIELD)
class OperationView {

    @XmlElement
    var id: Long = 0

    @XmlElement
    var amount: Double = 0.toDouble()

}
