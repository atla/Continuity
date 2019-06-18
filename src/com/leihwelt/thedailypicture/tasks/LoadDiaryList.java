package com.leihwelt.thedailypicture.tasks;

import android.os.AsyncTask;

import com.leihwelt.thedailypicture.BUS;
import com.leihwelt.thedailypicture.messages.DiaryListLoadedEvent;
import com.leihwelt.thedailypicture.model.DiaryList;
import com.leihwelt.thedailypicture.model.WrongLocalDataModel;

public class LoadDiaryList extends AsyncTask<Void, Void, DiaryList> {

    @Override
    protected DiaryList doInBackground(Void... arg0) {

        DiaryList list = WrongLocalDataModel.loadDiaryList();

        if (list == null) {
            list = new DiaryList();

            (new StoreDiaryList(list)).execute();
        }

        return list;
    }

    @Override
    protected void onPostExecute(DiaryList result) {
        super.onPostExecute(result);

        if (result != null) {
            BUS.INSTANCE.post(new DiaryListLoadedEvent(result));
        }
    }

}
