package com.leihwelt.thedailypicture.fragments.diary;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.GridView;
import android.widget.ListView;

import com.leihwelt.thedailypicture.App;
import com.leihwelt.thedailypicture.BUS;
import com.leihwelt.thedailypicture.Preferences;
import com.leihwelt.thedailypicture.R;
import com.leihwelt.thedailypicture.messages.DateSelectedEvent;
import com.leihwelt.thedailypicture.messages.EntrySetLoadedEvent;
import com.leihwelt.thedailypicture.messages.RefreshDiaryEvent;
import com.leihwelt.thedailypicture.messages.ShowDiaryEntryEvent;
import com.leihwelt.thedailypicture.messages.ShowTodayPictureEvent;
import com.leihwelt.thedailypicture.model.DataModel;
import com.leihwelt.thedailypicture.model.DataModelBuilder;
import com.leihwelt.thedailypicture.model.Diary;
import com.leihwelt.thedailypicture.model.Entry;
import com.leihwelt.thedailypicture.model.EntrySet;
import com.leihwelt.thedailypicture.model.ModelType;
import com.squareup.otto.Subscribe;

import org.lucasr.smoothie.AsyncAbsListView;
import org.lucasr.smoothie.ItemManager;

import butterknife.InjectView;
import butterknife.Views;
import uk.co.senab.bitmapcache.BitmapLruCache;

public class DiaryFragment extends Fragment implements OnItemClickListener, OnItemLongClickListener {

    public static enum DiaryMode {
        LIST(1), GRID(2);

        private int value;

        private DiaryMode(int v) {
            this.value = v;
        }

        public int getValue() {
            return this.value;
        }

        public static DiaryMode fromValue(int value) {
            switch (value) {
                case 1:
                    return LIST;
                case 2:
                    return GRID;
            }
            return LIST;
        }

        public DiaryMode invert() {

            if (this == LIST)
                return GRID;

            return LIST;
        }
    }

    @InjectView(R.id.list_container)
    ViewGroup listContainer;

    private EntrySet entrySet;
    private AbsListView asyncListView;
    private DiaryFragmentAdapter adapter;

    private MenuItem item;
    private ItemManager itemManager;

    private DataModel model;

    public DiaryFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        setHasOptionsMenu(true);

        return inflater.inflate(R.layout.diary_fragment, null);
    }

    private void setDiaryItemIcon() {

        if (item == null)
            return;

        DiaryMode mode = DiaryMode.fromValue(Preferences.INSTANCE.getDiaryMode(this.getActivity()));
        if (mode == DiaryMode.LIST)
            item.setIcon(R.drawable.ic_action_tiles_small);
        else if (mode == DiaryMode.GRID)
            item.setIcon(R.drawable.ic_action_list);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.diary_menu, menu);

        item = menu.findItem(R.id.menu_switch_diary);

        setDiaryItemIcon();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.menu_switch_diary) {
            DiaryMode current = DiaryMode.fromValue(Preferences.INSTANCE.getDiaryMode(this.getActivity()));
            current = current.invert();
            Preferences.INSTANCE.setDiaryMode(current, getActivity());

            setDiaryMode(current);
        }

        return super.onOptionsItemSelected(item);
    }

    private void setDiaryMode(DiaryMode current) {

        LayoutInflater inflater = this.getActivity().getLayoutInflater();
        int layout = current == DiaryMode.LIST ? R.layout.diary_list : R.layout.diary_grid;
        AbsListView newListView = (AbsListView) inflater.inflate(layout, null);

        if (newListView instanceof AsyncAbsListView) {
            BitmapLruCache cache = App.getInstance(this.getActivity()).getBitmapCache();
            DiaryListLoader loader = new DiaryListLoader(cache, this.model);
            ItemManager.Builder builder = new ItemManager.Builder(loader);
            builder.setPreloadItemsEnabled(true).setPreloadItemsCount(5);
            builder.setThreadPoolSize(4);

            itemManager = builder.build();

            ((AsyncAbsListView) newListView).setItemManager(itemManager);
        }

        if (newListView instanceof ListView) {
            ((ListView) newListView).setDivider(null);
        }

        this.asyncListView = newListView;
        this.listContainer.removeAllViews();

        this.asyncListView = newListView;
        this.asyncListView.setOnItemClickListener(this);
        this.asyncListView.setOnItemLongClickListener(this);
        this.listContainer.addView(asyncListView);

        setDiaryItemIcon();

        resetAdapter();

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Views.inject(this, view);

        DiaryMode mode = DiaryMode.fromValue(Preferences.INSTANCE.getDiaryMode(this.getActivity()));
        setDiaryMode(mode);

    }

    @Override
    public void onResume() {
        super.onResume();

        BUS.INSTANCE.get().register(this);

        Diary diary = App.getInstance(getActivity()).getCurrentDiary();

        if (diary != null) {
            this.model = DataModelBuilder.build(diary.getLabel(), ModelType.fromString(diary.getType()), this.getActivity());
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        BUS.INSTANCE.get().unregister(this);
    }

    @Subscribe
    public void onRefreshDiary(RefreshDiaryEvent event) {
        if (this.adapter != null)
            this.adapter.notifyDataSetChanged();
    }

    @Subscribe
    public void onEntrySetLoaded(EntrySetLoadedEvent entrySetLoaded) {
        this.entrySet = entrySetLoaded.getEntrySet();

        Diary diary = App.getInstance(getActivity()).getCurrentDiary();

        if (diary != null) {
            this.model = DataModelBuilder.build(diary.getLabel(), ModelType.fromString(diary.getType()), this.getActivity());
        }

        resetAdapter();
    }

    private void resetAdapter() {

        if (entrySet == null)
            return;

        DiaryMode mode = DiaryMode.fromValue(Preferences.INSTANCE.getDiaryMode(this.getActivity()));
        this.adapter = new DiaryFragmentAdapter(entrySet.getEntries().values(), mode, this.getActivity().getLayoutInflater());
        setAdapterToAbsListView();
    }

    private void setAdapterToAbsListView() {
        if (asyncListView instanceof ListView)
            ((ListView) this.asyncListView).setAdapter(adapter);
        else if (asyncListView instanceof GridView)
            ((GridView) this.asyncListView).setAdapter(adapter);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int pos, long id) {
        Entry entry = (Entry) this.adapter.getItem(pos);
        BUS.INSTANCE.post(new ShowDiaryEntryEvent(entry));
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> adapterView, View v, int pos, long id) {

        Entry entry = (Entry) this.adapter.getItem(pos);
        BUS.INSTANCE.post(new DateSelectedEvent(entry.getDate()));
        BUS.INSTANCE.post(new ShowTodayPictureEvent());

        return true;
    }

}
