package com.leihwelt.thedailypicture.messages;

import com.leihwelt.thedailypicture.fragments.diary.DiaryFragment.DiaryMode;

public class ChangeDiaryModeEvent {

    private DiaryMode mode;

    public ChangeDiaryModeEvent(DiaryMode mode) {
        this.mode = mode;
    }

    public DiaryMode getMode() {
        return mode;
    }

}
