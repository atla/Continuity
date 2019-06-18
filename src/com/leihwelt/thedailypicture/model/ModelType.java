package com.leihwelt.thedailypicture.model;

public enum ModelType {
    LOCAL("local"), DROPBOX("dropbox");

    private String type = "";

    private ModelType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public static ModelType fromString(String type) {
        if (type.equals("local"))
            return LOCAL;
        else if (type.equals("dropbox"))
            return DROPBOX;
        return null;
    }
}
