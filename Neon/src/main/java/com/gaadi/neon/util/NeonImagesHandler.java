package com.gaadi.neon.util;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.gaadi.neon.enumerations.LibraryMode;
import com.gaadi.neon.enumerations.ResponseCode;
import com.gaadi.neon.interfaces.ICameraParam;
import com.gaadi.neon.interfaces.IGalleryParam;
import com.gaadi.neon.interfaces.INeutralParam;
import com.gaadi.neon.interfaces.IParam;
import com.gaadi.neon.interfaces.LivePhotoNextTagListener;
import com.gaadi.neon.interfaces.LivePhotosListener;
import com.gaadi.neon.interfaces.OnImageCollectionListener;
import com.gaadi.neon.model.ImageTagModel;
import com.gaadi.neon.model.NeonResponse;
import com.scanlibrary.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author princebatra
 * @version 1.0
 * @since 1/2/17
 */
public class NeonImagesHandler {

    private static NeonImagesHandler singleonInstance;
    private boolean clearInstance;
    private List<FileInfo> imagesCollection;
    private ICameraParam cameraParam;
    private IGalleryParam galleryParam;
    private boolean neutralEnabled;
    private INeutralParam neutralParam;
    private OnImageCollectionListener imageResultListener;
    private LivePhotosListener livePhotosListener;
    private LivePhotoNextTagListener livePhotoNextTagListener;
    private String currentTag = "";
    private LibraryMode libraryMode;
    private int requestCode;

    private NeonImagesHandler() {
    }

    @Deprecated
    public synchronized static NeonImagesHandler getSingleonInstance() {
        return getSingletonInstance();
    }

    public synchronized static NeonImagesHandler getSingletonInstance() {
        if (singleonInstance == null || singleonInstance.clearInstance) {
            singleonInstance = new NeonImagesHandler();
            singleonInstance.clearInstance = false;
        }
        return singleonInstance;
    }


    public void scheduleSingletonClearance() {
        clearInstance = true;
      /*  new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                getSingleonInstance();
            }
        }, 10000);*/
    }

    public int getNumberOfPhotosCollected(ImageTagModel imageTagModel) {
        int count = 0;
        if (imagesCollection != null && imagesCollection.size() > 0) {
            for (FileInfo fileInfo : imagesCollection) {
                if (fileInfo.getFileTag() == null) {
                    continue;
                }
                if (fileInfo.getFileTag().getTagId().equals(imageTagModel.getTagId())) {
                    count++;
                }
            }
        }
        return count;
    }

    public OnImageCollectionListener getImageResultListener() {
        return imageResultListener;
    }

    public void setImageResultListener(OnImageCollectionListener imageResultListener) {
        this.imageResultListener = imageResultListener;
    }


    public LivePhotosListener getLivePhotosListener() {
        return livePhotosListener;
    }

    public void setLivePhotosListener(LivePhotosListener livePhotosListener) {
        this.livePhotosListener = livePhotosListener;
    }

    public LivePhotoNextTagListener getLivePhotoNextTagListener() {
        return livePhotoNextTagListener;
    }

    public void setLivePhotoNextTagListener(LivePhotoNextTagListener livePhotoNextTagListener) {
        this.livePhotoNextTagListener = livePhotoNextTagListener;
    }


    public String getCurrentTag() {
        return currentTag;
    }

    public void setCurrentTag(String currentTag) {
        this.currentTag = currentTag;
    }


    public IParam getGenericParam() {
        if (galleryParam != null)
            return galleryParam;
        else if (cameraParam != null)
            return cameraParam;
        else
            return neutralParam;
    }

    public boolean isNeutralEnabled() {
        return neutralEnabled;
    }

    public void setNeutralEnabled(boolean neutralEnabled) {
        this.neutralEnabled = neutralEnabled;
    }

    public INeutralParam getNeutralParam() {
        return neutralParam;
    }

    public void setNeutralParam(INeutralParam neutralParam) {
        this.neutralParam = neutralParam;
    }

    public List<FileInfo> getImagesCollection() {
        return imagesCollection;
    }

    public void setImagesCollection(List<FileInfo> alreadyAddedList) {
        imagesCollection = new ArrayList<>();
        if (alreadyAddedList != null && alreadyAddedList.size() > 0) {
            for (int i = 0; i < alreadyAddedList.size(); i++) {
                FileInfo cloneFile = new FileInfo();
                FileInfo originalFile = alreadyAddedList.get(i);
                if (originalFile == null) {
                    continue;
                }
                if (originalFile.getFileTag() != null) {
                    cloneFile.setFileTag(new ImageTagModel(originalFile.getFileTag().getTagName(), originalFile.getFileTag().getTagId(), originalFile.getFileTag().isMandatory(), originalFile.getFileTag().getNumberOfPhotos()));
                }
                cloneFile.setSelected(originalFile.getSelected());
                cloneFile.setSource(originalFile.getSource());
                cloneFile.setFileName(originalFile.getFileName());
                cloneFile.setDateTimeTaken(originalFile.getDateTimeTaken());
                cloneFile.setDisplayName(originalFile.getDisplayName());
                cloneFile.setFileCount(originalFile.getFileCount());
                cloneFile.setFilePath(originalFile.getFilePath());
                cloneFile.setType(originalFile.getType());
                imagesCollection.add(cloneFile);
            }
        }
    }

    public boolean checkImagesAvailableForTag(ImageTagModel tagModel) {
        if (imagesCollection == null || imagesCollection.size() <= 0) {
            return false;
        }
        for (int i = 0; i < imagesCollection.size(); i++) {
            if (imagesCollection.get(i).getFileTag() != null && imagesCollection.get(i).getFileTag().getTagId().equals(tagModel.getTagId()) &&
                    imagesCollection.get(i).getFileTag().getTagName().equals(tagModel.getTagName())) {
                return true;
            }
        }
        return false;
    }

    public List<ImageTagModel> getImageRequiredTags() {
        List<ImageTagModel> imageTagList = getNeutralParam().getImageTagsModel();
        List<ImageTagModel> imageRequiredTagList = new ArrayList<>();
        for (ImageTagModel itm : imageTagList) {
            if (getNumberOfPhotosCollected(itm) < itm.getNumberOfPhotos()) {
                imageRequiredTagList.add(itm);
            }
        }
        return imageRequiredTagList;
    }

    public boolean checkImageAvailableForPath(FileInfo fileInfo) {
        if (imagesCollection == null || imagesCollection.size() <= 0) {
            return false;
        }
        for (int i = 0; i < imagesCollection.size(); i++) {
            if (imagesCollection.get(i).getFilePath().equalsIgnoreCase(fileInfo.getFilePath())) {
                return true;
            }
        }
        return false;
    }

    public boolean removeFromCollection(FileInfo fileInfo) {
        if (imagesCollection == null || imagesCollection.size() <= 0) {
            return true;
        }
        for (int i = 0; i < imagesCollection.size(); i++) {
            if (imagesCollection.get(i).getFilePath().equals(fileInfo.getFilePath())) {
                return imagesCollection.remove(i) != null;
            }
        }
        return true;
    }

    public boolean putInImageCollection(FileInfo fileInfo, Context context) {
        if (imagesCollection == null) {
            imagesCollection = new ArrayList<>();
        }

        if (getGenericParam() != null && !getGenericParam().getTagEnabled()) {
            if (getGenericParam().getNumberOfPhotos() > 0 &&
                    getImagesCollection() != null &&
                    getGenericParam().getNumberOfPhotos() == getImagesCollection().size()) {
                Toast.makeText(context, context.getString(R.string.max_count_error, getGenericParam().getNumberOfPhotos()), Toast.LENGTH_SHORT).show();
                return false;
            }
        } else {
            ImageTagModel imageTagModel = fileInfo.getFileTag();
            if (imageTagModel != null && imageTagModel.getNumberOfPhotos() > 0 &&
                    getNumberOfPhotosCollected(imageTagModel) >= imageTagModel.getNumberOfPhotos()) {
                Toast.makeText(context, context.getString(R.string.max_tag_count_error, imageTagModel.getNumberOfPhotos()) + imageTagModel.getTagName(), Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        imagesCollection.add(fileInfo);
        return true;
    }

    public boolean removeFromCollection(int position) {
        return imagesCollection == null || imagesCollection.size() <= 0 || imagesCollection.remove(position) != null;
    }

    private HashMap<String, List<FileInfo>> getFileHashMap() {
        if (imagesCollection == null || imagesCollection.size() <= 0) {
            return null;
        }
        HashMap<String, List<FileInfo>> hashMap = new HashMap<>();

        for (int i = 0; i < imagesCollection.size(); i++) {
            FileInfo singleFile = imagesCollection.get(i);
            if (singleFile.getFileTag() == null) {
                continue;
            }
            if (hashMap.containsKey(singleFile.getFileTag().getTagId())) {
                hashMap.get(singleFile.getFileTag().getTagId()).add(singleFile);
            } else {
                List<FileInfo> singleTagFiles = new ArrayList<>();
                singleTagFiles.add(singleFile);
                hashMap.put(singleFile.getFileTag().getTagId(), singleTagFiles);
            }
        }
        return hashMap;
    }

    public IGalleryParam getGalleryParam() {
        return galleryParam;
    }

    public void setGalleryParam(IGalleryParam params) {
        this.galleryParam = params;
    }

    public ICameraParam getCameraParam() {
        return cameraParam;
    }

    public void setCameraParam(ICameraParam cameraParam) {
        this.cameraParam = cameraParam;
    }

    public void sendImageCollectionAndFinish(Activity activity, ResponseCode responseCode) {
        NeonResponse neonResponse = new NeonResponse();
        neonResponse.setRequestCode(getRequestCode());
        neonResponse.setResponseCode(responseCode);
        neonResponse.setImageCollection(NeonImagesHandler.getSingletonInstance().getImagesCollection());
        neonResponse.setImageTagsCollection(NeonImagesHandler.getSingletonInstance().getFileHashMap());
        if (NeonImagesHandler.getSingletonInstance() != null) {
            if (NeonImagesHandler.getSingletonInstance().getImageResultListener() != null) {
                NeonImagesHandler.getSingletonInstance().getImageResultListener().imageCollection(neonResponse);
                NeonImagesHandler.getSingletonInstance().scheduleSingletonClearance();
            }
        }
        activity.finish();
    }

    public void showBackOperationAlertIfNeeded(final Activity activity) {
        if (validateNeonExit(null)) {
            sendImageCollectionAndFinish(activity, ResponseCode.Back);
        } else {
            if (NeonImagesHandler.getSingleonInstance().getLibraryMode() == LibraryMode.Restrict) {
                showExitConfirmation(activity);
            } else {
                sendImageCollectionAndFinish(activity, ResponseCode.Back);
            }
        }


    }

    private void showExitConfirmation(final Activity activity) {
        new AlertDialog.Builder(activity).setTitle("Are you sure want to go back?")
                .setCancelable(true).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                sendImageCollectionAndFinish(activity, ResponseCode.Back);
            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    public void showBackOperationAlertIfNeededLive(final Activity activity) {
        if (NeonImagesHandler.getSingletonInstance().getLibraryMode() == LibraryMode.Restrict) {
            if (!validateNeonExit(null)) {
                new AlertDialog.Builder(activity).setTitle("Please upload " + NeonImagesHandler.getSingletonInstance().getCurrentTag() + " Photo")
                        .setCancelable(true).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
            } else {
                sendImageCollectionAndFinish(activity, ResponseCode.Back);
            }
        } else {

            if (!validateNeonExit(null)) {
                showExitConfirmation(activity);
            }

            showBackOperationAlertIfNeeded(activity);
        }
    }


    public boolean validateNeonExit(Activity activity) {
        try {
            if (NeonImagesHandler.getSingletonInstance() != null &&
                    NeonImagesHandler.getSingletonInstance().getGenericParam() != null &&
                    !NeonImagesHandler.getSingletonInstance().getGenericParam().getTagEnabled()) {
                return true;
            }
            List<FileInfo> fileInfos = NeonImagesHandler.getSingletonInstance().getImagesCollection();
            if (fileInfos != null && fileInfos.size() > 0) {
                for (int i = 0; i < fileInfos.size(); i++) {
                    if (fileInfos.get(i).getFileTag() == null) {
                        if (activity != null) {
                            Toast.makeText(activity, "Set tag for all images", Toast.LENGTH_SHORT).show();
                        }
                        return false;
                    }
                }
            }

            List<ImageTagModel> imageTagModels = NeonImagesHandler.getSingletonInstance().getGenericParam().getImageTagsModel();
            for (int j = 0; j < imageTagModels.size(); j++) {
                if (!imageTagModels.get(j).isMandatory()) {
                    continue;
                }
                if (!NeonImagesHandler.getSingletonInstance().checkImagesAvailableForTag(imageTagModels.get(j))) {
                    if (activity != null) {
                        Toast.makeText(activity, imageTagModels.get(j).getTagName() + " tag not covered.", Toast.LENGTH_SHORT).show();
                    }
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return true;
    }


    public LibraryMode getLibraryMode() {
        return libraryMode;
    }

    public void setLibraryMode(LibraryMode libraryMode) {
        this.libraryMode = libraryMode;
    }

    public int getRequestCode() {
        return requestCode;
    }

    public void setRequestCode(int requestCode) {
        this.requestCode = requestCode;
    }

}
