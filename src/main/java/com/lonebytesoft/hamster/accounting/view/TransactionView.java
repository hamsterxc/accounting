package com.lonebytesoft.hamster.accounting.view;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

@XmlRootElement(name = "transaction")
@XmlAccessorType(XmlAccessType.FIELD)
public class TransactionView {

    @XmlElement
    private long id;

    @XmlElement
    private long time;

    @XmlElementWrapper
    @XmlElement(name = "operation")
    private Collection<OperationView> operations;

    @XmlElement
    private double total;

    @XmlElement
    private String category;

    @XmlElement
    private String comment;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Collection<OperationView> getOperations() {
        return operations;
    }

    public void setOperations(Collection<OperationView> operations) {
        this.operations = operations;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

}
