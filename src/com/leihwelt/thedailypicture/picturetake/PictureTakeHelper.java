package com.leihwelt.thedailypicture.picturetake;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;

import com.leihwelt.thedailypicture.model.Entry;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PictureTakeHelper {

    private final static String TAG = PictureTakeHelper.class.getSimpleName();

    public static final int ACTION_TAKE_PICTURE = 1;

    private static final String BITMAP_STORAGE_KEY = "viewbitmap";
    private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
    // private ImageView mImageView;
    // private Bitmap mImageBitmap;

    private String mCurrentPhotoPath;

    private static final String JPEG_FILE_PREFIX = "IMG_";
    private static final String JPEG_FILE_SUFFIX = ".jpg";

    private AlbumStorageDirFactory mAlbumStorageDirFactory;

    public PictureTakeHelper() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
            mAlbumStorageDirFactory = new FroyoAlbumDirFactory();
        } else {
            mAlbumStorageDirFactory = new BaseAlbumDirFactory();
        }
    }

    /* Photo album for this application */
    public String getAlbumName() {
        return "CameraSample";
    }

    public void takePicture(Activity activity) {

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File f = null;

        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        }

        Log.d(PictureTakeHelper.class.getSimpleName(), "image path: " + mCurrentPhotoPath);

        activity.startActivityForResult(takePictureIntent, ACTION_TAKE_PICTURE);
    }

    public File getAlbumDir() {
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = mAlbumStorageDirFactory.getAlbumStorageDir(getAlbumName());

            if (storageDir != null) {
                if (!storageDir.mkdirs()) {
                    if (!storageDir.exists()) {
                        Log.d("CameraSample", "failed to create directory");
                        return null;
                    }
                }
            }

        } else {
            Log.v(PictureTakeHelper.class.getName(), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    public File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File albumF = getAlbumDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, albumF);
        return imageF;
    }

    public File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    public String getCurrentPicture() {
        return mCurrentPhotoPath;
    }

    public Bitmap loadPicture(InputStream is, Entry entry, int targetWidth, int targetHeight) {

        Log.d(TAG, "Loading picture for size " + targetWidth + "x" + targetHeight);

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        // define a really huge scale factor by default
        int scaleFactor = 5;

        if (entry.getWidth() > 0 && entry.getHeight() > 0) {
            if ((targetWidth > 0) || (targetHeight > 0)) {
                scaleFactor = Math.min(entry.getWidth() / targetWidth, entry.getHeight() / targetHeight);
            }
        }

        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bmp = BitmapFactory.decodeStream(is, null, bmOptions);

        // store last loaded sizes
        entry.setWidth(bmOptions.outWidth);
        entry.setHeight(bmOptions.outHeight);

        return bmp;
    }

    public Bitmap loadLocalPicture(String picture, int width, int height) {

        Log.d(TAG, "Loading picture for size " + width + "x" + height);

        BitmapFactory.Options bmOptions = new BitmapFactory.Options();

        int scaleFactor = 1;

        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(picture, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        if ((width > 0) || (height > 0)) {
            scaleFactor = Math.min(photoW / width, photoH / height);
        }

        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(picture, bmOptions);

        return bitmap;
    }

    public Bitmap loadPicture(String picture, Entry entry, int width, int height) {

        Bitmap bitmap = this.loadLocalPicture(picture, width, height);

        return bitmap;

    }

    public boolean setPic(ImageView mImageView, InputStream is, Entry entry) {

        Bitmap bitmap = this.loadPicture(is, entry, mImageView.getWidth(), mImageView.getHeight());

        if (bitmap != null) {
            /* Associate the Bitmap to the ImageView */
            mImageView.setScaleType(ScaleType.CENTER_CROP);
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);

            return bitmap != null;
        }

        return false;

    }

    // this is only used to set the picture right after image taking
    public boolean setPic(ImageView mImageView, Entry entry) {

        Bitmap bitmap = this.loadPicture(mCurrentPhotoPath, entry, mImageView.getWidth(), mImageView.getHeight());

        if (bitmap != null) {
			/* Associate the Bitmap to the ImageView */
            mImageView.setScaleType(ScaleType.CENTER_CROP);
            mImageView.setImageBitmap(bitmap);
            mImageView.setVisibility(View.VISIBLE);

            return bitmap != null;
        }
        return false;
    }

    public void galleryAddPic(Context ctx) {
        Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        ctx.sendBroadcast(mediaScanIntent);
    }

    // public boolean setPic(ImageView pictureView) {
    // return this.setPic(pictureView, mCurrentPhotoPath);
    // }
}
