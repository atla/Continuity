package com.leihwelt.thedailypicture;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v13.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.dropbox.sync.android.DbxAccountManager;
import com.leihwelt.android.helpers.DateTimeHelpers;
import com.leihwelt.thedailypicture.fragments.CalendarFragment;
import com.leihwelt.thedailypicture.fragments.DiarySelectionFragment;
import com.leihwelt.thedailypicture.fragments.PhoneTodayFragment;
import com.leihwelt.thedailypicture.fragments.TodayPictureFragment;
import com.leihwelt.thedailypicture.fragments.diary.DiaryFragment;
import com.leihwelt.thedailypicture.messages.DateSelectedEvent;
import com.leihwelt.thedailypicture.messages.DeleteDiaryEvent;
import com.leihwelt.thedailypicture.messages.DiaryListLoadedEvent;
import com.leihwelt.thedailypicture.messages.DiarySelectedEvent;
import com.leihwelt.thedailypicture.messages.EntrySetLoadedEvent;
import com.leihwelt.thedailypicture.messages.SetDropboxAccount;
import com.leihwelt.thedailypicture.messages.SetToDiaryEvent;
import com.leihwelt.thedailypicture.messages.ShowDiaryEntryEvent;
import com.leihwelt.thedailypicture.messages.ShowDiarySelectionEvent;
import com.leihwelt.thedailypicture.messages.ShowTodayPictureEvent;
import com.leihwelt.thedailypicture.model.DataModel;
import com.leihwelt.thedailypicture.model.DataModelBuilder;
import com.leihwelt.thedailypicture.model.Diary;
import com.leihwelt.thedailypicture.model.DiaryList;
import com.leihwelt.thedailypicture.model.EntrySet;
import com.leihwelt.thedailypicture.model.ModelType;
import com.leihwelt.thedailypicture.tasks.LoadEntrySetTask;
import com.leihwelt.thedailypicture.tasks.StoreEntrySetTask;
import com.squareup.otto.Subscribe;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.util.Locale;

import butterknife.InjectView;
import butterknife.Views;
import de.neofonie.mobile.app.android.widget.crouton.Crouton;
import de.neofonie.mobile.app.android.widget.crouton.Style;

public class MainActivity extends Activity implements OnPageChangeListener, TabListener {

    private static final int TAB_ALPHA_UNSELECTED = 160;
    private static final int TAB_ALPHA_SELECTED = 255;

    private static final String THE_DAILY = "The Daily ";
    private final static DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);
    private final static String TAG = MainActivity.class.getSimpleName();

    private final static String[] CONTENT = new String[]{"Past", "Today", "My Diary"};
    private static final int REQUEST_LINK_TO_DBX = 1120;

    @InjectView(R.id.pager)
    ViewPager mPager;
    private CalendarFragment calendarFragment;
    private PhoneTodayFragment phoneTodayFragment;
    private MainFragmentAdapter mAdapter;

    private Fragment[] viewPagerFragments = new Fragment[3];

    private TodayPictureFragment todayFragment;

    private DateTime selectedDate = DateTime.now();
    private DataModel model;
    private EntrySet entrySet;
    private DiaryFragment diaryFragment;

    private Tab tab1;
    private Tab tab2;
    private Tab tab3;
    private DbxAccountManager accountManager;

    private class MainFragmentAdapter extends FragmentPagerAdapter {

        private static final int TODAY_POSITION = 1;

        public MainFragmentAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return viewPagerFragments[position];
        }

        @Override
        public CharSequence getPageTitle(int position) {

            if (position == TODAY_POSITION) {
                return getTodayTitle();
            }

            return CONTENT[position % CONTENT.length].toUpperCase(Locale.getDefault());
        }

        @Override
        public int getCount() {
            return 3;
        }

    }

    @Subscribe
    public void onShowTodayPicture(ShowTodayPictureEvent showTodayPicture) {

        if (mPager != null) {
            // set to today picture fragment
            mPager.setCurrentItem(1, true);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Views.inject(this);

        accountManager = DbxAccountManager.getInstance(getApplicationContext(), App.APP_KEY, App.APP_SECRET);

        // remove background from main window to reduce GPU overdraw behind our
        // background
        getWindow().setBackgroundDrawable(null);

        // phone / swipe based main layout
        if (mPager != null) {

            this.calendarFragment = new CalendarFragment();
            this.phoneTodayFragment = new PhoneTodayFragment();
            this.todayFragment = this.phoneTodayFragment.getTodayPictureFragment();
            this.diaryFragment = new DiaryFragment();

            viewPagerFragments[0] = this.calendarFragment;
            viewPagerFragments[1] = this.phoneTodayFragment;
            viewPagerFragments[2] = this.diaryFragment;

            mAdapter = new MainFragmentAdapter(this.getFragmentManager());
            mPager.setAdapter(mAdapter);

            mPager.setOnPageChangeListener(this);

            ActionBar bar = getActionBar();
            bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
            tab1 = bar.newTab();
            tab2 = bar.newTab();
            tab3 = bar.newTab();
            tab1.setIcon(R.drawable.ic_action_calendar_day);
            tab2.setIcon(R.drawable.ic_action_user);
            tab3.setIcon(R.drawable.ic_action_database);

            tab1.setTabListener(this);
            tab2.setTabListener(this);
            tab3.setTabListener(this);
            bar.addTab(tab1);
            bar.addTab(tab2);
            bar.addTab(tab3);

            tab1.getIcon().setAlpha(TAB_ALPHA_UNSELECTED);
            tab2.getIcon().setAlpha(TAB_ALPHA_SELECTED);
            tab3.getIcon().setAlpha(TAB_ALPHA_UNSELECTED);
            ;

            bar.selectTab(tab2);

        }
        // setup tablet layout
        else {
            this.todayFragment = (TodayPictureFragment) this.getFragmentManager().findFragmentById(R.id.today_picture_fragment);
            this.calendarFragment = new CalendarFragment();
            this.todayFragment = new TodayPictureFragment();

            FragmentManager fragmentManager = this.getFragmentManager();

            if (this.calendarFragment != null) {
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.add(R.id.calendar_container, this.calendarFragment);
                transaction.commit();
            }
        }

        ActionBar actionBar = this.getActionBar();

        actionBar.setLogo(R.drawable.ic_launcher);
        actionBar.setDisplayUseLogoEnabled(true);

    }

    @Subscribe
    public void onSetToDiary(SetToDiaryEvent event) {

        // store current information
        if (entrySet != null && model != null) {
            (new StoreEntrySetTask(model, entrySet)).execute();
        }

        setToDiary(event.getDiary());

        // only show crouton if set via menu
        Crouton.showText(this, "Set to Diary \"" + event.getDiary().getLabel() + "\"", Style.INFO);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BUS.INSTANCE.get().register(this);

        if (this.entrySet == null) {
            App app = App.getInstance(this);
            Diary diary = app.getCurrentDiary();

            if (diary != null) {
                setToDiary(diary);

            }
        }

    }

    private void setToDiary(Diary diary) {
        this.model = DataModelBuilder.build(diary.getLabel(), ModelType.fromString(diary.getType()), this);
        this.entrySet = null;
        this.setTitle(THE_DAILY + diary.getLabel());
        (new LoadEntrySetTask(diary.getLabel(), model)).execute();
    }

    @Subscribe
    public void onDiaryListLoaded(DiaryListLoadedEvent event) {

        Diary diary = App.getInstance(this).getCurrentDiary();

        if (diary == null) {

            DiaryList list = event.getDiaryList();

            if (list != null && list.size() > 0) {
                Diary d = list.iterator().next();
                BUS.INSTANCE.post(new DiarySelectedEvent(d));
            } else {

                // TODO: CALL CREATE DIARY Dialog
                BUS.INSTANCE.post(new ShowDiarySelectionEvent());
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        BUS.INSTANCE.get().unregister(this);

        if (entrySet != null && model != null) {
            (new StoreEntrySetTask(model, entrySet)).execute();
        }
    }

    @Subscribe
    public void onEntrySetLoaded(EntrySetLoadedEvent entrySetLoaded) {
        this.entrySet = entrySetLoaded.getEntrySet();
    }

    public CharSequence getTodayTitle() {

        if (DateTimeHelpers.isToday(this.selectedDate)) {
            return "Today";
        }
        return DATE_FORMAT.format(this.selectedDate.toDate());
    }

    @Subscribe
    public void onShowDiaryEntry(ShowDiaryEntryEvent showDiaryEvent) {
        this.startActivity(new Intent(this, DiaryEntryActivity.class));
    }

    private void reloadDiaryFragment() {
        this.diaryFragment = new DiaryFragment();
        viewPagerFragments[2] = this.diaryFragment;

        mPager.setCurrentItem(2, true);

    }

    @Subscribe
    public void onDateSelected(DateSelectedEvent dateSelected) {

        this.selectedDate = dateSelected.getDate();

        if (tab2 != null) {
            this.tab2.setText(getTodayTitle());
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (this.todayFragment == null && this.phoneTodayFragment != null)
            this.todayFragment = this.phoneTodayFragment.getTodayPictureFragment();

        if (todayFragment != null) {
            todayFragment.onActivityResult(requestCode, resultCode, data);
        } else {
            Log.e(TAG, "TODAY FRAGMENT IS NULL");
        }

        if (requestCode == REQUEST_LINK_TO_DBX) {
            if (resultCode == Activity.RESULT_OK) {
                BUS.INSTANCE.post(new SetDropboxAccount(accountManager));
                Crouton.showText(this, "Dropbox account linked", Style.INFO);
            } else {
                Crouton.showText(this, "Dropbox account not linked", Style.ALERT);
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_main, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.change_diary) {
            onShowDiarySelection();
        } else if (item.getItemId() == R.id.menu_create_diary) {
            onShowCreateDiary();
        } else if (item.getItemId() == R.id.menu_delete_diary) {
            showDeleteDiaryDialog();
        } else if (item.getItemId() == R.id.register_dropbox) {
            registerDropbox();
        }

        return super.onOptionsItemSelected(item);

    }

    private void registerDropbox() {
        // BUS.INSTANCE.post(new RegisterDropboxAccountEvent());
        accountManager = DbxAccountManager.getInstance(getApplicationContext(), App.APP_KEY, App.APP_SECRET);

        accountManager.startLink(this, REQUEST_LINK_TO_DBX);

    }

    private void showDeleteDiaryDialog() {

        createDeleteDialog().show();
    }

    public Dialog createDeleteDialog() {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_diary_dialog).setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                Diary d = App.getInstance(getApplicationContext()).getCurrentDiary();

                BUS.INSTANCE.post(new DeleteDiaryEvent(d));

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // do nothgin
            }
        });
        // Create the AlertDialog object and return it
        return builder.create();
    }

    private void onShowCreateDiary() {
        // FragmentManager fm = getSupportFragmentManager();
        // CreateDiaryFragment createDiaryFragment = new CreateDiaryFragment();
        // createDiaryFragment.show(fm, CREATE_DIARY);

        this.startActivity(new Intent(this, CreateDiaryActivity.class));

    }

    private void onShowDiarySelection() {
        FragmentManager fm = getFragmentManager();
        DiarySelectionFragment selectionFragment = new DiarySelectionFragment();
        selectionFragment.show(fm, null);

    }

    @Subscribe
    public void onShowDiarySelection(ShowDiarySelectionEvent event) {
        onShowDiarySelection();
    }

    private void onShowDatePicker() {
        if (mPager != null) {
            // set to calendar fragment
            mPager.setCurrentItem(0, true);
        }
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {

    }

    @Override
    public void onPageScrolled(int page, float arg1, int arg2) {

    }

    @Override
    public void onPageSelected(int page) {

        if (this.todayFragment == null && this.phoneTodayFragment != null) {
            this.todayFragment = this.phoneTodayFragment.getTodayPictureFragment();
        }

        if (todayFragment != null)
            this.todayFragment.collapse();

        ActionBar bar = this.getActionBar();
        bar.getTabAt(page).select();

    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction ft) {

        if (tab == tab1)
            mPager.setCurrentItem(0);
        else if (tab == tab2)
            mPager.setCurrentItem(1);
        else if (tab == tab3)
            mPager.setCurrentItem(2);

        if (tab != tab2) {
            if (todayFragment != null)
                this.todayFragment.collapse();

        }

        tab.getIcon().setAlpha(TAB_ALPHA_SELECTED);

    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction ft) {

        tab.getIcon().setAlpha(TAB_ALPHA_UNSELECTED);

    }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction ft) {

    }

}
