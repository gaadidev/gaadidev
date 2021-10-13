package com.gaadi.neon.util;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.scanlibrary.R;

import java.io.File;

public class FileUtils {

    private final String TAG = FileUtils.class.getSimpleName();
    public static final String MEDIA_STORAGE_PATH = Environment.DIRECTORY_DCIM + File.separator + "NEON";

    public static Uri createDocFileName(String fileName, String path){

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            File file = createDocFileLegacy(fileName, path);
            return Uri.fromFile(file);
        } else {
            return createDocFileAbovePie(fileName,  path, Constants.PIC_MIME_TYPE);
        }
    }
    private static File createDocFileLegacy(String fileName, String path){
        File directory = new File(Environment.getExternalStorageDirectory(), path);
        if (!directory.exists())
            if (!directory.mkdirs()) {
                //Log.e(ApplicationController.getContext().getString(R.string.directory_creation_error));
                return null;
            }

        return new File(directory, fileName);
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private static Uri createDocFileAbovePie(String fileName, String path, String docType){
        ContentResolver resolver = ApplicationController.getContext().getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, docType);
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, path);
        return resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);
    }
}