package com.leihwelt.thedailypicture;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.leihwelt.thedailypicture.fragments.diary.DiaryFragment.DiaryMode;

public enum Preferences {
    INSTANCE;

    private static final String DIARY_MODE = "DIARY_MODE";
    private static final String TDP_PREFS = "WIDGET_DESIGNER_PREFS";
    private static final String LAST_SELECTED_DIARY = "LAST_SELECTED_DIARY";

    private Editor edit(final Context ctx) {
        return prefs(ctx).edit();
    }

    private SharedPreferences prefs(final Context ctx) {
        return ctx.getSharedPreferences(TDP_PREFS, 0);
    }

    // SETTINGS

    public void setDiaryMode(DiaryMode mode, final Context ctx) {
        edit(ctx).putInt(DIARY_MODE, mode.getValue()).commit();
    }

    // true = list, false = grid
    public int getDiaryMode(Context ctx) {
        return prefs(ctx).getInt(DIARY_MODE, 1);
    }

    public void setLastSelectedDiary(String diary, final Context ctx) {
        edit(ctx).putString(LAST_SELECTED_DIARY, diary).commit();
    }

    public String getLastSelectedDiary(final Context ctx) {
        return prefs(ctx).getString(LAST_SELECTED_DIARY, null);
    }
}
