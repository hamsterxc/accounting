package com.lonebytesoft.hamster.accounting.view

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "transaction")
@XmlAccessorType(XmlAccessType.FIELD)
class TransactionView {

    @XmlElement
    var id: Long = 0

    @XmlElement
    var time: Long = 0

    @XmlElementWrapper
    @XmlElement(name = "operation")
    var operations: Collection<OperationView>? = null

    @XmlElement
    var total: Double = 0.toDouble()

    @XmlElement
    var category: String? = null

    @XmlElement
    var comment: String? = null

}
