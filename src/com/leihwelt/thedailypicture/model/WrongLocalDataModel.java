package com.leihwelt.thedailypicture.model;

import android.os.Environment;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class WrongLocalDataModel extends WrongDataModel {

    private final static String TAG = WrongLocalDataModel.class.getSimpleName();
    private final static String APP_FOLDER = "TheDailyPicture/";

    public static class LocalPictureFileWrapper implements PictureFileWrapper {

        private InputStream stream;

        public LocalPictureFileWrapper(InputStream stream) {
            this.stream = stream;
        }

        @Override
        public InputStream getInputStream() {
            return stream;
        }

        @Override
        public void close() {
            try {
                stream.close();
            } catch (IOException e) {
                Log.d(TAG, "Error closing picture file stream ", e);
            }
        }

    }

    public WrongLocalDataModel(String diary) {
        super(diary);
    }

    /*
     * (non-Javadoc)
     *
     * @see com.leihwelt.thedailypicture.model.DataModel#hasEntrySet()
     */
    @Override
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

    /*
     * (non-Javadoc)
     *
     * @see
     * com.leihwelt.thedailypicture.model.DataModel#storeEntrySet(com.leihwelt
     * .thedailypicture.model.EntrySet)
     */
    @Override
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

    /*
     * (non-Javadoc)
     *
     * @see com.leihwelt.thedailypicture.model.DataModel#loadEntrySet()
     */
    @Override
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

    /*
     * (non-Javadoc)
     *
     * @see
     * com.leihwelt.thedailypicture.model.DataModel#savePictureForToday(java
     * .lang.String)
     */
    @Override
    public void savePictureForToday(final String picture) {

        final DateTime today = DateTime.now();
        this.savePictureForDate(picture, today);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.leihwelt.thedailypicture.model.DataModel#savePictureForDate(java.
     * lang.String, org.joda.time.DateTime)
     */
    @Override
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

            WrongLocalDataModel.copy(new FileInputStream(input), new FileOutputStream(outputFile));
            // Log.d(TAG, "Wrote picture to " + outputFile.getAbsolutePath());
            return outputFile.getAbsolutePath();

        } catch (FileNotFoundException e) {
            Log.e(WrongLocalDataModel.class.getSimpleName(), "Could not read input file " + picture, e);
        } catch (IOException e) {
            Log.e(WrongLocalDataModel.class.getSimpleName(), "Could not read from file " + picture, e);
        }

        return null;

    }

    /*
     * (non-Javadoc)
     *
     * @see com.leihwelt.thedailypicture.model.DataModel#hasPictureForToday()
     */
    @Override
    public boolean hasPictureForToday() {
        return hasPictureForDate(DateTime.now());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.leihwelt.thedailypicture.model.DataModel#hasPictureForDate(org.joda
     * .time.DateTime)
     */
    @Override
    public boolean hasPictureForDate(final DateTime date) {
        File fileToCheck = getPictureFileForDate(date);

        return fileToCheck.exists();

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.leihwelt.thedailypicture.model.DataModel#getPictureFileForDate(org
     * .joda.time.DateTime)
     */
    @Override
    public File getPictureFileForDate(final DateTime date) {

        final String pathToDate = getRootFolder() + getDirectoryPathForDate(date);
        final String dateFilename = getFileNameForDate(date);
        File dir = new File(Environment.getExternalStorageDirectory() + pathToDate);
        dir.mkdirs();

        return new File(dir, dateFilename);

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.leihwelt.thedailypicture.model.DataModel#getPictureFilenameForDate
     * (org.joda.time.DateTime)
     */
    @Override
    public String getPictureFilenameForDate(final DateTime date) {
        final String pathToDate = getRootFolder() + getDirectoryPathForDate(date);
        final String dateFilename = getFileNameForDate(date);
        return Environment.getExternalStorageDirectory() + pathToDate + dateFilename;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * com.leihwelt.thedailypicture.model.DataModel#getPictureFileForToday()
     */
    @Override
    public File getPictureFileForToday() {
        return getPictureFileForDate(DateTime.now());
    }

    @Override
    protected String getAppFolder() {
        return APP_FOLDER;
    }

    @Override
    public PictureFileWrapper loadPictureForDate(DateTime date) {

        LocalPictureFileWrapper fileWrapper = null;
        try {
            fileWrapper = new LocalPictureFileWrapper(new FileInputStream(this.getPictureFileForDate(date)));
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Could not load file", e);
        }

        return fileWrapper;

    }

}
