package com.leihwelt.thedailypicture.fragments;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.leihwelt.android.helpers.DateTimeHelpers;
import com.leihwelt.android.helpers.ExecuteAfterUITask;
import com.leihwelt.android.helpers.ExecuteAfterUITask.ExecuteAfterUIListener;
import com.leihwelt.thedailypicture.App;
import com.leihwelt.thedailypicture.BUS;
import com.leihwelt.thedailypicture.R;
import com.leihwelt.thedailypicture.messages.DateSelectedEvent;
import com.leihwelt.thedailypicture.messages.EntrySetLoadedEvent;
import com.leihwelt.thedailypicture.messages.NoPictureForDateEvent;
import com.leihwelt.thedailypicture.messages.PictureFinishedLoadingMessage;
import com.leihwelt.thedailypicture.messages.PictureInvalidatedEvent;
import com.leihwelt.thedailypicture.messages.PictureStartLoadingMessage;
import com.leihwelt.thedailypicture.messages.RefreshDiaryEvent;
import com.leihwelt.thedailypicture.messages.SetToDiaryEvent;
import com.leihwelt.thedailypicture.messages.TodayPictureLoadedMessage;
import com.leihwelt.thedailypicture.model.DataModel;
import com.leihwelt.thedailypicture.model.DataModelBuilder;
import com.leihwelt.thedailypicture.model.Diary;
import com.leihwelt.thedailypicture.model.Entry;
import com.leihwelt.thedailypicture.model.EntrySet;
import com.leihwelt.thedailypicture.model.ModelType;
import com.leihwelt.thedailypicture.picturetake.PictureTakeHelper;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.io.File;
import java.text.DateFormat;

import butterknife.InjectView;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

@SuppressLint("NewApi")
public class TodayPictureFragment extends Fragment implements OnClickListener, OnEditorActionListener, ExecuteAfterUIListener,
        OnGestureListener, OnTouchListener, TextWatcher {

    private final static String TAG = TodayPictureFragment.class.getSimpleName();
    private final static DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.FULL);
    public final static int NO_PICTURE_DRAWABLE = R.drawable.ic_action_emo_angry;

    private DateTime currentDate = DateTime.now();
    private boolean hasPicture = false;
    private EntrySet entrySet;

    private PictureTakeHelper pictureTakeHelper = new PictureTakeHelper();

    private View baseView;

    // @InjectView(R.id.no_picture_taken)
    ImageView pictureView;

    private TextView pictureViewLabel;
    private TextView pictureDescriptionLabel;
    private EditText pictureTextEdit;
    private ProgressBar pictureLoadingProgressbar;
    private View diaryLayout;
    private EditText diaryEdit;
    private TextView diaryInfo;
    @InjectView(R.id.picture_wrapper)
    ViewGroup pictureWrapper;

    private boolean animatingDiary = false;
    private boolean diaryShown = false;
    private ImageButton diaryButton;
    private float yold;

    private GestureDetector gestureDetector;

    private Entry currentEntry;
    private DataModel model;

    private TodayPictureState state = TodayPictureState.NO_PICTURE_TAKEN;

    private enum TodayPictureState {
        NO_PICTURE_TAKEN, NO_DESCRIPTION, PICTURE_DESCRIPTION_LOADED
    }

    private final static class LoadPictureForDateTask extends AsyncTask<Void, Void, Bitmap> {

        private int height;
        private int width;
        private String diary;
        private DataModel model;
        private Context ctx;
        private Entry entry;

        public LoadPictureForDateTask(String diary, Entry entry, DataModel model, int width, int height) {
            this.width = width;
            this.height = height;
            this.entry = entry;
            this.diary = diary;
            this.model = model;

            Log.d(TAG, "loading picture with " + width + "x" + height);
        }

        @Override
        protected Bitmap doInBackground(Void... voids) {

            Bitmap bitmap = null;

            if (model.hasPictureForDate(entry.getDate())) {

                Log.d(TAG, "Found picture for that day, loading...");

                File file = model.getPictureFileForDate(entry.getDate());

                if (file != null) {
                    PictureTakeHelper helper = new PictureTakeHelper();
                    bitmap = helper.loadPicture(file.getAbsolutePath(), entry, width, height);

                }
            }

            return bitmap;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            BUS.INSTANCE.get().post(new PictureStartLoadingMessage());
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            BUS.INSTANCE.get().post(new PictureFinishedLoadingMessage(result != null));

            if (result != null) {
                BUS.INSTANCE.post(new TodayPictureLoadedMessage(result));
            } else {
                BUS.INSTANCE.post(new NoPictureForDateEvent(entry.getDate()));
            }

        }

    }

    private final static class StorePictureForDateTask extends AsyncTask<Void, Void, Void> {

        private final String picture;
        private final Activity activity;
        private DateTime date;
        private Entry entry;
        private String diary;
        private DataModel model;

        public StorePictureForDateTask(String diary, DateTime date, String picture, Activity activity, Entry entry, DataModel model) {
            this.picture = picture;
            this.activity = activity;
            this.date = date;
            this.entry = entry;
            this.diary = diary;
            this.model = model;
        }

        @Override
        protected Void doInBackground(Void... voids) {

            String pictureFileName = model.savePictureForDate(picture, date);
            entry.setPicture(pictureFileName);

            return null;
        }

        @Override
        protected void onPostExecute(Void none) {

            boolean success = model.hasPictureForDate(date);

            BUS.INSTANCE.post(new PictureInvalidatedEvent(entry));

            if (success) {
                Crouton.showText(activity, "Saved picture for Date " + DATE_FORMAT.format(date.toDate()), Style.INFO);
            } else {
                Crouton.showText(activity, "Failed saving todays picture", Style.ALERT);
            }

        }
    }

    public TodayPictureFragment() {
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.baseView = inflater.inflate(R.layout.today_picture_fragment, null);

        // Views.inject(this, baseView);

        this.pictureView = (ImageView) baseView.findViewById(R.id.no_picture_taken);

        // Views.inject(this, baseView);

        this.pictureView.setOnClickListener(this);
        this.pictureView.setOnTouchListener(this);

        this.gestureDetector = new GestureDetector(this.getActivity(), this);

        this.pictureViewLabel = (TextView) this.baseView.findViewById(R.id.no_picture_taken_text);

        this.pictureDescriptionLabel = (TextView) this.baseView.findViewById(R.id.no_picture_taken_text);
        this.pictureDescriptionLabel.setOnClickListener(this);

        this.pictureTextEdit = (EditText) this.baseView.findViewById(R.id.picture_text_edit);
        this.pictureTextEdit.setOnEditorActionListener(this);
        this.pictureTextEdit.setVisibility(View.GONE);

        this.pictureLoadingProgressbar = (ProgressBar) this.baseView.findViewById(R.id.picture_loading_progressbar);
        this.pictureLoadingProgressbar.setVisibility(View.GONE);

        this.pictureWrapper = (ViewGroup) this.baseView.findViewById(R.id.picture_info_container);

        this.diaryButton = (ImageButton) this.baseView.findViewById(R.id.diary_button);
        this.diaryButton.setOnClickListener(this);
        this.diaryButton.setVisibility(View.GONE);

        this.diaryLayout = this.baseView.findViewById(R.id.diary_edit_ref);

        this.diaryEdit = (EditText) this.baseView.findViewById(R.id.diary_edit);
        this.diaryEdit.addTextChangedListener(this);

        this.diaryInfo = (TextView) this.baseView.findViewById(R.id.diary_text_info);
        this.diaryLayout.setVisibility(View.GONE);

        Diary diary = App.getInstance(this.getActivity()).getCurrentDiary();

        if (diary != null) {
            this.model = DataModelBuilder.build(diary.getLabel(), ModelType.fromString(diary.getType()), this.getActivity());
        }

        return this.baseView;

    }

    @Override
    public void onResume() {
        super.onResume();

        BUS.INSTANCE.get().register(this);

        this.animatingDiary = false;
        this.hideDiary();

        if (!this.hasPicture) {
            (new ExecuteAfterUITask(this)).execute();
        }
    }

    @Override
    public void onPause() {
        super.onPause();

        BUS.INSTANCE.get().unregister(this);
    }

    @Subscribe
    public void onDateSelected(DateSelectedEvent dateSelected) {

        if (this.entrySet != null) {
            setToDate(dateSelected.getDate());
        }

    }

    private void setToDate(DateTime date) {

        this.hideDiary();

        this.currentDate = date;

        if (entrySet.hasEntry(date)) {
            this.currentEntry = entrySet.getOrCreateEntry(date, model);
            this.pictureTextEdit.setText(currentEntry.getDescription());
            String description = this.currentEntry.getDescription();

            if (description != null && description.length() > 0) {
                this.pictureDescriptionLabel.setText(description);
            }
        }

        resetImageView();

        loadPictureForCurrentDate();
    }

    private void resetImageView() {
        // this.pictureView.setImageResource(NO_PICTURE_DRAWABLE);
        this.pictureView.setImageBitmap(null);
        this.pictureView.setScaleType(ScaleType.CENTER_INSIDE);
        this.pictureView.setBackgroundResource(R.drawable.take_picture_back);

        this.hasPicture = false;
    }

    @Subscribe
    public void onEntrySetLoaded(EntrySetLoadedEvent entrySetLoaded) {

        Log.d(TAG, "onEntrySetLoaded for " + entrySetLoaded.getEntrySet().getName());

        this.entrySet = entrySetLoaded.getEntrySet();

        this.hideDiary();

        this.currentDate = DateTime.now();

        if (entrySet.hasEntry(currentDate)) {
            this.currentEntry = entrySet.getOrCreateEntry(currentDate, model);
            this.pictureTextEdit.setText(currentEntry.getDescription());
            String description = this.currentEntry.getDescription();

            if (description != null && description.length() > 0) {
                this.pictureDescriptionLabel.setText(description);
            }
        }

    }

    @SuppressWarnings("deprecation")
    @Subscribe
    public void onTodayPictureLoaded(TodayPictureLoadedMessage message) {

        this.animatingDiary = false;

        showDiaryButton();

        setDescriptionLabel();

        this.hasPicture = true;

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1) {
            this.pictureView.setAlpha(0.0f);
        }
        this.pictureView.setImageBitmap(message.getBitmap());
        this.pictureView.setScaleType(ScaleType.CENTER_CROP);

        this.pictureView.animate().alpha(1.0f).setListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    pictureView.setBackground(null);
                } else {
                    pictureView.setBackgroundDrawable(null);
                }
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }
        });

    }

    private void setDescriptionLabel() {
        if (entrySet != null) {
            Entry entry = entrySet.getOrCreateEntry(currentDate, model);

            if (entry.getDescription() == null || entry.getDescription().length() == 0) {
                setPictureNotLabeledText();
            }
        } else {
            setPictureNotLabeledText();
        }
    }

    private void showDiaryButton() {
        if (this.diaryButton.getVisibility() == View.GONE) {

            this.diaryButton.setVisibility(View.VISIBLE);

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1) {
                this.diaryButton.setAlpha(0.0f);
            }
            this.diaryButton.animate().alpha(1.0f);
        }

    }

    @Subscribe
    public void onNoPictureForDate(NoPictureForDateEvent event) {

        this.hasPicture = false;
        this.diaryButton.setVisibility(View.GONE);
        this.pictureView.setImageResource(NO_PICTURE_DRAWABLE);
        if (DateTimeHelpers.isToday(currentDate)) {
            this.pictureDescriptionLabel.setText(R.string.today_picture_not_taken);
        } else {
            this.pictureDescriptionLabel.setText(R.string.that_day_picture_not_taken);
        }

    }

    @Subscribe
    public void onPictureStartLoading(PictureStartLoadingMessage message) {
        // this.pictureLoadingProgressbar.setVisibility(View.VISIBLE);
    }

    @Subscribe
    public void onPictureFinishedLoading(PictureFinishedLoadingMessage message) {
        // this.pictureLoadingProgressbar.setVisibility(View.GONE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PictureTakeHelper.ACTION_TAKE_PICTURE && resultCode == Activity.RESULT_OK) {
            onPictureTaken();
        }

    }

    private void onPictureTaken() {

        Log.d(TAG, "On Picture Taken");

        if (entrySet != null && model != null) {
            this.currentEntry = entrySet.getOrCreateEntry(currentDate, model);

            (new StorePictureForDateTask(model.getDiary(), this.currentDate, pictureTakeHelper.getCurrentPicture(), this.getActivity(),
                    this.currentEntry, this.model)).execute();

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB_MR1) {
                this.pictureView.setAlpha(0.0f);
            }

            if (pictureTakeHelper.setPic(this.pictureView, this.currentEntry)) {
                this.pictureView.animate().alpha(1.0f);

                if (this.currentEntry.getDescription() == null || this.currentEntry.getDescription().length() == 0) {
                    setPictureNotLabeledText();
                }

                this.hasPicture = true;
            }

            this.showDiaryButton();
        }

    }

    private void setPictureNotLabeledText() {
        this.pictureViewLabel.setText(R.string.want_to_label_it);
    }

    @Override
    public void onClick(View v) {

        if (this.entrySet == null)
            return;

        if (v == this.pictureView) {
            takePicture();
        } else if (v == this.pictureDescriptionLabel) {

            // if there is no picture yet this click will also result in the
            // takePicture action
            if (!hasPicture) {
                takePicture();
            } else {
                updateDescriptionEditText();

                this.pictureTextEdit.animate().setListener(null);
                this.pictureDescriptionLabel.animate().alpha(0.0f).setListener(new AnimatorListener() {

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        pictureDescriptionLabel.setVisibility(View.GONE);
                        pictureDescriptionLabel.requestFocus();
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                });

                this.pictureTextEdit.setVisibility(View.VISIBLE);
                this.pictureTextEdit.animate().alpha(1.0f);

            }
        } else if (v == this.diaryButton) {

            Log.d(TAG, "Animating Diary: " + animatingDiary);

            if (!this.animatingDiary) {

                this.animatingDiary = true;

                if (diaryShown) {
                    hideDiary();
                } else {
                    showDiary();
                }

            }
        }
    }

    private void finishDescriptionEditing() {
        if (this.pictureTextEdit.getVisibility() == View.VISIBLE) {
            onPictureTextChanged(this.pictureTextEdit.getText().toString());
        }
    }

    private void updateDescriptionEditText() {

        if (this.currentEntry == null) {
            this.currentEntry = entrySet.getOrCreateEntry(currentDate, model);
        }

        if (this.currentEntry.getDescription() == null) {
            pictureTextEdit.setText("");
        } else {
            pictureTextEdit.setText(this.currentEntry.getDescription());
        }
    }

    private void hideDiary() {

        finishDescriptionEditing();

        moveFocus();

        diaryInfo.animate().alpha(0.0f).setListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {

                diaryInfo.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

        });

        this.pictureWrapper.animate().y(yold).setListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                animatingDiary = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animatingDiary = false;
                diaryShown = false;
                diaryLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                animatingDiary = false;
                diaryShown = false;
                diaryLayout.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

        });
    }

    private void moveFocus() {

        // hide keyboard
        InputMethodManager imm = (InputMethodManager) this.getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(diaryEdit.getWindowToken(), 0);

        // try to unfocus the text edit element
        View v = this.diaryEdit.focusSearch(View.FOCUS_DOWN);

        if (v != null)
            v.requestFocus();

    }

    private void showDiary() {

        finishDescriptionEditing();

        updateDiaryData();

        float height = this.pictureView.getHeight();
        this.yold = this.pictureView.getY();

        DisplayMetrics metrics = new DisplayMetrics();
        this.getActivity().getWindow().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float ynew = yold - (height - (50 * metrics.density));

        this.diaryLayout.setVisibility(View.VISIBLE);

        this.diaryInfo.setVisibility(View.GONE);

        this.pictureWrapper.animate().y(ynew).setListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
                animatingDiary = true;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                animatingDiary = false;
                diaryShown = true;

                diaryInfo.animate().alpha(0.0f).setDuration(10).setListener(new AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                        diaryInfo.setVisibility(View.VISIBLE);
                        diaryInfo.animate().alpha(1.0f).setDuration(400);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }

                });

            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

        });
    }

    private void updateDiaryData() {
        String dateString = DATE_FORMAT.format(this.currentDate.toDate());
        this.currentEntry = entrySet.getOrCreateEntry(currentDate, model);

        this.diaryInfo.setText(dateString);
        this.diaryEdit.setText(this.currentEntry.getLongText());
    }

    private void takePicture() {

        if (hasPicture) {
            Dialog d = onCreateDialog();
            if (d != null) {
                d.show();
            }

        } else {
            doTakePicture();
        }

    }

    private void doTakePicture() {
        pictureTakeHelper.takePicture(this.getActivity());
    }

    public Dialog onCreateDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        ImageView view = new ImageView(this.getActivity());
        view.setScaleType(this.pictureView.getScaleType());

        if (this.pictureView.getDrawable() != null) {

            Bitmap bitmap = ((BitmapDrawable) this.pictureView.getDrawable()).getBitmap();
            view.setImageBitmap(bitmap);

            adjustHeight(view);

            builder.setView(view).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            }).setPositiveButton("Retake", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {

                    onRetakePicture();

                }
            }).setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {

                    onDeletePicture();

                }
            });
            return builder.create();
        }
        return null;
    }

    private void adjustHeight(ImageView view) {
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(view.getWidth(), view.getHeight());
        view.setLayoutParams(params);
    }

    protected void onDeletePicture() {

    }

    protected void onRetakePicture() {
        this.doTakePicture();
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent event) {

        if (actionId == EditorInfo.IME_NULL || actionId == EditorInfo.IME_ACTION_DONE) {

            if (textView == this.pictureTextEdit) {
                onPictureTextChanged(textView.getText().toString());
            }

        }

        return false;
    }

    private void onDiaryTextChanged(String string) {
        storeDiaryForDate(this.currentDate, string);
    }

    private void onPictureTextChanged(final String text) {

        storeDescriptionForDate(this.currentDate, text);

        this.pictureDescriptionLabel.animate().setListener(null);
        this.pictureTextEdit.animate().alpha(0.0f).setListener(new AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                pictureTextEdit.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

        });

        this.pictureDescriptionLabel.setText(text);
        this.pictureDescriptionLabel.setVisibility(View.VISIBLE);
        this.pictureDescriptionLabel.animate().alpha(1.0f);

    }

    private void storeDescriptionForDate(DateTime date, String text) {

        if (entrySet != null) {
            Entry entry = entrySet.getOrCreateEntry(date, model);
            entry.setDescription(text);
        }

    }

    private void storeDiaryForDate(DateTime date, String text) {

        if (entrySet != null) {
            Entry entry = entrySet.getOrCreateEntry(date, model);
            entry.setLongText(text);
        }

    }

    @Override
    public void onAfterUI() {
        loadPictureForCurrentDate();
    }

    @Subscribe
    public void onSetToDiary(SetToDiaryEvent event) {

        Diary diary = event.getDiary();
        recreate(diary);

    }

    private void recreate(Diary diary) {

        this.model = DataModelBuilder.build(diary.getLabel(), ModelType.fromString(diary.getType()), this.getActivity());
        BUS.INSTANCE.post(new DateSelectedEvent(DateTime.now()));

    }

    private void loadPictureForCurrentDate() {

        if (this.entrySet != null && model != null) {
            this.currentEntry = entrySet.getOrCreateEntry(currentDate, model);

            (new LoadPictureForDateTask(model.getDiary(), this.currentEntry, this.model, pictureView.getWidth(), pictureView.getHeight()))
                    .execute();

            if (currentEntry != null) {
                String description = currentEntry.getDescription();
                if (description != null && description.length() > 0) {

                    this.pictureDescriptionLabel.setText(description);

                }
            }
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    public boolean onFling(MotionEvent start, MotionEvent finish, float xVelocity, float yVelocity) {

        if (!this.animatingDiary) {

            if (start.getRawY() < finish.getRawY() && diaryShown) {
                hideDiary();
            } else if (start.getRawY() > finish.getRawY() && !diaryShown) {
                showDiary();
            }
        }
        return true;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent motionEvent) {

        return this.gestureDetector.onTouchEvent(motionEvent);
    }

    public void collapse() {

        Log.d(TAG, "Collapse Called");

        if (diaryShown) {
            hideDiary();
            this.animatingDiary = false;
        }
    }

    @Override
    public void afterTextChanged(Editable edit) {

        if (this.currentEntry != null) {
            this.currentEntry.setLongText(edit.toString());
            BUS.INSTANCE.post(new RefreshDiaryEvent());
        }

    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

}
