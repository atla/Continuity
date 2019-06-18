package com.leihwelt.thedailypicture.model;

import android.util.Log;

import com.dropbox.sync.android.DbxAccountManager;
import com.dropbox.sync.android.DbxException;
import com.dropbox.sync.android.DbxException.Unauthorized;
import com.dropbox.sync.android.DbxFile;
import com.dropbox.sync.android.DbxFileSystem;
import com.dropbox.sync.android.DbxPath;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.leihwelt.thedailypicture.model.WrongDataModel.PictureFileWrapper;

import org.joda.time.DateTime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class DropboxDataModel extends WrongDataModel {

    private static final String TAG = DropboxDataModel.class.getSimpleName();
    private DbxAccountManager accountManager;
    private DbxFileSystem fs;
    private String diary;

    public static class DropboxPictureFileWrapper implements PictureFileWrapper {

        private InputStream stream;
        private DbxFile file;

        public DropboxPictureFileWrapper(InputStream stream, DbxFile file) {
            this.stream = stream;
            this.file = file;
        }

        @Override
        public InputStream getInputStream() {
            return stream;
        }

        @Override
        public void close() {
            try {
                stream.close();

                file.close();

            } catch (IOException e) {
                Log.d(TAG, "Error closing picture file stream ", e);
            }

        }

    }

    public DropboxDataModel(String diary, DbxAccountManager accountManager) throws Unauthorized {
        super(diary);

        this.accountManager = accountManager;
        fs = DbxFileSystem.forAccount(accountManager.getLinkedAccount());
    }

    @Override
    public String getDiary() {
        return this.diary;
    }

    @Override
    public boolean hasEntrySet() {

        String dbpath = this.getRootFolder() + ENTRIES_JSON;

        DbxPath path = new DbxPath(dbpath.replace("//", "/"));
        try {
            return fs.exists(path);
        } catch (DbxException e) {
            Log.d(TAG, "Dropbox error while checking for entry set", e);
        }
        return false;
    }

    @Override
    public void storeEntrySet(EntrySet entrySet) {

        try {
            ObjectMapper mapper = new ObjectMapper();

            String path = this.getRootFolder() + ENTRIES_JSON;
            DbxPath filePath = new DbxPath(path.replaceAll("//", "/"));

            DbxFile file = null;

            if (!fs.exists(filePath)) {
                file = fs.create(filePath);
            } else {
                file = fs.open(filePath);
            }

            FileOutputStream outputStream = file.getWriteStream();

            mapper.writeValue(outputStream, entrySet);

            outputStream.close();
            file.close();

            Log.d(TAG, "Wrote entry set: " + entrySet.getEntries().values());

        } catch (DbxException e) {
            Log.d(TAG, "Dropbox error while storing entry set", e);
        } catch (IOException e) {
            Log.d(TAG, "Dropbox error while storing entry set", e);
        }

    }

    @Override
    public EntrySet loadEntrySet() {

        try {
            ObjectMapper mapper = new ObjectMapper();

            String path = this.getRootFolder() + ENTRIES_JSON;
            DbxPath filePath = new DbxPath(path.replaceAll("//", "/"));
            EntrySet set = null;

            if (fs.exists(filePath)) {

                DbxFile file = fs.open(filePath);
                FileInputStream stream = file.getReadStream();

                set = mapper.readValue(stream, EntrySet.class);

                stream.close();
                file.close();

                Log.d(TAG, "mapper read");
                Log.d(TAG, "READ ENTRY SIZE: " + set.getEntries().size());
                Log.d(TAG, "Load entry set: " + set.getEntries().values());

            }

            Log.d(TAG, "Loaded entry set: " + set);

            return set;
        } catch (DbxException e) {
            Log.d(TAG, "Dropbox error while storing entry set", e);
        } catch (IOException e) {
            Log.d(TAG, "Dropbox error while storing entry set", e);
        }

        return null;
    }

    @Override
    public void savePictureForToday(String picture) {
        final DateTime today = DateTime.now();
        this.savePictureForDate(picture, today);

    }

    @Override
    public String savePictureForDate(String picture, DateTime date) {

        try {

            File input = new File(picture);

            final String pathToDate = getRootFolder() + getDirectoryPathForDate(date);
            final String dateFilename = getFileNameForDate(date);

            String dbpath = pathToDate + dateFilename;

            DbxPath path = new DbxPath(dbpath.replaceAll("//", "/"));

            DbxFile file = null;

            if (!fs.exists(path)) {
                file = fs.create(path);
            } else {
                file = fs.open(path);
            }

            WrongDataModel.copy(new FileInputStream(input), file.getWriteStream());
            return path.getName();

        } catch (DbxException e) {
            Log.d(TAG, "Dropbox error while storing entry set", e);
        } catch (IOException e) {
            Log.d(TAG, "Dropbox error while storing entry set", e);
        }

        return null;
    }

    @Override
    public boolean hasPictureForToday() {
        return hasPictureForDate(DateTime.now());
    }

    @Override
    public boolean hasPictureForDate(DateTime date) {

        final String pathToDate = getRootFolder() + getDirectoryPathForDate(date);
        final String dateFilename = getFileNameForDate(date);

        String dbpath = pathToDate + dateFilename;
        DbxPath path = new DbxPath(dbpath.replace("//", "/"));
        try {
            return fs.exists(path);
        } catch (DbxException e) {
            Log.d(TAG, "Dropbox error in hasPictureForDate", e);
        }

        return false;
    }

    @Override
    public File getPictureFileForDate(DateTime date) {
        return null;
    }

    @Override
    public String getPictureFilenameForDate(DateTime date) {
        final String pathToDate = getRootFolder() + getDirectoryPathForDate(date);
        final String dateFilename = getFileNameForDate(date);
        return pathToDate + dateFilename;
    }

    @Override
    public File getPictureFileForToday() {
        return getPictureFileForDate(DateTime.now());
    }

    @Override
    protected String getAppFolder() {
        return "/";
    }

    @Override
    public PictureFileWrapper loadPictureForDate(DateTime date) {

        final String pathToDate = getRootFolder() + getDirectoryPathForDate(date);
        final String dateFilename = getFileNameForDate(date);

        String dbpath = pathToDate + dateFilename;
        DbxPath path = new DbxPath(dbpath.replaceAll("//", "/"));

        DropboxPictureFileWrapper fileWrapper = null;

        try {
            if (fs.exists(path)) {

                DbxFile file = fs.open(path);

                InputStream is = file.getReadStream();

                fileWrapper = new DropboxPictureFileWrapper(is, file);
            }
        } catch (IOException e) {
            Log.d(TAG, "error loading picture");
        }

        return fileWrapper;
    }
}
