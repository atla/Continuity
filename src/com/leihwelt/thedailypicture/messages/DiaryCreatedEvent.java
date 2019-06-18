package com.leihwelt.thedailypicture.messages;

import com.leihwelt.thedailypicture.model.Diary;

public class DiaryCreatedEvent {

    private Diary diary;

    public DiaryCreatedEvent(Diary diary) {
        this.diary = diary;
    }

    public Diary getDiary() {
        return this.diary;
    }

}
