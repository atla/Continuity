package com.leihwelt.thedailypicture.messages;

import android.graphics.Bitmap;

public class TodayPictureLoadedMessage {

    private Bitmap bitmap = null;

    public TodayPictureLoadedMessage(Bitmap bitmap) {
        this.bitmap = bitmap;
    }


    public Bitmap getBitmap() {
        return this.bitmap;
    }

}
