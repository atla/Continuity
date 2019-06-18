package com.leihwelt.thedailypicture.messages;

import org.joda.time.DateTime;

public class NoPictureForDateEvent {

    private DateTime date;

    public NoPictureForDateEvent(DateTime date) {
        this.date = date;
    }

    public DateTime getDate() {
        return this.date;
    }

}
