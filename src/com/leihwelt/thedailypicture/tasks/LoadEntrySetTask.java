package com.leihwelt.thedailypicture.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.leihwelt.thedailypicture.BUS;
import com.leihwelt.thedailypicture.messages.EntrySetLoadedEvent;
import com.leihwelt.thedailypicture.model.DataModel;
import com.leihwelt.thedailypicture.model.EntrySet;

public class LoadEntrySetTask extends AsyncTask<Void, Void, EntrySet> {

    private DataModel model;
    private String name;

    public LoadEntrySetTask(String name, DataModel model) {
        this.model = model;
        this.name = name;
    }

    @Override
    protected EntrySet doInBackground(Void... params) {

        EntrySet set;

        set = this.model.loadEntrySet();

        if (set == null) {
            set = new EntrySet();
            set.setName(name);

            this.model.storeEntrySet(set);
        }

        model.validateSet(set);

        return set;
    }

    @Override
    protected void onPostExecute(EntrySet result) {

        if (result != null) {

            Log.d("LoadEntrySetTask", "posting EntrySetLoadedEvent " + result.getEntries());
            BUS.INSTANCE.post(new EntrySetLoadedEvent(result));
        }

    }

}
