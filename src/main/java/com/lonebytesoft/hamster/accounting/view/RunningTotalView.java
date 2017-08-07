package com.lonebytesoft.hamster.accounting.view;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Collection;

@XmlRootElement(name = "runningTotal")
@XmlAccessorType(XmlAccessType.FIELD)
public class RunningTotalView {

    @XmlElementWrapper
    @XmlElement(name = "item")
    private Collection<OperationView> items;

    @XmlElement
    private double total;

    public Collection<OperationView> getItems() {
        return items;
    }

    public void setItems(Collection<OperationView> items) {
        this.items = items;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }

}
