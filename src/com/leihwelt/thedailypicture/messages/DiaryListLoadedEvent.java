package com.leihwelt.thedailypicture.messages;

import com.leihwelt.thedailypicture.model.DiaryList;

public class DiaryListLoadedEvent {

    private DiaryList list;

    public DiaryListLoadedEvent(DiaryList list) {
        this.list = list;
    }

    public DiaryList getDiaryList() {
        return this.list;
    }

}
