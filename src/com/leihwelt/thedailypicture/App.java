
package com.leihwelt.thedailypicture;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.dropbox.sync.android.DbxAccountManager;
import com.leihwelt.thedailypicture.messages.DeleteDiaryEvent;
import com.leihwelt.thedailypicture.messages.DiaryCreatedEvent;
import com.leihwelt.thedailypicture.messages.DiaryListLoadedEvent;
import com.leihwelt.thedailypicture.messages.DiarySelectedEvent;
import com.leihwelt.thedailypicture.messages.PictureInvalidatedEvent;
import com.leihwelt.thedailypicture.messages.SetDropboxAccount;
import com.leihwelt.thedailypicture.messages.SetToDiaryEvent;
import com.leihwelt.thedailypicture.messages.ShowDiaryEntryEvent;
import com.leihwelt.thedailypicture.model.Diary;
import com.leihwelt.thedailypicture.model.DiaryList;
import com.leihwelt.thedailypicture.tasks.LoadDiaryList;
import com.leihwelt.thedailypicture.tasks.RemoveEntryFromCacheTask;
import com.leihwelt.thedailypicture.tasks.StoreDiaryList;
import com.squareup.otto.Produce;
import com.squareup.otto.Subscribe;

import java.io.File;

import uk.co.senab.bitmapcache.BitmapLruCache;
import uk.co.senab.bitmapcache.BitmapLruCache.RecyclePolicy;

public class App extends Application {

    // dropbox data
    public static final String APP_KEY = "";
    public static final String APP_SECRET = "";

    private static final String THEDAILYPICTURE = "thedailypicture";
    private static final String TAG = App.class.getSimpleName();
    private BitmapLruCache cache;
    private ShowDiaryEntryEvent showDiaryEvent;
    private DiaryList list;

    private Diary currentDiary;
    private String lastSelectedDiary;

    private SetToDiaryEvent setToDiaryEvent = null;

    private DbxAccountManager accountManager = null;

    @Override
    public void onCreate() {
        super.onCreate();

        BUS.INSTANCE.get().register(this);

        accountManager = DbxAccountManager.getInstance(getApplicationContext(), APP_KEY, APP_SECRET);

        lastSelectedDiary = Preferences.INSTANCE.getLastSelectedDiary(this);

        recreateCache();

        (new LoadDiaryList()).execute();

    }

    public DiaryList getDiaryList() {
        return list;
    }

    public Diary getCurrentDiary() {
        return currentDiary;
    }

    private void recreateCache() {

        File cacheDir = new File(getCacheDir(), THEDAILYPICTURE);
        cacheDir.mkdirs();

        BitmapLruCache.Builder builder = setupCache(cacheDir);

        cache = builder.build();
    }

    private BitmapLruCache.Builder setupCache(File cacheDir) {
        BitmapLruCache.Builder builder = new BitmapLruCache.Builder();
        builder.setMemoryCacheEnabled(true).setMemoryCacheMaxSizeUsingHeapSize();
        builder.setRecyclePolicy(RecyclePolicy.DISABLED);
        builder.setDiskCacheEnabled(true).setDiskCacheLocation(cacheDir);
        return builder;
    }

    public BitmapLruCache getBitmapCache() {
        return cache;
    }

    public static App getInstance(Context context) {
        return (App) context.getApplicationContext();
    }

    @Subscribe
    public void onDiaryCreated(DiaryCreatedEvent event) {

        this.currentDiary = event.getDiary();
        this.lastSelectedDiary = this.currentDiary.getLabel();
        Preferences.INSTANCE.setLastSelectedDiary(lastSelectedDiary, this);

        this.setToDiaryEvent = new SetToDiaryEvent(event.getDiary());

        if (list != null) {
            list.add(event.getDiary());
            (new StoreDiaryList(list)).execute();
        }

    }

    @Produce
    public SetToDiaryEvent produceSetToDiaryEvent() {
        SetToDiaryEvent tmp = this.setToDiaryEvent;
        // this.setToDiaryEvent = null;
        return tmp;
    }

    @Subscribe
    public void onDeleteDiary(DeleteDiaryEvent event) {

        this.currentDiary = null;
        this.lastSelectedDiary = null;

        if (list != null) {
            this.list.remove(event.getDiary());
            (new StoreDiaryList(list)).execute();

            BUS.INSTANCE.post(new DiaryListLoadedEvent(list));
        }

    }

    @Subscribe
    public void onDiarySelectedEvent(DiarySelectedEvent event) {

        this.currentDiary = event.getDiary();
        this.lastSelectedDiary = this.currentDiary.getLabel();

        this.recreateCache();

        Preferences.INSTANCE.setLastSelectedDiary(lastSelectedDiary, this);

        BUS.INSTANCE.post(new SetToDiaryEvent(currentDiary));


        Log.d(TAG, "Diary Selected: " + this.currentDiary.getLabel());
    }

    @Subscribe
    public void onDiaryListLoaded(DiaryListLoadedEvent event) {

        Log.d(TAG, "onDiaryListLoaded");

        this.list = event.getDiaryList();
        this.currentDiary = this.list.get(this.lastSelectedDiary);

        BUS.INSTANCE.post(new SetToDiaryEvent(currentDiary));

    }

    @Subscribe
    public void onShowDiaryEntry(ShowDiaryEntryEvent showDiaryEvent) {
        this.showDiaryEvent = showDiaryEvent;
    }

    @Produce
    public ShowDiaryEntryEvent produceShowDiaryEntryEvent() {

        if (this.showDiaryEvent == null)
            return null;

        ShowDiaryEntryEvent event = this.showDiaryEvent;
        this.showDiaryEvent = null;
        return event;
    }

    @Subscribe
    public void onPictureInvalidated(PictureInvalidatedEvent event) {

        String picture = event.getEntry().getDateStringValue();

        (new RemoveEntryFromCacheTask(cache, picture)).execute();
    }

    @Subscribe
    public void onSetDropboxAccount(SetDropboxAccount event) {
        this.accountManager = event.getmDbxAcctMgr();
    }

    public DbxAccountManager getAccountManager() {
        return accountManager;
    }

}
