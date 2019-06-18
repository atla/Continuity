package com.leihwelt.thedailypicture.messages;

import com.leihwelt.thedailypicture.model.Diary;

public class DiarySelectedEvent {

    private Diary diary;

    public DiarySelectedEvent(Diary diary) {
        this.diary = diary;
    }

    public Diary getDiary() {
        return this.diary;
    }

}
