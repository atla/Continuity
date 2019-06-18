package com.leihwelt.thedailypicture.tasks;

import android.os.AsyncTask;

import com.leihwelt.thedailypicture.model.DataModel;
import com.leihwelt.thedailypicture.model.EntrySet;

public class StoreEntrySetTask extends AsyncTask<Void, Void, Void> {

    private EntrySet set;
    private DataModel model;

    public StoreEntrySetTask(DataModel model, EntrySet set) {
        this.model = model;
        this.set = set;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        model.storeEntrySet(set);

        return null;
    }

}
