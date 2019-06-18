package com.leihwelt.thedailypicture.fragments;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.leihwelt.thedailypicture.App;
import com.leihwelt.thedailypicture.BUS;
import com.leihwelt.thedailypicture.R;
import com.leihwelt.thedailypicture.messages.EntrySetLoadedEvent;
import com.leihwelt.thedailypicture.messages.ShowDiaryEntryEvent;
import com.leihwelt.thedailypicture.model.DataModel;
import com.leihwelt.thedailypicture.model.DataModelBuilder;
import com.leihwelt.thedailypicture.model.Diary;
import com.leihwelt.thedailypicture.model.Entry;
import com.leihwelt.thedailypicture.model.EntrySet;
import com.leihwelt.thedailypicture.model.ModelType;
import com.leihwelt.thedailypicture.picturetake.PictureTakeHelper;
import com.squareup.otto.Subscribe;

import java.io.File;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.CacheableBitmapDrawable;

public class PastDaysFragmenet extends Fragment implements OnClickListener {

    public static final String TAG = PastDaysFragmenet.class.getSimpleName();
    private View view;
    private EntrySet entrySet;
    private Entry[] entries;
    private ImageView day1;
    private ImageView day2;
    private ImageView day3;

    private class PastDaysItemLoadedEvent {
        public int pos;
        private CacheableBitmapDrawable drawable;

        public PastDaysItemLoadedEvent(int pos, CacheableBitmapDrawable drawable) {
            this.pos = pos;
            this.drawable = drawable;
        }
    }

    private class LoadPastDaysTask extends AsyncTask<Void, PastDaysItemLoadedEvent, Void> {

        private static final int PICTURE_SIZE = 240;
        private BitmapLruCache mCache;
        private Entry[] entries;
        private PictureTakeHelper helper;
        private DataModel model;

        public LoadPastDaysTask(Context ctx, Entry[] entries, DataModel model) {
            this.mCache = App.getInstance(ctx).getBitmapCache();
            this.entries = entries;
            this.model = model;
            helper = new PictureTakeHelper();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            if (entries != null) {
                int i = 0;
                for (Entry entry : entries) {
                    CacheableBitmapDrawable wrapper = mCache.get(entry.getPicture());
                    if (wrapper == null && mCache != null) {

                        if (entry.getPicture() != null) {

                            File file = model.getPictureFileForDate(entry.getDate());

                            if (file != null) {

                                PictureTakeHelper helper = new PictureTakeHelper();
                                Bitmap bitmap = helper.loadPicture(file.getAbsolutePath(), entry, PICTURE_SIZE, PICTURE_SIZE);

                                if (bitmap != null) {
                                    wrapper = mCache.put(entry.getPicture(), bitmap);
                                }
                            }

                        }

                    }

                    publishProgress(new PastDaysItemLoadedEvent(i, wrapper));

                    ++i;
                }
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(PastDaysItemLoadedEvent... values) {
            super.onProgressUpdate(values);

            BUS.INSTANCE.post(values[0]);

        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }

    }

    @Override
    public void onResume() {
        super.onResume();

        BUS.INSTANCE.get().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();

        BUS.INSTANCE.get().unregister(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.view = inflater.inflate(R.layout.past_days_fragment, null);

        this.day1 = (ImageView) this.view.findViewById(R.id.day1);
        this.day2 = (ImageView) this.view.findViewById(R.id.day2);
        this.day3 = (ImageView) this.view.findViewById(R.id.day3);

        this.day1.setOnClickListener(this);
        this.day2.setOnClickListener(this);
        this.day3.setOnClickListener(this);

        this.day1.setVisibility(View.GONE);
        this.day2.setVisibility(View.GONE);
        this.day3.setVisibility(View.GONE);

        return view;

    }

    @Subscribe
    public void onPastDaysItemLoaded(PastDaysItemLoadedEvent item) {

        ImageView view = getImageView(item.pos);

        if (view != null && item.drawable != null) {

            view.setVisibility(View.VISIBLE);

            BitmapDrawable emptyDrawable = new BitmapDrawable(this.getResources());

            TransitionDrawable fadeInDrawable = new TransitionDrawable(new Drawable[]{emptyDrawable, item.drawable});
            view.setImageDrawable(fadeInDrawable);
            fadeInDrawable.startTransition(400);
        }

    }

    private ImageView getImageView(int pos) {

        switch (pos) {
            case 0:
                return day1;
            case 1:
                return day2;
            case 2:
                return day3;
        }

        return null;
    }

    @Subscribe
    public void onEntrySetLoaded(EntrySetLoadedEvent event) {

        hideAll();

        this.entrySet = event.getEntrySet();

        Diary diary = App.getInstance(this.getActivity()).getCurrentDiary();

        DataModel model = DataModelBuilder.build(diary.getLabel(), ModelType.fromString(diary.getType()), this.getActivity());

        this.entries = model.getLatestThreeDays(entrySet);

        (new LoadPastDaysTask(getActivity(), entries, model)).execute();

    }

    private void hideAll() {
        getImageView(0).setVisibility(View.GONE);
        getImageView(1).setVisibility(View.GONE);
        getImageView(2).setVisibility(View.GONE);
    }

    @Override
    public void onClick(View v) {

        if (v == day1) {
            showEntry(0);
        } else if (v == day2) {
            showEntry(1);
        } else if (v == day3) {
            showEntry(2);
        }

    }

    private void showEntry(int i) {
        if (entries != null && entries.length > i) {
            Entry entry = entries[i];
            BUS.INSTANCE.post(new ShowDiaryEntryEvent(entry));
        }

    }

}
