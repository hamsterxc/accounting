package com.lonebytesoft.hamster.accounting.view

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "runningTotal")
@XmlAccessorType(XmlAccessType.FIELD)
class RunningTotalView {

    @XmlElementWrapper
    @XmlElement(name = "item")
    var items: Collection<OperationView>? = null

    @XmlElement
    var total: Double = 0.toDouble()

}
