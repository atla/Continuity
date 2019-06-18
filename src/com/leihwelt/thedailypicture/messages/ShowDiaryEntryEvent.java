package com.leihwelt.thedailypicture.messages;

import com.leihwelt.thedailypicture.model.Entry;

public class ShowDiaryEntryEvent {

    private Entry entry;

    public ShowDiaryEntryEvent(Entry entry) {
        this.entry = entry;
    }

    public Entry getEntry() {
        return entry;
    }


}
