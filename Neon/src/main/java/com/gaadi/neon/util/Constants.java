package com.gaadi.neon.util;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.scanlibrary.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Lakshay
 *
 * @since 13-02-2015.
 */
public class Constants {
    public static final String Gallery_Params = "Gallery_Params";
    public static final String Camera_Params = "Camera_Params";
    public static final String BucketName = "BucketName";
    public static final String BucketId = "BucketId";

    public static final int destroyPreviousActivity = 300;

    public static final int TYPE_IMAGE = 1;
    public static final String APP_SHARED_PREFERENCE = "com.gcloud.gaadi.prefs";
    public static final String TAG = "Gallery";
    public static final String RESULT_IMAGES = "result_images";


    public static final String IMAGES_SELECTED = "imagesSelected";
    public static final String IMAGE_PATH = "image_path";
    public static final int REQUEST_PERMISSION_CAMERA = 104;
    public static final int REQUEST_PERMISSION_READ_EXTERNAL_STORAGE = 105;
    public static final String FLASH_MODE = "flashMode";
    public static final String IMAGE_TAGS_FOR_REVIEW = "imageTagsReview";
    public static final String IMAGE_MODEL_FOR__REVIEW = "imageModelReview";
    public static final String IMAGE_REVIEW_POSITION = "imageReviewPosition";
    public static final String SINGLE_TAG_SELECTION = "singleTagSelection";
    public static final String ALREADY_SELECTED_TAGS = "alreadySelectedTags";
    public static String FLAG = "Flag";
    public static String PIC_MIME_TYPE = "image/jpeg";

    public static Uri getMediaOutputFile(Context context, int type) {
        String appName = context.getString(R.string.app_name);
        if (appName.length() > 0) {
            appName = appName.replace(" ", "");
        }

        String fileName;
        Uri mediaFile;
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + appName;
            // Create the storage directory if it does not exist

            File mediaStorageDir = new File(fileName);
            if (!mediaStorageDir.exists()) {
                if (!mediaStorageDir.mkdirs()) {
                    Log.d("MyCameraApp", "failed to create directory");
                }
            }
            //String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss.SSS").format(new Date());
            mediaFile = Uri.fromFile(new File(mediaStorageDir.getPath() + File.separator +
                    "IMG_" + System.currentTimeMillis() + ".jpg"));

        } else {
            fileName = Environment.DIRECTORY_PICTURES + File.separator + appName;
            mediaFile =  FileUtils.createDocFileName(context, fileName,  "IMG_" + System.currentTimeMillis() + ".jpg");
        }
        return mediaFile;
    }
}
