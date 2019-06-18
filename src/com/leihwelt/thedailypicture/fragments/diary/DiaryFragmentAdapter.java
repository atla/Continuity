package com.leihwelt.thedailypicture.fragments.diary;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.leihwelt.thedailypicture.R;
import com.leihwelt.thedailypicture.fragments.diary.DiaryFragment.DiaryMode;
import com.leihwelt.thedailypicture.model.Entry;
import com.leihwelt.thedailypicture.model.EntryComparator;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import butterknife.InjectView;
import butterknife.Views;

public class DiaryFragmentAdapter extends BaseAdapter {

    private final static DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);

    class ViewHolder {
        @InjectView(R.id.description_text)
        TextView description;
        @InjectView(R.id.long_text)
        TextView longText;
        @InjectView(R.id.picture_view)
        ImageView picture;
        @InjectView(R.id.date_label)
        TextView dateText;

        public ViewHolder(View view) {
            Views.inject(this, view);
        }
    }

    private ArrayList<Entry> data = null;
    private LayoutInflater inflater;

    private int layout;

    public DiaryFragmentAdapter(Collection<Entry> values, DiaryMode mode, LayoutInflater inflater) {
        this.data = new ArrayList<Entry>(values);

        if (mode == DiaryMode.LIST)
            layout = R.layout.diary_list_entry;
        else if (mode == DiaryMode.GRID)
            layout = R.layout.diary_grid_entry;

        Collections.sort(data, new EntryComparator());

        this.inflater = inflater;
    }

    @Override
    public int getCount() {

        if (data != null) {
            return data.size();
        }

        return 0;
    }

    @Override
    public Object getItem(int index) {

        if (data != null) {
            return data.get(index);
        }

        return null;
    }

    @Override
    public long getItemId(int index) {
        return index;
    }

    @Override
    public View getView(int index, View convertView, ViewGroup parent) {

        ViewHolder holder = null;

        if (convertView == null) {
            convertView = inflater.inflate(layout, null);
            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        Entry item = (Entry) this.getItem(index);

        if (item != null) {
            setText(holder.description, item.getDescription(), "No Description");
            setText(holder.longText, item.getLongText(), "...");
            setText(holder.dateText, DATE_FORMAT.format(item.getDate().toDate()), "");
        }

        holder.picture.setImageDrawable(null);
        holder.picture.setBackgroundResource(R.drawable.no_image_back);

        return convertView;
    }

    private void setText(TextView textView, String text, String defaultText) {

        if (textView == null)
            return;

        if (text != null)
            textView.setText(text);
        else
            textView.setText(defaultText);
    }

}
