package com.leihwelt.thedailypicture.fragments;

import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.leihwelt.thedailypicture.App;
import com.leihwelt.thedailypicture.BUS;
import com.leihwelt.thedailypicture.R;
import com.leihwelt.thedailypicture.messages.DiarySelectedEvent;
import com.leihwelt.thedailypicture.model.Diary;
import com.leihwelt.thedailypicture.model.DiaryList;

import java.util.ArrayList;

import butterknife.InjectView;
import butterknife.Views;

public class DiarySelectionFragment extends DialogFragment implements OnItemClickListener {

    private class DiaryListAdapter extends BaseAdapter {

        private class ViewHolder {
            public TextView label;
        }

        private ArrayList<Diary> diaryList;
        private LayoutInflater inflater;

        public DiaryListAdapter(ArrayList<Diary> diaryList, LayoutInflater inflater) {
            this.diaryList = diaryList;
            this.inflater = inflater;
        }

        @Override
        public int getCount() {

            if (this.diaryList == null)
                return 0;

            return this.diaryList.size();
        }

        @Override
        public Object getItem(int index) {

            if (this.diaryList == null)
                return null;

            return this.diaryList.get(index);
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }

        @Override
        public View getView(int pos, View view, ViewGroup parent) {

            ViewHolder holder = null;

            if (view == null) {
                view = inflater.inflate(R.layout.diary_list_item, null);
                holder = new ViewHolder();
                holder.label = (TextView) view.findViewById(R.id.label);
                view.setTag(holder);
            } else {
                holder = (ViewHolder) view.getTag();
            }

            Diary d = (Diary) getItem(pos);
            holder.label.setText(d.getLabel());

            return view;
        }
    }

    private static final String TAG = DiarySelectionFragment.class.getSimpleName();

    private DiaryList diaryList;
    private DiaryListAdapter adapter;

    @InjectView(R.id.list)
    ListView listView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.getDialog().setTitle(R.string.choose_diary_title);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.thedailytheme_solid_ActionBar);
        return inflater.inflate(R.layout.diary_list_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Views.inject(this, view);

        this.diaryList = App.getInstance(getActivity()).getDiaryList();
        this.adapter = new DiaryListAdapter(new ArrayList<Diary>(diaryList), this.getActivity().getLayoutInflater());

        this.listView.setAdapter(adapter);
        this.listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Log.d(TAG, "On List Item Clicked: " + position);

        Diary selectedDiary = (Diary) adapterView.getAdapter().getItem(position);

        BUS.INSTANCE.post(new DiarySelectedEvent(selectedDiary));

        this.dismiss();
    }

}
