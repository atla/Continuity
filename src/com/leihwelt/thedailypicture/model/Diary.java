package com.leihwelt.thedailypicture.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.joda.time.DateTime;

public class Diary {

    private String label;
    private String type = ModelType.LOCAL.getType();
    private DateTime date = DateTime.now();

    public Diary() {

    }

    @JsonIgnore
    public DateTime getDate() {
        return date;
    }

    @JsonIgnore
    public void setDate(DateTime date) {
        this.date = date;
    }

    public void setDateString(long timestamp) {
        this.date = new DateTime(timestamp);
    }

    public long getDateString() {
        return date.getMillis();
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @Override
    public boolean equals(Object o) {

        if (o instanceof Diary) {
            return label.equals(((Diary) o).label);
        }

        return false;

    }

    @Override
    public int hashCode() {
        return label.hashCode();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
