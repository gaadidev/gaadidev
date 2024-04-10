package com.gaadi.neon.activity.gallery;

import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.gaadi.neon.activity.NeonBaseActivity;
import com.gaadi.neon.enumerations.Sorting_Type;
import com.gaadi.neon.model.BucketModel;
import com.gaadi.neon.util.FileInfo;
import com.gaadi.neon.util.NeonImagesHandler;
import com.scanlibrary.R;

import java.util.ArrayList;

/**
 * @author princebatra
 * @version 1.0
 * @since 25/1/17
 */
public abstract class NeonBaseGalleryActivity extends NeonBaseActivity {

    private Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    private ArrayList<BucketModel> buckets;

    protected ArrayList<BucketModel> getImageBuckets() {
        buckets = new ArrayList<>();

        String[] PROJECTION_BUCKET = {MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.BUCKET_ID, MediaStore.Images.ImageColumns.DATA};

        String orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";

        Cursor mCursor;
        if (NeonImagesHandler.getSingletonInstance().getGalleryParam() != null && NeonImagesHandler.getSingletonInstance().getGalleryParam().isRestrictedExtensionJpgPngEnabled()) {
            mCursor = getContentResolver().query(uri, PROJECTION_BUCKET, MediaStore.Images.Media.MIME_TYPE + " in (?, ?, ?, ?)", new String[]{"image/jpeg", "image/png","image/jpg", "image/heic"}, orderBy);
        } else {
            mCursor = getContentResolver().query(uri, PROJECTION_BUCKET, null, null, orderBy);
        }
        if (mCursor == null) {
            Toast.makeText(this, getString(R.string.gallery_error), Toast.LENGTH_SHORT).show();
            finish();
            return null;
        }
        mCursor.moveToFirst();


        Log.e("Anurag", mCursor.getCount()+"");
        if(mCursor.getCount() > 0){
            do {
                String bucketId = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID));

                int index = getBucketIndexWithId(bucketId);
                Log.e("Anurag", index +" bucketId = "+bucketId);
                if (index == -1) {
                    BucketModel bucketModel = new BucketModel();
                    bucketModel.setBucketId(bucketId);
                    bucketModel.setBucketName(mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)));
                    bucketModel.setFileCount(1);
                    bucketModel.setBucketCoverImagePath(mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
                    buckets.add(bucketModel);
                } else {
                    buckets.get(index).setFileCount(buckets.get(index).getFileCount() + 1);
                }
            }while (mCursor.moveToNext());
        }
        mCursor.close();

        return buckets;
    }

    protected ArrayList<BucketModel> getSortedImageBuckets(Sorting_Type sortingType) {
        buckets = new ArrayList<>();

        String[] PROJECTION_BUCKET = {MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.BUCKET_ID, MediaStore.Images.ImageColumns.DATA};

        String orderBy = MediaStore.Images.ImageColumns.DATE_ADDED + " DESC";

        switch (sortingType){
            case Date_Descending:{
                orderBy = MediaStore.Images.ImageColumns.DATE_ADDED + " DESC";
                break;
            }
            case Date_Ascending:{
                orderBy = MediaStore.Images.ImageColumns.DATE_ADDED + " ASC";
                break;
            }
            case Alphabetical_Descending:{
                orderBy = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " DESC";
                break;
            }
            case Alphabetical_Ascending:{
                orderBy = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " ASC";
                break;
            }
        }

        Cursor mCursor;
        if (NeonImagesHandler.getSingletonInstance().getGalleryParam() != null && NeonImagesHandler.getSingletonInstance().getGalleryParam().isRestrictedExtensionJpgPngEnabled()) {
            mCursor = getContentResolver().query(uri, PROJECTION_BUCKET, MediaStore.Images.Media.MIME_TYPE + " in (?, ?, ?, ?)", new String[]{"image/jpeg", "image/png","image/jpg", "image/heic"}, orderBy);
        } else {
            mCursor = getContentResolver().query(uri, PROJECTION_BUCKET, null, null, orderBy);
        }
        if (mCursor == null) {
            Toast.makeText(this, getString(R.string.gallery_error), Toast.LENGTH_SHORT).show();
            finish();
            return null;
        }
        mCursor.moveToFirst();


        if(mCursor.getCount() > 0){
            do {
                String bucketId = mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_ID));

                int index = getBucketIndexWithId(bucketId);
                if (index == -1) {
                    BucketModel bucketModel = new BucketModel();
                    bucketModel.setBucketId(bucketId);
                    bucketModel.setBucketName(mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME)));
                    bucketModel.setFileCount(1);
                    bucketModel.setBucketCoverImagePath(mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
                    buckets.add(bucketModel);
                } else {
                    buckets.get(index).setFileCount(buckets.get(index).getFileCount() + 1);
                }
            }while (mCursor.moveToNext());
        }
        mCursor.close();

        return buckets;
    }

    /**Pass bucketId if need all images from all buckets*/
    protected ArrayList<FileInfo> getFileFromBucketId(String bucketId) {
        ArrayList<FileInfo> fileInfos = new ArrayList<>();

        String[] PROJECTION_FILES = {MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.BUCKET_ID, MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DATE_TAKEN};

        String orderBy = MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC";

        String selection = MediaStore.Images.Media.BUCKET_ID + " =? and " + MediaStore.Images.Media.SIZE + " >? and "
                + MediaStore.Images.Media.MIME_TYPE + " in (?, ?, ?, ?)";
        String[] selectionArgs = new String[]{bucketId, String.valueOf(0), "image/jpeg", "image/png", "image/jpg", "image/heic"};
        if(bucketId == null){
            selection = null;
            selectionArgs = null;
        }
        Cursor mCursor = getContentResolver().query(uri, PROJECTION_FILES, selection, selectionArgs, orderBy);
        if (mCursor == null) {
            Toast.makeText(this, getString(R.string.gallery_error), Toast.LENGTH_SHORT).show();
            finish();
            return null;
        }
        mCursor.moveToFirst();

        if(mCursor.getCount()>0){
            do{
                FileInfo singleFileInfo = new FileInfo();
                singleFileInfo.setSource(FileInfo.SOURCE.PHONE_GALLERY);
                singleFileInfo.setFilePath(mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
                singleFileInfo.setDateTimeTaken(mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)));
                singleFileInfo.setType(FileInfo.FILE_TYPE.IMAGE);
                fileInfos.add(singleFileInfo);
            }while (mCursor.moveToNext());
        }
        mCursor.close();

        return fileInfos;
    }

    protected ArrayList<FileInfo> getSortedFilesFromBucketId(String bucketId, Sorting_Type sortingType) {
        ArrayList<FileInfo> fileInfos = new ArrayList<>();

        String[] PROJECTION_FILES = {MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                MediaStore.Images.ImageColumns.BUCKET_ID, MediaStore.Images.ImageColumns.DATA,
                MediaStore.Images.ImageColumns.DATE_TAKEN};

        String orderBy = MediaStore.Images.ImageColumns.DATE_ADDED + " DESC";

        switch (sortingType){
            case Date_Descending:{
                orderBy = MediaStore.Images.ImageColumns.DATE_ADDED + " DESC";
                break;
            }
            case Date_Ascending:{
                orderBy = MediaStore.Images.ImageColumns.DATE_ADDED + " ASC";
                break;
            }
            case Alphabetical_Descending:{
                orderBy = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " DESC";
                break;
            }
            case Alphabetical_Ascending:{
                orderBy = MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME + " ASC";
                break;
            }
        }

        String selection = MediaStore.Images.Media.BUCKET_ID + " =? and " + MediaStore.Images.Media.SIZE + " >? and "
                + MediaStore.Images.Media.MIME_TYPE + " in (?, ?, ?, ?)";
        String[] selectionArgs = new String[]{bucketId, String.valueOf(0), "image/jpeg", "image/png", "image/jpg", "image/heic"};
        if(bucketId == null){
            selection = null;
            selectionArgs = null;
        }
        Cursor mCursor = getContentResolver().query(uri, PROJECTION_FILES, selection, selectionArgs, orderBy);
        if (mCursor == null) {
            Toast.makeText(this, getString(R.string.gallery_error), Toast.LENGTH_SHORT).show();
            finish();
            return null;
        }
        mCursor.moveToFirst();

        if(mCursor.getCount()>0){
            do{
                FileInfo singleFileInfo = new FileInfo();
                singleFileInfo.setSource(FileInfo.SOURCE.PHONE_GALLERY);
                singleFileInfo.setFilePath(mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)));
                singleFileInfo.setDateTimeTaken(mCursor.getString(mCursor.getColumnIndex(MediaStore.Images.ImageColumns.DATE_TAKEN)));
                singleFileInfo.setType(FileInfo.FILE_TYPE.IMAGE);
                fileInfos.add(singleFileInfo);
            }while (mCursor.moveToNext());
        }
        mCursor.close();

        return fileInfos;
    }


    private int getBucketIndexWithId(String id) {
        if (buckets == null || buckets.size() <= 0) {
            return -1;
        }

        for (int i = 0; i < buckets.size(); i++) {
            if (buckets.get(i).getBucketId().equals(id)) {
                return i;
            }
        }
        return -1;
    }


}
