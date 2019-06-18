package com.leihwelt.thedailypicture.tasks;

import android.os.AsyncTask;

import com.leihwelt.thedailypicture.model.DiaryList;
import com.leihwelt.thedailypicture.model.WrongLocalDataModel;

public class StoreDiaryList extends AsyncTask<Void, Void, Void> {

    private DiaryList list;

    public StoreDiaryList(DiaryList list) {
        this.list = list;
    }

    @Override
    protected Void doInBackground(Void... params) {

        WrongLocalDataModel.storeDiaryList(list);

        return null;
    }

}
