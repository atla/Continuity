package com.leihwelt.thedailypicture.fragments;

import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import com.leihwelt.android.helpers.DateTimeHelpers;
import com.leihwelt.thedailypicture.BUS;
import com.leihwelt.thedailypicture.R;
import com.leihwelt.thedailypicture.messages.DiaryCreatedEvent;
import com.leihwelt.thedailypicture.model.Diary;
import com.leihwelt.thedailypicture.model.ModelType;
import com.squareup.timessquare.CalendarPickerView;

import org.joda.time.DateTime;

import java.text.DateFormat;

public class CreateDiaryFragment extends Fragment implements OnClickListener, OnDateSetListener {

    private static final String DATE_PICKER = "datePicker";
    private final static DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.FULL);

    private CalendarPickerView calendar;
    private Button changeDiaryStartDateButton;
    private EditText diaryEditText;

    private DateTime date = DateTime.now();
    private TextView dateLabel;
    private Button okButton;
    private Button cancelButton;

    public static class DatePickerFragment extends DialogFragment {

        private OnDateSetListener listener;
        private DateTime date;

        public DatePickerFragment(DatePickerDialog.OnDateSetListener listener, DateTime date) {
            this.listener = listener;
            this.date = date;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int year = date.getYear();
            int month = date.getMonthOfYear() - 1;
            int day = date.getDayOfMonth();
            return new DatePickerDialog(getActivity(), listener, year, month, day);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.create_new_diary_fragment, null);

        return v;

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        changeDiaryStartDateButton = (Button) view.findViewById(R.id.change_date_button);
        changeDiaryStartDateButton.setOnClickListener(this);
        diaryEditText = (EditText) view.findViewById(R.id.diary_name_edit);

        dateLabel = (TextView) view.findViewById(R.id.date_label);

        okButton = (Button) view.findViewById(R.id.ok_button);
        cancelButton = (Button) view.findViewById(R.id.cancel_button);

        okButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        if (v == changeDiaryStartDateButton) {
            DialogFragment newFragment = new DatePickerFragment(this, date);
            newFragment.show(this.getFragmentManager(), DATE_PICKER);
        } else if (v == okButton) {
            String text = diaryEditText.getText().toString();

            if (text != null && text.length() > 0) {
                onCreateDiary();
            }

        } else if (v == cancelButton) {
            this.getActivity().finish();
        }

    }


    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {

        DateTime newDate = new DateTime(year, monthOfYear + 1, dayOfMonth, date.hourOfDay().get(), date.minuteOfHour().get());

        if (newDate.isBefore(DateTime.now())) {
            date = newDate;
            updateText();
        }
    }

    private void updateText() {
        String dateText = getTodayTitle();

        String text = "Start with the diary on the following day: " + dateText;

        dateLabel.setText(text);
    }

    private void onCreateDiary() {

        Diary d = new Diary();
        d.setLabel(diaryEditText.getText().toString());
        d.setDate(this.date);
        d.setType(ModelType.DROPBOX.getType());

        BUS.INSTANCE.post(new DiaryCreatedEvent(d));

        this.getActivity().finish();
    }

    private String getTodayTitle() {

        if (DateTimeHelpers.isToday(this.date)) {
            return "Today";
        }
        return DATE_FORMAT.format(this.date.toDate());
    }

}
