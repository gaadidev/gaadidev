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

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            File file = createDocFileLegacy(path, displayName);
            return Uri.fromFile(file);
        } else {
            return createDocFileAbovePie(context, path,  displayName, Constants.PIC_MIME_TYPE);
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
    private static Uri createDocFileAbovePie(Context context, String path, String displayName, String docType){
        ContentResolver resolver = context.getContentResolver();
        ContentValues contentValues = new ContentValues();
        contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, path);
        contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, displayName);
        contentValues.put(MediaStore.MediaColumns.MIME_TYPE, docType);
        return resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
    }
    public static String getPath (Context context, Uri uri) {
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

    }

    public static File getImageFilePath(Context context, Uri uri) throws IOException {
        context.grantUriPermission(context.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        String fileName = getFileName(context, uri);
        File file = File.createTempFile((fileName != null && !fileName.isEmpty()) ? fileName : "" + UUID.randomUUID().toString(), ".jpg");
        file.deleteOnExit();
        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        FileOutputStream out = new FileOutputStream(file);
        if (inputStream != null) {
            copyStream(inputStream, out);
            out.close();
        }
        return file;
    }

    public static String getFileName(Context context, Uri uri) {
        String result = "";
        if (uri.getScheme() != null && uri.getScheme().contains("content")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            try {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } finally {
                if (cursor != null)
                    cursor.close();
            }
        }
        if (result == null) {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1) {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }

    public static void copyStream(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[4096];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }
}