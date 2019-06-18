package com.leihwelt.thedailypicture.messages;

import com.leihwelt.thedailypicture.model.Diary;

public class DeleteDiaryEvent {

    private Diary diary;
    private boolean deepDelete = false;

    public DeleteDiaryEvent(Diary diary) {
        this.diary = diary;
    }

    public Diary getDiary() {
        return this.diary;
    }

    public boolean isDeepDelete() {
        return deepDelete;
    }

    public void setDeepDelete(boolean deepDelete) {
        this.deepDelete = deepDelete;
    }

}
