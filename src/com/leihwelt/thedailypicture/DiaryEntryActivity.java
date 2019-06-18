package com.leihwelt.thedailypicture;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;

public class DiaryEntryActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_diary_entry);

        // remove background from main window to reduce GPU overdraw behind our
        // background
        getWindow().setBackgroundDrawable(null);

        ActionBar bar = this.getActionBar();

        if (bar != null) {
            bar.setHomeButtonEnabled(true);
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setLogo(R.drawable.ic_launcher);
            bar.setTitle("");
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            this.finish();
        }

        return super.onMenuItemSelected(featureId, item);
    }

}
