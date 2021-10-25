package com.gaadi.neon.util;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.TaskStackBuilder;
import androidx.core.content.ContextCompat;
import androidx.core.os.EnvironmentCompat;

import com.scanlibrary.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Lakshay
 * @since 17-03-2015.
 *
 */

public class NeonUtils {

        public static void createNotification(Context context, int smallIcon, String title, String content, Intent resultIntent, int imageUploadNotifId) {
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(smallIcon)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(content);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(resultIntent);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(imageUploadNotifId, mBuilder.build());
    }

    public static String getStringSharedPreference(Context context, String key, String defaultValue) {
        SharedPreferences preferences = context.getApplicationContext().
                getSharedPreferences(
                        Constants.APP_SHARED_PREFERENCE,
                        Context.MODE_PRIVATE
                );

        if (preferences.contains(key)) {
            return preferences.getString(key, defaultValue);
        } else {
            return defaultValue;
        }
    }


    private String verifyFolder(File file) {
        File[] filesInFolder = file.listFiles();
        if (filesInFolder != null && filesInFolder.length > 0) {
            for (File file1 : filesInFolder) {
                if (file1.getName().contains("jpg") || file1.getName().contains("jpeg") || file1.getName().contains("png")) {
                    return file1.getAbsolutePath();
                }
            }
        }
        return "";
    }


    private ArrayList<FileInfo> getFolders() {

        //Directory Pictures
        File pathPictures = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //Log.e(Constants.TAG, "External Path :" + pathPictures.toString());
        ArrayList<FileInfo> files1 = getAllFoldersInfo(pathPictures);

        //Directory DCIM
        File pathDCIM = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        ArrayList<FileInfo> files2 = getAllFoldersInfo(pathDCIM);
        files1.addAll(files2);

        //SD-Card Mounted
        String secStore = System.getenv("SECONDARY_STORAGE");
        //Log.e(Constants.TAG, "Sec Store : "+secStore);
        try {
            if (secStore != null) {
                int index = secStore.indexOf(":");
                String externalStorage = "";
                if (index > 0) {
                    externalStorage = secStore.substring(0, index);

                } else {
                    externalStorage = secStore;
                }
                String externalStorageDCIM = externalStorage + "/DCIM";
                File externalFile = new File(externalStorageDCIM);

                ArrayList<FileInfo> externalFiles = getAllFoldersInfo(externalFile);
                files1.addAll(externalFiles);

            }

        } catch (Exception e) {
            //Log.e(Constants.TAG, e.getMessage());
        }

        //WhatsApp Images
        File pathWhatsApp = new File(Environment.getExternalStorageDirectory() + "/WhatsApp/Media/WhatsApp Images");
        Log.e(Constants.TAG, pathWhatsApp.getAbsolutePath());
        ArrayList<FileInfo> files3 = getAllFoldersInfo(pathWhatsApp);
        files1.addAll(files3);


        //Download Directory
        File pathDownload = new File(Environment.getExternalStorageDirectory() + "/Download");
        Log.e(Constants.TAG, "pathDownload : " + pathDownload.getName());

        ArrayList<FileInfo> files4 = getAllFoldersInfo(pathDownload);
        files1.addAll(files4);

        return files1;
    }

    private ArrayList<FileInfo> getAllFoldersInfo(File file) {

        ArrayList<FileInfo> allFiles = new ArrayList<FileInfo>();
        File[] contentPictures = file.listFiles();

        if ((contentPictures == null) || (contentPictures.length == 0)) {
            Log.e(Constants.TAG, "No Files found at the path mentioned");
        } else {
            Boolean makeSelfFolder = false;
            for (File folder : contentPictures) {
                if (!folder.isDirectory()) {
                    makeSelfFolder = true;
                }
                String valid = verifyFolder(folder);
                if (folder.getName().equals("Sent"))
                    continue;
                if (valid.length() > 0) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.setDisplayName(folder.getName());
                    fileInfo.setFileName(folder.getAbsolutePath());
                    File[] imagesInFolder = folder.listFiles();
                    if (imagesInFolder != null) {
                        if (imagesInFolder.length == 0) {
                            fileInfo.setFilePath(folder.getAbsolutePath());
                        } else {
                            fileInfo.setType(FileInfo.FILE_TYPE.FOLDER);
                            fileInfo.setFileCount(imagesInFolder.length);
                            fileInfo.setFilePath(imagesInFolder[imagesInFolder.length - 1].getAbsolutePath());
                        }
                        allFiles.add(fileInfo);
                    }
                }
            }
            if (makeSelfFolder) {
                FileInfo selfFolder = new FileInfo();
                selfFolder.setDisplayName(file.getName());
                selfFolder.setType(FileInfo.FILE_TYPE.FOLDER);
                selfFolder.setFileName(file.getAbsolutePath());
                selfFolder.setFilePath(contentPictures[contentPictures.length - 1].getAbsolutePath());
                allFiles.add(selfFolder);
            }
        }
        return allFiles;
    }



    public static ArrayList<FileInfo> removeFileInfo(ArrayList<FileInfo> source, FileInfo fileInfo) {
        for (FileInfo fileInfo1 : source) {
            if (fileInfo.getFilePath().equals(fileInfo1.getFilePath())) {
                source.remove(fileInfo);
                break;
            }
        }
        return source;
    }

    public static void removeFileInfo(ArrayList<String> source, ArrayList<FileInfo> removeFiles, Boolean flag) {
        if (source == null)
            return;
        for (FileInfo fileInfo : removeFiles) {
            if (source.contains(fileInfo.getFilePath())) {
                source.remove(fileInfo.getFilePath());
            }
        }
    }

    public static void addFileInfo(ArrayList<FileInfo> source, FileInfo fileInfo) {
        Boolean alreadyPresent = false;
        for (FileInfo fileInfo1 : source) {
            if (fileInfo.getFilePath().equals(fileInfo1.getFilePath())) {
                alreadyPresent = true;
                break;
            }
        }
        if (!alreadyPresent)
            source.add(fileInfo);
    }

    public static void removeFileInfo(ArrayList<FileInfo> source, String filePath) {
        for (FileInfo fileInfo : source) {
            if (filePath.equals(fileInfo.getFilePath())) {
                source.remove(fileInfo);
                break;
            }
        }
    }

    public static void removeFileInfo(ArrayList<FileInfo> source, ArrayList<FileInfo> fileInfos) {
        ArrayList<FileInfo> toBeDeleted = new ArrayList<>();
        for (FileInfo fileInfo : source) {
            for (FileInfo fileInfo1 : fileInfos) {
                if (fileInfo1.getFilePath().equals(fileInfo.getFilePath())) {
                    toBeDeleted.add(fileInfo);
                    break;
                }
            }
        }
        source.removeAll(toBeDeleted);
    }

    public static void expand(final View v) {
        v.measure(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final int targetHeight = v.getMeasuredHeight();

        v.getLayoutParams().height = 0;
        v.setVisibility(View.VISIBLE);
        Animation a = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        // 1dp/ms
        a.setDuration((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density));
        v.startAnimation(a);
    }


    public static Uri getImageStoreUri() {
        return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    public static boolean getBooleanSharedPreference(Context context, String key, boolean defaultValue) {
        SharedPreferences preferences = context.getApplicationContext().
                getSharedPreferences(
                        Constants.APP_SHARED_PREFERENCE,
                        Context.MODE_PRIVATE
                );

        if (preferences.contains(key)) {
            return preferences.getBoolean(key, defaultValue);
        } else {
            return defaultValue;
        }
    }

    public static void setBooleanSharedPreference(Context context, String key, boolean value) {
        SharedPreferences preferences = context.getApplicationContext().
                getSharedPreferences(
                        Constants.APP_SHARED_PREFERENCE,
                        Context.MODE_PRIVATE
                );
        SharedPreferences.Editor editor = preferences.edit();
        if ((key != null) && !key.isEmpty()) {
            editor.putBoolean(key, value);
            editor.apply();
        }
    }

    public static boolean checkForPermission(final Context context, final String[] permissions,
                                             final int requestCode, final String requestFor) {
        final ArrayList<String> permissionNeededForList = checkSelfPermission(context, permissions);
        String requestsFor = permissionNeededForList.get(permissionNeededForList.size() - 1);
        permissionNeededForList.remove(permissionNeededForList.size()-1);
        if (permissionNeededForList.isEmpty()) {
            return true;
        }
        if (!requestsFor.isEmpty()) {
            new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.permission_error))
                    .setMessage(context.getString(R.string.you_need_to_allow_access_to,
                            new String[]{requestFor, requestFor}))
                    .setPositiveButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            requestPermission(context,
                                    permissionNeededForList.toArray(new String[permissionNeededForList.size()]),
                                    requestCode);
                        }
                    })
                    .setNegativeButton(context.getString(R.string.go_to_app_info), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                            Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                            intent.setData(uri);
                            ((Activity) context).startActivityForResult(intent, 10);
                        }
                    })
                    .create().show();
        }

        requestPermission(context,
                permissionNeededForList.toArray(new String[permissionNeededForList.size()]),
                requestCode);

        return false;
    }

    private static ArrayList<String> checkSelfPermission(Context context, String[] permissions) {
        ArrayList<String> list = new ArrayList<>();
        StringBuilder requestsFor = new StringBuilder();
        for (String permission: permissions) {
            if (ActivityCompat.checkSelfPermission(context, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                list.add(permission);
                if (getBooleanSharedPreference(context, permission, false)) {
                    // Check if permission has been called previously, true if called previously
                    if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, permission)) {
                        if (requestsFor.length() > 0) {
                            requestsFor.append(", ");
                        }
                        requestsFor.append(permission.substring(permission.lastIndexOf(".") + 1));
                    }
                } else {
                    setBooleanSharedPreference(context, permission, true);
                }
            }
        }
        if (requestsFor.length() > 0)
            list.add(requestsFor.toString());
        else
            list.add("");
        return list;
    }

    private static void requestPermission(Context context, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions((Activity) context, permissions, requestCode);
    }

    public static int isFrontCameraAvailable() {
        Camera.CameraInfo ci = new Camera.CameraInfo();
        for (int i = 0 ; i < Camera.getNumberOfCameras(); i++) {
            Camera.getCameraInfo(i, ci);
            if (ci.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                return Camera.CameraInfo.CAMERA_FACING_FRONT;
            }
        }
        return Camera.CameraInfo.CAMERA_FACING_BACK; // No front-facing camera found
    }

    public static Bitmap doBrightness(Bitmap src, int value) {
        // image size
        int width = src.getWidth();
        int height = src.getHeight();
        // create output bitmap
        Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
        // color information
        int A, R, G, B;
        int pixel;

        // scan through all pixels
        for(int x = 0; x < width; ++x) {
            for(int y = 0; y < height; ++y) {
                // get pixel color
                pixel = src.getPixel(x, y);
                A = Color.alpha(pixel);
                R = Color.red(pixel);
                G = Color.green(pixel);
                B = Color.blue(pixel);

                // increase/decrease each channel
                R += value;
                if(R > 255) { R = 255; }
                else if(R < 0) { R = 0; }

                G += value;
                if(G > 255) { G = 255; }
                else if(G < 0) { G = 0; }

                B += value;
                if(B > 255) { B = 255; }
                else if(B < 0) { B = 0; }

                // apply new pixel color to output bitmap
                bmOut.setPixel(x, y, Color.argb(A, R, G, B));
            }
        }

        // return final image
        return bmOut;
    }

    public static File getEmptyStoragePath(Context ctx) {
        File mediaFile = null;
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
        String selectedPath = "";
        ArrayList<String> list = (ArrayList) getSdCardPaths(ctx, true);
        for (String path : list) {

            long freeBytes = new File(path).getFreeSpace();
            if (freeBytes > 5120) {
                selectedPath = path;
                break;
            }
        }
       /* File externalDir = new File(selectedPath , ctx.getString(R.string.app_name));
        if (!externalDir.exists()) {
            if (!externalDir.mkdir()) {
                //Toast.makeText(ctx,"FAILED externalDir.mkdir() TO CREATE DIRECTORY",Toast.LENGTH_SHORT).show();
                Log.d("MyCameraApp", "failed to create directory");
                return null;
            }
            else{
                //Toast.makeText(ctx,"SUCCESS to create folder",Toast.LENGTH_SHORT).show();
            }
        }

        mediaFile = new File(externalDir.getPath() + File.separator +
                "IMG_" + timeStamp + ".jpg");
        return mediaFile;*/
        return new File(selectedPath);
    }

    public static List<String> getSdCardPaths(final Context context, final boolean includePrimaryExternalStorage) {

//        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES).toString());

        Uri pictureFile = Constants.getMediaOutputFile(context,Constants.TYPE_IMAGE);
        if(pictureFile == null)
            return null;
        String path = FileUtils.getPath(context, pictureFile);

        final List<String> result = new ArrayList<>();
        /*if(!mediaStorageDir.exists()){
            if(!mediaStorageDir.mkdir()){
                Log.e("CommonUtils","Pictures Directory not found");
            }
            else{
                result.add(mediaStorageDir.getAbsolutePath());
            }
        } else{
            result.add(mediaStorageDir.getAbsolutePath());
        }*/
        result.add(path);
        final File[] externalCacheDirs = ContextCompat.getExternalFilesDirs(context,null);
        if (externalCacheDirs == null || externalCacheDirs.length == 0)
            return null;
        if (externalCacheDirs.length == 1) {
            if (externalCacheDirs[0] == null)
                return null;
            final String storageState = EnvironmentCompat.getStorageState(externalCacheDirs[0]);
            if (!Environment.MEDIA_MOUNTED.equals(storageState))
                return null;
            if (!includePrimaryExternalStorage && Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB && Environment.isExternalStorageEmulated())
                return null;
        }

        if (includePrimaryExternalStorage || externalCacheDirs.length == 1)
        {
            result.add(externalCacheDirs[0].getAbsolutePath());
            //result.add(getRootOfInnerSdCardFolder(externalCacheDirs[0]));
        }
        for (int i = 1; i < externalCacheDirs.length; ++i) {
            final File file = externalCacheDirs[i];
            if (file == null)
                continue;
            final String storageState = EnvironmentCompat.getStorageState(file);
            if (Environment.MEDIA_MOUNTED.equals(storageState)) {
                result.add(externalCacheDirs[i].getAbsolutePath());
                //  result.add(getRootOfInnerSdCardFolder(externalCacheDirs[i]));
            }
        }
        if (result.isEmpty())
            return null;
        return result;
    }
}
