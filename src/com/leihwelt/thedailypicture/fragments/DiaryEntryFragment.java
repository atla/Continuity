package com.leihwelt.thedailypicture.fragments;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.annotation.SuppressLint;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.leihwelt.android.helpers.ExecuteAfterUITask;
import com.leihwelt.android.helpers.ExecuteAfterUITask.ExecuteAfterUIListener;
import com.leihwelt.android.view.EventScrollView;
import com.leihwelt.android.view.EventScrollView.ScrollChangedListener;
import com.leihwelt.thedailypicture.App;
import com.leihwelt.thedailypicture.BUS;
import com.leihwelt.thedailypicture.R;
import com.leihwelt.thedailypicture.messages.ShowDiaryEntryEvent;
import com.leihwelt.thedailypicture.messages.ShowDiaryEntryPictureEvent;
import com.leihwelt.thedailypicture.model.DataModel;
import com.leihwelt.thedailypicture.model.DataModelBuilder;
import com.leihwelt.thedailypicture.model.Diary;
import com.leihwelt.thedailypicture.model.Entry;
import com.leihwelt.thedailypicture.model.ModelType;
import com.leihwelt.thedailypicture.picturetake.PictureTakeHelper;
import com.squareup.otto.Subscribe;

import java.io.File;
import java.text.DateFormat;

import butterknife.InjectView;
import butterknife.Views;

@SuppressLint("NewApi")
public class DiaryEntryFragment extends Fragment implements ExecuteAfterUIListener, ScrollChangedListener {

    private final static String TAG = DiaryEntryFragment.class.getSimpleName();
    private final static DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.FULL);

    // Views
    @InjectView(R.id.long_text)
    TextView longText;
    @InjectView(R.id.description_text)
    TextView description;
    @InjectView(R.id.picture_view)
    ImageView pictureView;
    @InjectView(R.id.date_label)
    TextView date;
    @InjectView(R.id.event_scroll_view)
    EventScrollView scrollView;
    @InjectView(R.id.image_wrapper)
    ViewGroup imageWrapper;
    @InjectView(R.id.description_label_overlay)
    TextView descriptionOverlay;
    @InjectView(R.id.relative_wrapper)
    ViewGroup container;

    private Entry entry;
    private Bitmap picture = null;
    private boolean firstShown = true;
    private ShareActionProvider shareActionProvider;

    private final static class LoadPictureForDateTask extends AsyncTask<Void, Void, Bitmap> {

        private int height;
        private int width;
        private Context ctx;
        private Diary diary;
        private Entry entry;

        public LoadPictureForDateTask(Diary diary, Entry entry, int width, int height, Context ctx) {

            this.width = width;
            this.height = height;
            this.entry = entry;
            this.diary = diary;
            this.ctx = ctx;
        }

        @Override
        protected Bitmap doInBackground(Void... arg0) {
            DataModel model = DataModelBuilder.build(diary.getLabel(), ModelType.fromString(diary.getType()), ctx);
            Bitmap bitmap = null;

            if (model.hasPictureForDate(entry.getDate())) {

                File file = model.getPictureFileForDate(entry.getDate());

                if (file != null) {
                    PictureTakeHelper helper = new PictureTakeHelper();
                    bitmap = helper.loadPicture(file.getAbsolutePath(), entry, width, height);
                }

            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            if (result != null) {
                BUS.INSTANCE.get().post(new ShowDiaryEntryPictureEvent(result));
            }
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
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);

        inflater.inflate(R.menu.diary_entry_menu, menu);

        MenuItem shareItem = menu.findItem(R.id.menu_entry_share);

        shareActionProvider = (ShareActionProvider) shareItem.getActionProvider();

        if (this.entry != null) {
            Intent shareIntent = createShareIntent();
            shareActionProvider.setShareIntent(shareIntent);
        }

    }

    private Intent createShareIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        Uri picUri = Uri.parse(entry.getPicture());
        shareIntent.setType("image/jpeg");
        shareIntent.putExtra(Intent.EXTRA_STREAM, picUri);
        shareIntent.putExtra(Intent.EXTRA_TEXT, addVia(entry.getDescription() + "\n" + entry.getLongText()));
        shareIntent.putExtra(Intent.EXTRA_TITLE, entry.getDescription());
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, entry.getDescription());
        return shareIntent;
    }

    private String addVia(String text) {
        return text + "\n\nvia @TheDailyPicture";
    }

    @Subscribe
    public void onShowDiaryEntryPicture(final ShowDiaryEntryPictureEvent event) {

        this.picture = event.getPicture();
        this.pictureView.setVisibility(View.GONE);

        if (firstShown) {
            this.pictureView.animate().alpha(0.0f).setDuration(50).setListener(new AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    Bitmap picture = event.getPicture();
                    pictureView.setScaleType(ScaleType.CENTER_CROP);
                    pictureView.setImageBitmap(picture);
                    pictureView.setVisibility(View.VISIBLE);

                    pictureView.animate().setDuration(400).alpha(1.0f).setListener(new AnimatorListener() {

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

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

            });

            firstShown = false;
        } else {
            Bitmap picture = event.getPicture();
            pictureView.setScaleType(ScaleType.CENTER_CROP);
            pictureView.setImageBitmap(picture);
            pictureView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        this.setRetainInstance(true);
        this.setHasOptionsMenu(true);

        return inflater.inflate(R.layout.diary_entry_fragment, null);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Views.inject(this, view);

        if (scrollView != null) {
            this.scrollView.setListener(this);
        }

        if (this.entry != null) {
            setToEntry();
        }

        (new ExecuteAfterUITask(this)).execute();

    }

    @Subscribe
    public void onShowDiaryEntry(ShowDiaryEntryEvent showDiaryEvent) {
        this.entry = showDiaryEvent.getEntry();

        setToEntry();
    }

    private void setToEntry() {
        this.longText.setText(entry.getLongText());
        this.description.setText(entry.getDescription());
        this.date.setText(DATE_FORMAT.format(entry.getDate().toDate()));

        this.description.setVisibility(TextUtils.isEmpty(entry.getDescription()) ? View.GONE : View.VISIBLE);

        if (this.descriptionOverlay != null)
            this.descriptionOverlay.setText(entry.getDescription());

        if (shareActionProvider != null) {
            Intent shareIntent = createShareIntent();
            shareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onAfterUI() {

        // resize image wrapper if available
        if (imageWrapper != null && container != null) {
            LayoutParams params = (LayoutParams) imageWrapper.getLayoutParams();
            params.height = container.getWidth();
            params.width = container.getWidth();
            imageWrapper.setLayoutParams(params);
        }

        App app = App.getInstance(this.getActivity());

        if (app != null) {

            Diary diary = app.getCurrentDiary();

            if (picture == null) {
                LoadPictureForDateTask task = new LoadPictureForDateTask(diary, entry, pictureView.getWidth(), pictureView.getHeight(), this.getActivity());
                task.execute();
            } else {
                onShowDiaryEntryPicture(new ShowDiaryEntryPictureEvent(picture));
            }
        }
    }

    @Override
    public void onScrollChanged(int l, int t, int oldl, int oldt) {

        LayoutParams params = ((LinearLayout.LayoutParams) this.imageWrapper.getLayoutParams());

        int ypos = params.topMargin + params.height;

        if (t > ypos && this.description.getVisibility() == View.VISIBLE) {

            this.description.setVisibility(View.INVISIBLE);
            this.descriptionOverlay.setAlpha(0.0f);
            this.descriptionOverlay.setVisibility(View.VISIBLE);
            this.descriptionOverlay.animate().alpha(1.0f).setListener(null);

        } else if (t < ypos && this.description.getVisibility() == View.INVISIBLE) {

            this.description.setVisibility(View.VISIBLE);

            this.descriptionOverlay.animate().alpha(0.0f).setListener(new AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    descriptionOverlay.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }
            });
        }

    }
}
