package com.leihwelt.thedailypicture.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.leihwelt.android.helpers.LoremIpsum;

import org.joda.time.DateTime;

public class Entry {

    private String thumbnail;
    private String picture;
    private String description;
    private String longText = LoremIpsum.TWO_PARAGRAPHS;
    private Mood mood = Mood.NONE;

    private int width = -1;
    private int height = -1;

    private DateTime date = DateTime.now();

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

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLongText() {
        return longText;
    }

    public void setLongText(String longText) {
        this.longText = longText;
    }

    @JsonIgnore
    public String toString() {
        return "Entry: " + date.toString() + " " + description + " picture " + width + "x" + height;
    }

    public Mood getMood() {
        return mood;
    }

    public void setMood(Mood mood) {
        this.mood = mood;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @JsonIgnore
    public String getDateStringValue() {
        return Long.toString(getDateString());
    }
}
