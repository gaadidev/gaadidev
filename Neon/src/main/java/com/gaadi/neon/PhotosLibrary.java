package com.gaadi.neon;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.gaadi.neon.activity.camera.NormalCameraActivityNeon;
import com.gaadi.neon.activity.gallery.GridFilesActivity;
import com.gaadi.neon.activity.gallery.GridFoldersActivity;
import com.gaadi.neon.activity.gallery.HorizontalFilesActivity;
import com.gaadi.neon.activity.neutral.NeonNeutralActivity;
import com.gaadi.neon.enumerations.LibraryMode;
import com.gaadi.neon.interfaces.ICameraParam;
import com.gaadi.neon.interfaces.IGalleryParam;
import com.gaadi.neon.interfaces.INeutralParam;
import com.gaadi.neon.interfaces.LivePhotosListener;
import com.gaadi.neon.interfaces.OnImageCollectionListener;
import com.gaadi.neon.model.PhotosMode;
import com.gaadi.neon.util.FileInfo;
import com.gaadi.neon.util.NeonException;
import com.gaadi.neon.util.NeonImagesHandler;

import java.util.List;

/**
 * @author lakshaygirdhar
 * @since 13-08-2016
 */
public class PhotosLibrary {

    public static void collectLivePhotos(int requestCode, LibraryMode libraryMode, final Context activity,
                                         final OnImageCollectionListener imageCollectionListener,
                                         final LivePhotosListener livePhotosListener, ICameraParam iCameraParam)
            throws NullPointerException, NeonException {
        try {
            if (livePhotosListener == null) {
                throw new NullPointerException("'LivePhotosListener' cannot be null");
            }
            NeonImagesHandler.getSingletonInstance().setLivePhotosListener(livePhotosListener);

            collectPhotos(requestCode, activity, libraryMode, PhotosMode.setCameraMode().setParams(iCameraParam), imageCollectionListener);

        } catch (NeonException e) {
            e.printStackTrace();
        }
    }


    public static void collectPhotos(int requestCode, Context activity, LibraryMode libraryMode,
                                     PhotosMode photosMode, OnImageCollectionListener listener)
            throws NullPointerException, NeonException {
        NeonImagesHandler.getSingletonInstance().setImageResultListener(listener);
        NeonImagesHandler.getSingletonInstance().setLibraryMode(libraryMode);
        NeonImagesHandler.getSingletonInstance().setRequestCode(requestCode);
        validate(activity, photosMode, listener);
        List<FileInfo> alreadyAddedImages = photosMode.getParams().getAlreadyAddedImages();
        if (alreadyAddedImages != null) {
            NeonImagesHandler.getSingletonInstance().setImagesCollection(alreadyAddedImages);
        }
        if (photosMode.getParams() instanceof INeutralParam) {
            startNeutralActivity(activity, photosMode);
        } else if (photosMode.getParams() instanceof ICameraParam) {
            startCameraActivity(activity, photosMode);
        } else if (photosMode.getParams() instanceof IGalleryParam) {
            startGalleryActivity(activity, photosMode);
        }
    }


    private static void validate(Context activity, PhotosMode photosMode, OnImageCollectionListener listener) throws NullPointerException, NeonException {
        if (activity == null) {
            throw new NullPointerException("Activity instance cannot be null");
        } else if (photosMode == null) {
            throw new NullPointerException("PhotosMode instance cannot be null");
        } else if ((photosMode.getParams().getTagEnabled()) &&
                (photosMode.getParams().getImageTagsModel() == null || photosMode.getParams().getImageTagsModel().size() <= 0)) {
            throw new NeonException("Tags enabled but list is empty or null");
        } else if (listener == null) {
            throw new NullPointerException("'OnImageCollectionListener' cannot be null");
        }
    }

    private static void startCameraActivity(Context activity, PhotosMode photosMode) {
        ICameraParam cameraParams = (ICameraParam) photosMode.getParams();
        if (cameraParams == null) {
            Toast.makeText(activity, "Camera param null", Toast.LENGTH_SHORT).show();
            return;
        }
        NeonImagesHandler.getSingletonInstance().setCameraParam(cameraParams);

        switch (cameraParams.getCameraViewType()) {

            case normal_camera:
            case gallery_preview_camera:
                Intent intent = new Intent(activity, NormalCameraActivityNeon.class);
                activity.startActivity(intent);
                break;

        }
    }

    private static void startGalleryActivity(Context activity, PhotosMode photosMode) {
        IGalleryParam galleryParams = (IGalleryParam) photosMode.getParams();
        if (galleryParams == null) {
            Toast.makeText(activity, "Gallery param null", Toast.LENGTH_SHORT).show();
            return;
        }
        NeonImagesHandler.getSingletonInstance().setGalleryParam(galleryParams);

        switch (galleryParams.getGalleryViewType()) {
            case Folder_Alphabetical_Sorted:
            case Grid_Structure:
                Intent gridGalleryIntent;
                if (galleryParams.enableFolderStructure()) {
                    gridGalleryIntent = new Intent(activity, GridFoldersActivity.class);
                } else {
                    gridGalleryIntent = new Intent(activity, GridFilesActivity.class);
                }
                activity.startActivity(gridGalleryIntent);
                break;

            case Horizontal_Structure:
                Intent horizontalGalleryIntent;
                if (galleryParams.enableFolderStructure()) {
                    horizontalGalleryIntent = new Intent(activity, GridFoldersActivity.class);
                } else {
                    horizontalGalleryIntent = new Intent(activity, HorizontalFilesActivity.class);
                }
                activity.startActivity(horizontalGalleryIntent);
                break;

        }
    }

    private static void startNeutralActivity(Context activity, PhotosMode photosMode) {
        NeonImagesHandler.getSingletonInstance().setNeutralEnabled(true);

        INeutralParam neutralParamParams = (INeutralParam) photosMode.getParams();
        if (neutralParamParams == null) {
            Toast.makeText(activity, "Neutral param null", Toast.LENGTH_SHORT).show();
            return;
        }
        NeonImagesHandler.getSingletonInstance().setNeutralParam(neutralParamParams);

        Intent neutralIntent = new Intent(activity, NeonNeutralActivity.class);
        activity.startActivity(neutralIntent);

    }
}
