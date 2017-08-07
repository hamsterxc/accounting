package com.lonebytesoft.hamster.accounting.view;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "accounting")
@XmlAccessorType(XmlAccessType.FIELD)
public class AccountingView {

    @XmlElementWrapper
    @XmlElement(name = "account")
    private List<AccountView> accounts;

    @XmlElementWrapper
    @XmlElement(name = "category")
    private List<CategoryView> categories;

    @XmlElementWrapper
    @XmlElement(name = "transaction")
    private List<TransactionView> transactions;

    @XmlElement
    private RunningTotalView accountsRunningTotalBefore;

    @XmlElement
    private RunningTotalView accountsRunningTotalAfter;

    @XmlElementWrapper
    @XmlElement(name = "block")
    private List<SummaryView> summary;

    public List<AccountView> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<AccountView> accounts) {
        this.accounts = accounts;
    }

    public List<CategoryView> getCategories() {
        return categories;
    }

    public void setCategories(List<CategoryView> categories) {
        this.categories = categories;
    }

    public List<TransactionView> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionView> transactions) {
        this.transactions = transactions;
    }

    public RunningTotalView getAccountsRunningTotalBefore() {
        return accountsRunningTotalBefore;
    }

    public void setAccountsRunningTotalBefore(RunningTotalView accountsRunningTotalBefore) {
        this.accountsRunningTotalBefore = accountsRunningTotalBefore;
    }

    public RunningTotalView getAccountsRunningTotalAfter() {
        return accountsRunningTotalAfter;
    }

    public void setAccountsRunningTotalAfter(RunningTotalView accountsRunningTotalAfter) {
        this.accountsRunningTotalAfter = accountsRunningTotalAfter;
    }

    public List<SummaryView> getSummary() {
        return summary;
    }

    public void setSummary(List<SummaryView> summary) {
        this.summary = summary;
    }

}
