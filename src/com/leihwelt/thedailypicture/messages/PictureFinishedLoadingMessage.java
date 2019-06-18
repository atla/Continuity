package com.leihwelt.thedailypicture.messages;

public class PictureFinishedLoadingMessage {

    private boolean success;

    public PictureFinishedLoadingMessage(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

}

