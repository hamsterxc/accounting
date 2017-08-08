package com.lonebytesoft.hamster.accounting.view

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "summary")
@XmlAccessorType(XmlAccessType.FIELD)
class SummaryView {

    @XmlElement
    var time: Long = 0

    @XmlElementWrapper
    @XmlElement(name = "item")
    var items: List<SummaryItemView>? = null

}
