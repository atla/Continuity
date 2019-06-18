package com.leihwelt.thedailypicture.messages;

import android.graphics.Bitmap;

public class ShowDiaryEntryPictureEvent {

    private Bitmap picture;

    public ShowDiaryEntryPictureEvent(Bitmap picture) {
        this.picture = picture;
    }

    public Bitmap getPicture() {
        return picture;
    }

}
