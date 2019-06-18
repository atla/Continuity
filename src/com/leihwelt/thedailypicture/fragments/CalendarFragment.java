package com.leihwelt.thedailypicture.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.leihwelt.thedailypicture.BUS;
import com.leihwelt.thedailypicture.R;
import com.leihwelt.thedailypicture.R.id;
import com.leihwelt.thedailypicture.R.layout;
import com.leihwelt.thedailypicture.messages.DateSelectedEvent;
import com.leihwelt.thedailypicture.messages.SetToDiaryEvent;
import com.leihwelt.thedailypicture.messages.ShowTodayPictureEvent;
import com.squareup.otto.Subscribe;
import com.squareup.timessquare.CalendarPickerView;
import com.squareup.timessquare.CalendarPickerView.DateChangedListener;

import org.joda.time.DateTime;

import java.util.Date;

import butterknife.InjectView;
import butterknife.Views;

public class CalendarFragment extends Fragment implements DateChangedListener {

    private static final String TAG = CalendarFragment.class.getSimpleName();
    @InjectView(R.id.calendar_view)
    CalendarPickerView calendar;

    private DateTime lastStartDate = DateTime.now();
    private DateTime lastSelectedDate = DateTime.now();

    public CalendarFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.calendar_fragment, null);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Views.inject(this, view);

        setupCalendar(lastStartDate);
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

    private void setupCalendar(DateTime start) {

        Date today = new Date();

        calendar.init(lastSelectedDate.toDate(), start.toDate(), today);
        calendar.setDateChangeListener(this);
        // calendar.setSelection(calendar.getAdapter().getCount() - 1);
    }

    @Subscribe
    public void onSetToDiary(SetToDiaryEvent event) {

        Log.d(TAG, "onSetToDiary called, setup calendar with start date " + event.getDiary().getDateString());

        this.lastStartDate = event.getDiary().getDate();

        setupCalendar(lastStartDate);

    }

    private void postSelectedDate(DateTime date) {
        if (calendar != null) {
            BUS.INSTANCE.post(new DateSelectedEvent(date));
            BUS.INSTANCE.post(new ShowTodayPictureEvent());
        }
    }

    @Override
    public void onDateChanged(Date date) {

        lastSelectedDate = new DateTime(date);

        postSelectedDate(new DateTime(date));
    }

}
