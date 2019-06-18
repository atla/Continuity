package com.leihwelt.thedailypicture.model;

import android.os.Environment;

import org.joda.time.DateTime;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;

public abstract class WrongDataModel {

    protected static final String ENTRIES_JSON = "entries.json";
    protected final static String DIARY_LIST = "diary_list.json";
    protected final static String PICTURE_PREFIX = "PICTURE_";
    protected final static String THUMB_PREFIX = "THUMB_";
    protected final static String PICTURE_SUFFIX = ".jpg";

    protected String diary;

    public static interface PictureFileWrapper {
        public InputStream getInputStream();

        public void close();
    }

    public WrongDataModel(String diary) {
        this.diary = diary;
    }

    public String getDiary() {
        return diary;
    }

    protected String getDirectoryPathForDate(final DateTime dateTime) {

        StringBuilder b = new StringBuilder();

        b.append("/");
        b.append(dateTime.getYear());
        b.append("/");
        b.append(dateTime.getMonthOfYear());
        b.append("/");

        return b.toString();

    }

    protected String getThumbFileNameForDate(final DateTime dateTime) {
        StringBuilder b = new StringBuilder();

        b.append(THUMB_PREFIX);
        b.append(dateTime.getDayOfMonth());
        b.append(PICTURE_SUFFIX);

        return b.toString();

    }

    protected String getFileNameForDate(final DateTime dateTime) {
        StringBuilder b = new StringBuilder();

        b.append(PICTURE_PREFIX);
        b.append(dateTime.getDayOfMonth());
        b.append(PICTURE_SUFFIX);

        return b.toString();

    }

    protected String getRootFolder() {
        return "/" + getAppFolder() + diary + "/";
    }

    protected abstract String getAppFolder();

    protected String getPictureThumbFilenameForDate(DateTime date) {
        final String pathToDate = getRootFolder() + getDirectoryPathForDate(date);
        final String dateFilename = getThumbFileNameForDate(date);
        return Environment.getExternalStorageDirectory() + pathToDate + dateFilename;
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

    public abstract boolean hasEntrySet();

    public abstract void storeEntrySet(EntrySet entrySet);

    public abstract EntrySet loadEntrySet();

    public abstract void savePictureForToday(final String picture);

    /**
     * This method saves a picture by first reading it from filesystem and than
     * directly writing the data to the specific date folder. This is done
     * without decoding the image data itself. The picture should be already in
     * the right size and decoding.
     *
     * @param localSource Path to the image on the file system that should be saved for
     *                    the date
     * @param date        The date the image should be saved to
     */
    public abstract String savePictureForDate(final String localSource, final DateTime date);

    public abstract boolean hasPictureForToday();

    public abstract boolean hasPictureForDate(final DateTime date);

    public abstract File getPictureFileForDate(final DateTime date);

    public abstract String getPictureFilenameForDate(final DateTime date);

    public abstract File getPictureFileForToday();

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

    public abstract PictureFileWrapper loadPictureForDate(DateTime date);

}