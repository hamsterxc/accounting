package com.lonebytesoft.hamster.accounting.view

import javax.xml.bind.annotation.XmlAccessType
import javax.xml.bind.annotation.XmlAccessorType
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "accounting")
@XmlAccessorType(XmlAccessType.FIELD)
class AccountingView {

    @XmlElementWrapper
    @XmlElement(name = "account")
    var accounts: List<AccountView>? = null

    @XmlElementWrapper
    @XmlElement(name = "category")
    var categories: List<CategoryView>? = null

    @XmlElementWrapper
    @XmlElement(name = "transaction")
    var transactions: List<TransactionView>? = null

    @XmlElement
    var accountsRunningTotalBefore: RunningTotalView? = null

    @XmlElement
    var accountsRunningTotalAfter: RunningTotalView? = null

    @XmlElementWrapper
    @XmlElement(name = "block")
    var summary: List<SummaryView>? = null

}
