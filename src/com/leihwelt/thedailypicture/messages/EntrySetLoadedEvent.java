package com.leihwelt.thedailypicture.messages;

import com.leihwelt.thedailypicture.model.EntrySet;

public class EntrySetLoadedEvent {

    private EntrySet entrySet;

    public EntrySetLoadedEvent(EntrySet entrySet) {
        this.entrySet = entrySet;
    }

    public EntrySet getEntrySet() {
        return entrySet;
    }

}
