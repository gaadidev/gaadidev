package com.gaadi.neon.util;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class FileUtils {

    private final String TAG = FileUtils.class.getSimpleName();
    public static final String MEDIA_STORAGE_PATH = Environment.DIRECTORY_DCIM + File.separator + "NEON";

    public static Uri createDocFileName(Context context, String path, String displayName){

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            return createDocFileAboveQ(context, path,  displayName, Constants.PIC_MIME_TYPE);
        } else {
            File file = createDocFileLegacy(path, displayName);
            return Uri.fromFile(file);
        }
    }
    private static File createDocFileLegacy(String path, String displayName){
        File directory = new File(Environment.getExternalStorageDirectory(), path);
        if (!directory.exists())
            if (!directory.mkdirs()) {
                //Log.e(ApplicationController.getContext().getString(R.string.directory_creation_error));
                return null;
            }

        return new File(directory, displayName);
    }

    @TargetApi(Build.VERSION_CODES.Q)
    private static Uri createDocFileAboveQ(Context context, String path, String displayName, String docType){
        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, path);
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, docType);
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }
    public static String getPath (Context context, Uri uri) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            try {
                context.grantUriPermission(context.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                String[] projection = {MediaStore.MediaColumns.DATA};
                Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                cursor.moveToFirst();
                return cursor.getString(column_index);
            } catch (Exception e){
                return "";
            }
        } else {
            return uri.getPath();
        }
    }
}