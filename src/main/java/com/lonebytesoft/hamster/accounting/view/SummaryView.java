package com.lonebytesoft.hamster.accounting.view;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement(name = "summary")
@XmlAccessorType(XmlAccessType.FIELD)
public class SummaryView {

    @XmlElement
    private long time;

    @XmlElementWrapper
    @XmlElement(name = "item")
    private List<SummaryItemView> items;

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public List<SummaryItemView> getItems() {
        return items;
    }

    public void setItems(List<SummaryItemView> items) {
        this.items = items;
    }

}
