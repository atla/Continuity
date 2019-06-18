package com.leihwelt.thedailypicture.messages;

import com.leihwelt.thedailypicture.model.Entry;

public class PictureInvalidatedEvent {

    private Entry entry;

    public PictureInvalidatedEvent(Entry entry) {
        this.entry = entry;
    }

    public Entry getEntry() {
        return entry;
    }

}
