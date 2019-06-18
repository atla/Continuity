package com.leihwelt.thedailypicture.messages;

import com.leihwelt.thedailypicture.model.Diary;

public class SetToDiaryEvent {

    private Diary diary;

    public SetToDiaryEvent(Diary diary) {
        this.diary = diary;
    }

    public Diary getDiary() {
        return this.diary;
    }

}
