package com.leihwelt.thedailypicture.tasks;

import android.os.AsyncTask;

import com.leihwelt.thedailypicture.BUS;
import com.leihwelt.thedailypicture.messages.RefreshDiaryEvent;

import uk.co.senab.bitmapcache.BitmapLruCache;

public class RemoveEntryFromCacheTask extends AsyncTask<Void, Void, Void> {

    private String entry;
    private BitmapLruCache cache;

    public RemoveEntryFromCacheTask(BitmapLruCache cache, String entry) {
        this.cache = cache;
        this.entry = entry;
    }

    @Override
    protected Void doInBackground(Void... arg0) {

        if (entry != null && cache != null && this.cache.contains(entry)) {
            this.cache.remove(entry);
        }

        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        BUS.INSTANCE.post(new RefreshDiaryEvent());
    }

}
