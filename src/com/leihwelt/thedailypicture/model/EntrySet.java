package com.leihwelt.thedailypicture.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

public class EntrySet {

    private Map<String, Entry> entries = new HashMap<String, Entry>();
    private DateTime diaryStartDate = new DateTime();
    private String name;

    public void setDateString(long timestamp) {
        this.diaryStartDate = new DateTime(timestamp);
    }

    public long getDateString() {
        return diaryStartDate.getMillis();
    }

    public EntrySet() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @JsonIgnore
    public void add(Entry entry) {

        String hashkey = createHashKey(entry.getDate());

        getEntries().put(hashkey, entry);

    }

    @JsonIgnore
    public Entry getEntry(DateTime dateTime) {
        String hashkey = createHashKey(dateTime);

        return getEntries().get(hashkey);
    }

    @JsonIgnore
    public boolean hasEntry(DateTime dateTime) {
        String hashkey = createHashKey(dateTime);

        return getEntries().containsKey(hashkey);
    }

    private String createHashKey(DateTime dateTaken) {

        StringBuilder b = new StringBuilder();
        b.append(dateTaken.getYear());
        b.append(dateTaken.getMonthOfYear());
        b.append(dateTaken.getDayOfMonth());

        return b.toString();
    }

    @JsonIgnore
    public Entry getOrCreateEntry(DateTime date, DataModel model) {

        if (!this.hasEntry(date)) {
            Entry entry = new Entry();
            entry.setDate(date);

            if (entry.getPicture() == null) {
                entry.setPicture(model.getPictureFilenameForDate(date));
            }

            this.add(entry);
        }

        return this.getEntry(date);
    }

    public Map<String, Entry> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, Entry> entries) {
        this.entries = entries;

    }

    @JsonIgnore
    public DateTime getDiaryStartDate() {
        return diaryStartDate;
    }

    @JsonIgnore
    public void setDiaryStartDate(DateTime diaryStartDate) {
        this.diaryStartDate = diaryStartDate;
    }
}
