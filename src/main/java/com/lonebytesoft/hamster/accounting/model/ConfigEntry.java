package com.lonebytesoft.hamster.accounting.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "config")
public class ConfigEntry implements HasId<String> {

    @Id
    @Column
    private String key;

    @Column
    private String value;

    @Override
    public String getId() {
        return key;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "ConfigEntry{" +
                "key='" + key + '\'' +
                ", value='" + value + '\'' +
                '}';
    }

}
