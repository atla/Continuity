package com.leihwelt.thedailypicture.messages;

import org.joda.time.DateTime;

public class DateSelectedEvent {

    private DateTime date;

    public DateSelectedEvent(DateTime date) {
        this.date = date;
    }

    public DateTime getDate() {
        return this.date;
    }

}
