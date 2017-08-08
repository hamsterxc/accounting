package com.lonebytesoft.hamster.accounting.view

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "category")
@XmlAccessorType(XmlAccessType.FIELD)
class CategoryView {

    @XmlElement
    var id: Long = 0

    @XmlElement
    var name: String? = null

}
