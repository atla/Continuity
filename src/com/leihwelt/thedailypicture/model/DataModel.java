package com.leihwelt.thedailypicture.model;

import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.DateTime;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;

public class DataModel {
    private static final String ENTRIES_JSON = "entries.json";

    private final static String TAG = WrongDataModel.class.getSimpleName();

    private final static String APP_FOLDER = "TheDailyPicture/";

    private final static String DIARY_LIST = "diary_list.json";

    private final static String PICTURE_PREFIX = "PICTURE_";
    private final static String THUMB_PREFIX = "THUMB_";
    private final static String PICTURE_SUFFIX = ".jpg";
    private String diary;

    private final static DateFormat DATE_FORMAT = DateFormat.getDateInstance();

//	public static OldDataModel dm(String root) {
//		return new OldDataModel(root);
//	}

    public DataModel(String diary) {
        this.diary = diary;
    }

    public String getDiary() {
        return this.diary;
    }

    private String getDirectoryPathForDate(final DateTime dateTime) {

        StringBuilder b = new StringBuilder();

        b.append("/");
        b.append(dateTime.getYear());
        b.append("/");
        b.append(dateTime.getMonthOfYear());
        b.append("/");

        return b.toString();

    }

    private String getThumbFileNameForDate(final DateTime dateTime) {
        StringBuilder b = new StringBuilder();

        b.append(THUMB_PREFIX);
        b.append(dateTime.getDayOfMonth());
        b.append(PICTURE_SUFFIX);

        return b.toString();

    }

    private String getFileNameForDate(final DateTime dateTime) {
        StringBuilder b = new StringBuilder();

        b.append(PICTURE_PREFIX);
        b.append(dateTime.getDayOfMonth());
        b.append(PICTURE_SUFFIX);

        return b.toString();

    }

    public boolean hasEntrySet() {

        File dir = new File(Environment.getExternalStorageDirectory() + this.getRootFolder() + ENTRIES_JSON);
        return dir.exists();
    }

    public static DiaryList loadDiaryList() {
        ObjectMapper mapper = new ObjectMapper();
        String file = "/" + APP_FOLDER + "/";
        File dir = new File(Environment.getExternalStorageDirectory() + file);
        dir.mkdirs();
        File diaryListFile = new File(dir, DIARY_LIST);

        DiaryList diaryList = null;

        try {

            if (diaryListFile.exists()) {

                diaryList = mapper.readValue(diaryListFile, DiaryList.class);

                Log.d(TAG, "Loaded diary list: " + diaryList);

            }
        } catch (IOException e) {
            Log.d(TAG, "Could not load entries file ", e);
        } catch (RuntimeException e) {
            Log.d(TAG, "Could not load entries file ", e);
        }

        return diaryList;
    }

    public static void storeDiaryList(DiaryList list) {
        ObjectMapper mapper = new ObjectMapper();
        String file = "/" + APP_FOLDER + "/";
        File dir = new File(Environment.getExternalStorageDirectory() + file);
        dir.mkdirs();
        File diaryListFile = new File(dir, DIARY_LIST);

        try {

            if (!diaryListFile.exists()) {
                diaryListFile.createNewFile();
            }
            if (diaryListFile.exists()) {
                mapper.writeValue(diaryListFile, list);
            }
        } catch (IOException e) {
            Log.d(TAG, "Could not save diary list file ", e);
        }
    }

    public void storeEntrySet(EntrySet entrySet) {
        ObjectMapper mapper = new ObjectMapper();
        String file = this.getRootFolder();
        File dir = new File(Environment.getExternalStorageDirectory() + file);
        dir.mkdirs();
        File entriesFile = new File(dir, ENTRIES_JSON);

        try {

            if (!entriesFile.exists()) {
                entriesFile.createNewFile();
            }
            if (entriesFile.exists()) {
                mapper.writeValue(entriesFile, entrySet);

                Log.d(TAG, "Wrote entry set: " + entrySet.getEntries().values());

            }
        } catch (IOException e) {
            Log.d(TAG, "Could not save entries file ", e);
        }
    }

    public EntrySet loadEntrySet() {
        ObjectMapper mapper = new ObjectMapper();
        String file = this.getRootFolder();
        File dir = new File(Environment.getExternalStorageDirectory() + file);
        dir.mkdirs();
        File entriesFile = new File(dir, ENTRIES_JSON);

        EntrySet set = null;

        Log.d(TAG, "Trying to load entry set: " + entriesFile.getAbsolutePath());

        try {

            if (entriesFile.exists()) {

                set = mapper.readValue(entriesFile, EntrySet.class);

                Log.d(TAG, "mapper read");
                Log.d(TAG, "READ ENTRY SIZE: " + set.getEntries().size());
                Log.d(TAG, "Load entry set: " + set.getEntries().values());

            }
        } catch (IOException e) {
            Log.d(TAG, "Could not load entries file ", e);
        } catch (RuntimeException e) {
            Log.d(TAG, "Could not load entries file ", e);
        }

        Log.d(TAG, "Loaded entry set: " + set);

        return set;
    }

    public void savePictureForToday(final String picture) {

        final DateTime today = DateTime.now();
        this.savePictureForDate(picture, today);

    }

    /**
     * This method saves a picture by first reading it from filesystem and than
     * directly writing the data to the specific date folder. This is done
     * without decoding the image data itself. The picture should be already in
     * the right size and decoding.
     *
     * @param picture Path to the image on the file system that should be saved for
     *                the date
     * @param date    The date the image should be saved to
     */
    public String savePictureForDate(final String picture, final DateTime date) {

        File input = new File(picture);
        File outputFile = getPictureFileForDate(date);

        // Log.d(TAG, "dateFilename " + dateFilename);
        // Log.d(TAG, "dir " + dir);
        // Log.d(TAG, "Writing picture to " + outputFile.getAbsolutePath());

        if (!outputFile.exists()) {
            try {
                outputFile.createNewFile();
            } catch (IOException e) {
                Log.d(TAG, "Could not create file " + outputFile.getAbsolutePath());
            }
        }

        try {

            WrongDataModel.copy(new FileInputStream(input), new FileOutputStream(outputFile));
            // Log.d(TAG, "Wrote picture to " + outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();

        } catch (FileNotFoundException e) {
            Log.e(WrongDataModel.class.getSimpleName(), "Could not read input file " + picture, e);
        } catch (IOException e) {
            Log.e(WrongDataModel.class.getSimpleName(), "Could not read from file " + picture, e);
        }

        return null;

    }

    public static void copy(InputStream in, OutputStream out) throws IOException {

        BufferedInputStream bin = new BufferedInputStream(in);
        BufferedOutputStream bout = new BufferedOutputStream(out);

        while (true) {
            int datum = bin.read();
            if (datum == -1)
                break;
            bout.write(datum);
        }
        bout.flush();
    }

    public boolean hasPictureForToday() {
        return hasPictureForDate(DateTime.now());
    }

    public boolean hasPictureForDate(final DateTime date) {
        File fileToCheck = getPictureFileForDate(date);

        return fileToCheck.exists();

    }

    public File getPictureFileForDate(final DateTime date) {

        final String pathToDate = getRootFolder() + getDirectoryPathForDate(date);
        final String dateFilename = getFileNameForDate(date);
        File dir = new File(Environment.getExternalStorageDirectory() + pathToDate);
        dir.mkdirs();

        return new File(dir, dateFilename);

    }

    public String getPictureFilenameForDate(final DateTime date) {
        final String pathToDate = getRootFolder() + getDirectoryPathForDate(date);
        final String dateFilename = getFileNameForDate(date);
        return Environment.getExternalStorageDirectory() + pathToDate + dateFilename;
    }

    private String getRootFolder() {
        return "/" + APP_FOLDER + diary + "/";
    }

    public File getPictureFileForToday() {
        return getPictureFileForDate(DateTime.now());
    }

    public void validateSet(EntrySet set) {

        for (Entry entry : set.getEntries().values()) {
            fix(entry);
        }

    }

    private void fix(Entry entry) {
        if (entry.getPicture() == null) {
            entry.setPicture(getPictureFilenameForDate(entry.getDate()));
        }
        if (entry.getThumbnail() == null) {
            entry.setThumbnail(getPictureThumbFilenameForDate(entry.getDate()));
        }
    }

    private String getPictureThumbFilenameForDate(DateTime date) {
        final String pathToDate = getRootFolder() + getDirectoryPathForDate(date);
        final String dateFilename = getThumbFileNameForDate(date);
        return Environment.getExternalStorageDirectory() + pathToDate + dateFilename;
    }

    public Entry[] getLatestThreeDays(EntrySet set) {

        ArrayList<Entry> data = new ArrayList<Entry>(set.getEntries().values());

        Collections.sort(data, new EntryComparator());

        int count = data.size() < 4 ? data.size() - 1 : 3;

        if (count > 0) {
            Entry[] entries = new Entry[count];

            for (int i = 0; i < count; ++i) {
                entries[i] = data.get(i + 1);
            }

            return entries;
        }

        return null;

    }

}
