package com.leihwelt.android.helpers;

import android.os.AsyncTask;

public class ExecuteAfterUITask extends AsyncTask<Void, Void, Void> {

    public static interface ExecuteAfterUIListener {
        public void onAfterUI();
    }

    private ExecuteAfterUIListener listener;

    public ExecuteAfterUITask(ExecuteAfterUIListener listener) {
        this.listener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected Void doInBackground(Void... arg0) {
        return null;
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        listener.onAfterUI();
    }
}