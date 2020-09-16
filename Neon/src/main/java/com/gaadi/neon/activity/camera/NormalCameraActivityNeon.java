package com.gaadi.neon.activity.camera;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.gaadi.neon.PhotosLibrary;
import com.gaadi.neon.activity.ImageShow;
import com.gaadi.neon.enumerations.CameraType;
import com.gaadi.neon.enumerations.GalleryType;
import com.gaadi.neon.enumerations.ResponseCode;
import com.gaadi.neon.fragment.CameraFragment1;
import com.gaadi.neon.interfaces.ICameraParam;
import com.gaadi.neon.interfaces.IGalleryParam;
import com.gaadi.neon.interfaces.LivePhotoNextTagListener;
import com.gaadi.neon.interfaces.OnPermissionResultListener;
import com.gaadi.neon.model.ImageTagModel;
import com.gaadi.neon.model.PhotosMode;
import com.gaadi.neon.util.AnimationUtils;
import com.gaadi.neon.util.Constants;
import com.gaadi.neon.util.CustomParameters;
import com.gaadi.neon.util.ExifInterfaceHandling;
import com.gaadi.neon.util.FileInfo;
import com.gaadi.neon.util.LocationHelper;
import com.gaadi.neon.util.ManifestPermission;
import com.gaadi.neon.util.NeonException;
import com.gaadi.neon.util.NeonImagesHandler;
import com.gaadi.neon.util.NeonUtils;
import com.gaadi.neon.util.PermissionType;
import com.google.android.gms.location.LocationListener;
import com.intsig.csopen.sdk.CSOpenAPI;
import com.intsig.csopen.sdk.CSOpenApiFactory;
import com.intsig.csopen.sdk.CSOpenApiHandler;
import com.scanlibrary.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * @author princebatra
 * @version 1.0
 * @since 25/1/17
 */
public class NormalCameraActivityNeon extends NeonBaseCameraActivity implements CameraFragment1.SetOnPictureTaken
        , LivePhotoNextTagListener, View.OnClickListener, LocationListener {

    ICameraParam cameraParams;
    RelativeLayout tagsLayout, previewLayout;
    List<ImageTagModel> tagModels;
    int currentTag;
    private TextView tvTag, tvNext, tvPrevious, buttonDone, previewTitle;
    private ImageView buttonGallery, showTagPreview, imagePreview, ivPreviewDone, ivPreviewCancel;
    private Location location;
    private LocationHelper locationTracker;
    private LinearLayout imageHolderView;
    private final int REQ_CODE_CALL_CAMSCANNER = 168;
    private String mOutputImagePath;
    private String mInputImagePath;
    private CSOpenAPI camScannerApi;
    private boolean isPreviewVisible;
    private String filePathToReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.normal_camera_activity_layout);
        tagsLayout = findViewById(R.id.rlTags);
        tvTag = findViewById(R.id.tvTag);
        tvNext = findViewById(R.id.tvSkip);
        tvPrevious = findViewById(R.id.tvPrev);
        buttonGallery = findViewById(R.id.buttonGallery);
        buttonDone = findViewById(R.id.buttonDone);
        imageHolderView = findViewById(R.id.imageHolderView);
        showTagPreview = findViewById(R.id.tag_preview);
        previewLayout = findViewById(R.id.rl_image_preview);
        imagePreview = findViewById(R.id.iv_review);
        ivPreviewCancel = findViewById(R.id.iv_preview_cancel);
        ivPreviewDone = findViewById(R.id.iv_preview_done);
        previewTitle = findViewById(R.id.tv_preview_title);
        ivPreviewDone.setOnClickListener(this);
        ivPreviewCancel.setOnClickListener(this);
        buttonDone.setOnClickListener(this);
        buttonGallery.setOnClickListener(this);
        tvNext.setOnClickListener(this);
        tvPrevious.setOnClickListener(this);

        cameraParams = NeonImagesHandler.getSingletonInstance().getCameraParam();
        if (NeonImagesHandler.getSingletonInstance().getLivePhotosListener() != null) {
            buttonDone.setVisibility(View.INVISIBLE);
        } else {
            buttonDone.setVisibility(View.VISIBLE);
        }
        customize();
        bindCameraFragment();

        locationTracker = new LocationHelper(NormalCameraActivityNeon.this);
        locationTracker.setLocationListener(this);

        if(cameraParams != null && cameraParams.getCustomParameters() != null && cameraParams.getCustomParameters().getCamScannerAPIKey() != null && !cameraParams.getCustomParameters().getCamScannerAPIKey().equals("")){
            camScannerApi = CSOpenApiFactory.createCSOpenApi(this, cameraParams.getCustomParameters().getCamScannerAPIKey(), null);
        }

        if (NeonImagesHandler.getSingletonInstance().getLivePhotosListener() != null) {
            NeonImagesHandler.getSingletonInstance().setLivePhotoNextTagListener(this);
        }
        if (cameraParams == null || cameraParams.getCustomParameters() == null || cameraParams.getCustomParameters().getLocationRestrictive()) {
            locationTracker.getLocation();
        }
        showTagImages();
    }


    public void showTagImages() {
        if (tagModels != null && tagModels.size() != 0) {
            ImageTagModel imageTagModel = tagModels.get(currentTag);
            if ((cameraParams != null && cameraParams.getCustomParameters() != null) && cameraParams.getCustomParameters().showTagImage()) {
                if (!TextUtils.isEmpty(imageTagModel.getTagPreviewUrl())) {
                    showTagPreview.setVisibility(View.VISIBLE);
                    RequestOptions options = new RequestOptions()
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop()
                            .placeholder(R.drawable.default_placeholder);
                    Glide.with(this).load(imageTagModel.getTagPreviewUrl())
                            .apply(options)
                            .transition(withCrossFade())
                            .into(showTagPreview);
                } else {
                    showTagPreview.setVisibility(View.GONE);
                }
            }
        }
    }

    private void bindCameraFragment() {
        try {
            askForPermissionIfNeeded(PermissionType.write_external_storage, new OnPermissionResultListener() {
                @Override
                public void onResult(boolean permissionGranted) {
                    if (permissionGranted) {
                        try {
                            askForPermissionIfNeeded(PermissionType.camera, new OnPermissionResultListener() {
                                @Override
                                public void onResult(boolean permissionGranted) {
                                    if (permissionGranted) {
                                        new Handler().post(new Runnable() {
                                            @Override
                                            public void run() {
                                                try {
                                                    boolean locationRestrictive = true;
                                                    if (cameraParams != null && cameraParams.getCustomParameters() != null) {
                                                        locationRestrictive = cameraParams.getCustomParameters().getLocationRestrictive();
                                                    }

                                                    CameraFragment1 fragment = CameraFragment1.getInstance(locationRestrictive);
                                                    FragmentManager manager = getSupportFragmentManager();
                                                    manager.beginTransaction().replace(R.id.content_frame, fragment).commit();
                                                } catch (Exception e) {
                                                    e.printStackTrace();
                                                }
                                            }
                                        });

                                    } else {
                                        if (NeonImagesHandler.getSingletonInstance().isNeutralEnabled()) {
                                            finish();
                                        } else {
                                            NeonImagesHandler.getSingletonInstance().sendImageCollectionAndFinish(NormalCameraActivityNeon.this,
                                                    ResponseCode.Camera_Permission_Error);
                                        }
                                        Toast.makeText(NormalCameraActivityNeon.this, R.string.permission_error, Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } catch (ManifestPermission manifestPermission) {
                            manifestPermission.printStackTrace();
                        }
                    } else {
                        if (NeonImagesHandler.getSingletonInstance().isNeutralEnabled()) {
                            finish();
                        } else {
                            NeonImagesHandler.getSingletonInstance().sendImageCollectionAndFinish(NormalCameraActivityNeon.this,
                                    ResponseCode.Write_Permission_Error);
                        }
                        Toast.makeText(NormalCameraActivityNeon.this, R.string.permission_error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (ManifestPermission manifestPermission) {
            manifestPermission.printStackTrace();
        }
    }

    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.buttonDone) {
            try {
                if (!NeonImagesHandler.getSingletonInstance().isNeutralEnabled()) {
                    if (NeonImagesHandler.getSingletonInstance().getCameraParam() != null) {
                        if (NeonImagesHandler.getSingletonInstance().getCameraParam().enableImageEditing()
                                || NeonImagesHandler.getSingletonInstance().getCameraParam().getTagEnabled()) {
                            Intent intent = new Intent(this, ImageShow.class);
                            startActivity(intent);
                            finish();
                        } else {
                            if (NeonImagesHandler.getSingletonInstance().validateNeonExit(this)) {
                                NeonImagesHandler.getSingletonInstance().sendImageCollectionAndFinish(this, ResponseCode.Success);
                                finish();
                            }
                        }
                    }

                } else {
                    setResult(RESULT_OK);
                    finish();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else if (id == R.id.buttonGallery) {
            try {
                IGalleryParam galleryParam = NeonImagesHandler.getSingletonInstance().getGalleryParam();
                if (galleryParam == null) {
                    galleryParam = new IGalleryParam() {
                        @Override
                        public boolean selectVideos() {
                            return false;
                        }

                        @Override
                        public GalleryType getGalleryViewType() {
                            return GalleryType.Grid_Structure;
                        }

                        @Override
                        public boolean enableFolderStructure() {
                            return true;
                        }

                        @Override
                        public boolean galleryToCameraSwitchEnabled() {
                            return true;
                        }

                        @Override
                        public boolean isRestrictedExtensionJpgPngEnabled() {
                            return true;
                        }

                        @Override
                        public int getNumberOfPhotos() {
                            return NeonImagesHandler.getSingletonInstance().getCameraParam().getNumberOfPhotos();
                        }

                        @Override
                        public boolean getTagEnabled() {
                            return NeonImagesHandler.getSingletonInstance().getCameraParam().getTagEnabled();
                        }

                        @Override
                        public List<ImageTagModel> getImageTagsModel() {
                            return NeonImagesHandler.getSingletonInstance().getCameraParam().getImageTagsModel();
                        }

                        @Override
                        public ArrayList<FileInfo> getAlreadyAddedImages() {
                            return null;
                        }

                        @Override
                        public boolean enableImageEditing() {
                            return NeonImagesHandler.getSingletonInstance().getCameraParam().enableImageEditing();
                        }

                        @Override
                        public CustomParameters getCustomParameters() {
                            return NeonImagesHandler.getSingletonInstance().getCameraParam().getCustomParameters();
                        }

                    };
                }
                PhotosLibrary.collectPhotos(NeonImagesHandler.getSingletonInstance().getRequestCode(), this, NeonImagesHandler.getSingletonInstance().getLibraryMode(), PhotosMode.setGalleryMode().setParams(galleryParam), NeonImagesHandler.getSingleonInstance().getImageResultListener());
                finish();
            } catch (NeonException e) {
                e.printStackTrace();
            }
        } else if (id == R.id.tvSkip) {
            if (currentTag == tagModels.size() - 1) {
                if (NeonImagesHandler.getSingletonInstance().getLivePhotosListener() != null) {
                    if (NeonImagesHandler.getSingletonInstance().validateNeonExit(this)) {
                        NeonImagesHandler.getSingletonInstance().sendImageCollectionAndFinish(this, ResponseCode.Success);
                    }
                } else {
                    onClick(buttonDone);
                }

            } else {
                setTag(getNextTag(), true);
                showTagImages();
            }
        } else if (id == R.id.tvPrev) {
            setTag(getPreviousTag(), false);
            showTagImages();
        } else if(id == R.id.iv_preview_cancel){
            isPreviewVisible = false;
            previewLayout.setVisibility(View.GONE);
            NeonUtils.deleteFile(NormalCameraActivityNeon.this, filePathToReview);
        } else if(id == R.id.iv_preview_done){
            isPreviewVisible = false;
            previewLayout.setVisibility(View.GONE);
            afterPictureTaken(filePathToReview);
        }
    }

    private boolean finishValidation() {
        if (NeonImagesHandler.getSingleonInstance().getCameraParam().getTagEnabled()) {
            for (int i = 0; i < tagModels.size(); i++) {
                if (tagModels.get(i).isMandatory() &&
                        !NeonImagesHandler.getSingleonInstance().checkImagesAvailableForTag(tagModels.get(i))) {
                    Toast.makeText(this, String.format(getString(R.string.tag_mandatory_error), tagModels.get(i).getTagName()),
                            Toast.LENGTH_SHORT).show();
                    return false;
                }
            }
        } else {
            if (NeonImagesHandler.getSingleonInstance().getImagesCollection() == null ||
                    NeonImagesHandler.getSingleonInstance().getImagesCollection().size() <= 0) {
                Toast.makeText(this, R.string.no_images, Toast.LENGTH_SHORT).show();
                return false;
            } else if (NeonImagesHandler.getSingleonInstance().getImagesCollection().size() <
                    NeonImagesHandler.getSingleonInstance().getCameraParam().getNumberOfPhotos()) {
               /* Toast.makeText(this, NeonImagesHandler.getSingleonInstance().getCameraParam().getNumberOfPhotos() -
                        NeonImagesHandler.getSingleonInstance().getImagesCollection().size() + " more image required", Toast.LENGTH_SHORT).show();
                */
                Toast.makeText(this, getString(R.string.more_images, NeonImagesHandler.getSingleonInstance().getCameraParam().getNumberOfPhotos() -
                        NeonImagesHandler.getSingleonInstance().getImagesCollection().size()), Toast.LENGTH_SHORT).show();

                return false;
            }
        }
        return true;
    }

    public ImageTagModel getNextTag() {
       /* if (tagModels.get(currentTag).isMandatory() &&
                !NeonImagesHandler.getSingleonInstance().checkImagesAvailableForTag(tagModels.get(currentTag))) {
            Toast.makeText(this, String.format(getString(R.string.tag_mandatory_error), tagModels.get(currentTag).getTagName()),
                    Toast.LENGTH_SHORT).show();
        } else {
            currentTag++;
        }
        */
        currentTag++;

        if (currentTag == tagModels.size() - 1) {

            tvNext.setVisibility(View.VISIBLE);
            tvNext.setText(getString(R.string.finish));

        }
        if (currentTag > 0) {
            tvPrevious.setVisibility(View.VISIBLE);
        }
        ImageTagModel imageTagModel = tagModels.get(currentTag);


        if (NeonImagesHandler.getSingletonInstance().getLivePhotosListener() != null) {
            tvPrevious.setVisibility(View.INVISIBLE);
            if (imageTagModel.isMandatory()) {
                tvNext.setVisibility(View.INVISIBLE);
            } else {
                tvNext.setText("Skip");
                tvNext.setVisibility(View.VISIBLE);
            }
        }

        return imageTagModel;
    }

    public ImageTagModel getPreviousTag() {
        if (currentTag > 0) {
            currentTag--;
        }
        if (currentTag != tagModels.size() - 1) {
            tvNext.setText(getString(R.string.next));
        }
        if (currentTag == 0) {
            tvPrevious.setVisibility(View.GONE);
        }
        return tagModels.get(currentTag);
    }

    public void setTag(ImageTagModel imageTagModel, boolean rightToLeft) {
        // need to update both view and internal data @ the same time, animation may delay this.
        String tag = imageTagModel.isMandatory() ? "*" + imageTagModel.getTagName() : imageTagModel.getTagName();
        tvTag.setText(tag);
        if (NeonImagesHandler.getSingletonInstance().getLivePhotosListener() != null) {
            NeonImagesHandler.getSingletonInstance().setCurrentTag(tag.trim());
        }
        if (imageTagModel.isMandatory()) {
            tvTag.setTextColor(Color.RED);
        } else {
            tvTag.setTextColor(Color.WHITE);
        }
        if (rightToLeft) {
            AnimationUtils.translateOnXAxis(tvTag, 200, 0);
        } else {
            AnimationUtils.translateOnXAxis(tvTag, -200, 0);
        }
    }

    private void customize() {
        if (cameraParams != null && cameraParams.getTagEnabled()) {
            //tvImageName.setVisibility(View.GONE);
            tagsLayout.setVisibility(View.VISIBLE);
            tagModels = cameraParams.getImageTagsModel();
            initialiazeCurrentTag();
            ImageTagModel singleTagModel = tagModels.get(currentTag);

            if (NeonImagesHandler.getSingletonInstance().getLivePhotosListener() != null) {
                if (singleTagModel.isMandatory()) {
                    tvNext.setVisibility(View.INVISIBLE);
                } else {
                    tvNext.setVisibility(View.VISIBLE);
                    tvNext.setText("Skip");
                }
                tvPrevious.setVisibility(View.INVISIBLE);
            } else {
                tvNext.setVisibility(View.VISIBLE);
            }
            setTag(singleTagModel, true);
        } else {
            tagsLayout.setVisibility(View.GONE);
            findViewById(R.id.rlTags).setVisibility(View.GONE);
        }

        if (cameraParams != null) {
            buttonGallery.setVisibility(cameraParams.cameraToGallerySwitchEnabled() ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void initialiazeCurrentTag() {
        for (int i = 0; i < NeonImagesHandler.getSingletonInstance().getGenericParam().getImageTagsModel().size(); i++) {
            if (tagModels.get(i).isMandatory() &&
                    !NeonImagesHandler.getSingletonInstance().checkImagesAvailableForTag(tagModels.get(i))) {
                currentTag = i;
                break;
            }
        }
        if (currentTag == tagModels.size() - 1) {
            tvNext.setVisibility(View.VISIBLE);
            tvNext.setText(getString(R.string.finish));

        }
        if (currentTag > 0) {
            tvPrevious.setVisibility(View.VISIBLE);
        }
        /*if (NeonImagesHandler.getSingletonInstance().getLivePhotosListener() != null) {
            tvNext.setVisibility(View.INVISIBLE);
            tvPrevious.setVisibility(View.INVISIBLE);
        }*/
    }

    @Override
    public void onBackPressed() {
        if(isPreviewVisible){
            isPreviewVisible = false;
            previewLayout.setVisibility(View.GONE);
            NeonUtils.deleteFile(NormalCameraActivityNeon.this, filePathToReview);
        }else {
            if (NeonImagesHandler.getSingletonInstance().isNeutralEnabled()) {
                super.onBackPressed();
            } else {
                if (NeonImagesHandler.getSingletonInstance().getLivePhotosListener() != null) {
                    NeonImagesHandler.getSingletonInstance().showBackOperationAlertIfNeededLive(this);
                } else {
                    NeonImagesHandler.getSingletonInstance().showBackOperationAlertIfNeeded(this);
                }

            }
        }
    }

    @Override
    public void onPictureTaken(String filePath) {
        Log.d("NormalCamera", "onPictureTaken: ");
        mInputImagePath = filePath;
        if (cameraParams != null && cameraParams.getCustomParameters() != null && cameraParams.getCustomParameters().getCamScannerAPIKey() != null && !cameraParams.getCustomParameters().getCamScannerAPIKey().equals("")) {
            previewLayout.setVisibility(View.GONE);
            isPreviewVisible = false;
            if (camScannerApi != null) {
                if (camScannerApi.isCamScannerInstalled()) {

                    String folderName = null;
                    if (NeonImagesHandler.getSingletonInstance().getCameraParam() != null && NeonImagesHandler.getSingletonInstance().getCameraParam().getCustomParameters() != null && NeonImagesHandler.getSingletonInstance().getCameraParam().getCustomParameters().getFolderName() != null) {
                        folderName = NeonImagesHandler.getSingletonInstance().getCameraParam().getCustomParameters().getFolderName();
                    }
                    mOutputImagePath = NeonUtils.getMediaOutputPath(NormalCameraActivityNeon.this, folderName);
                    boolean res = PhotosLibrary.go2CamScanner(this, filePath, mOutputImagePath, REQ_CODE_CALL_CAMSCANNER, camScannerApi);
                    Log.d("NormalCamera", "go2CamScanner  " + res);
                    if (!res)
                        afterPictureTaken(filePath);
                } else {
                    Log.d("NormalCamera", "CamScanner not found!!!");
                    afterPictureTaken(filePath);
                }
            } else {
                Log.d("NormalCamera", "CamScanner not initialised!!!");
                afterPictureTaken(filePath);
            }
        } else if(cameraParams != null && cameraParams.getCustomParameters() != null && cameraParams.getCustomParameters().isShowPreviewForEachImage()){
            previewLayout.setVisibility(View.VISIBLE);
            isPreviewVisible = true;
            filePathToReview = filePath;
            if (cameraParams != null && cameraParams.getTagEnabled()){
                previewTitle.setText(tagModels.get(currentTag).getTagName());;
            }else {
                previewTitle.setText(R.string.image_preview);
            }
            RequestOptions options = new RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .placeholder(R.drawable.default_placeholder);
            Glide.with(this).load(filePath)
                    .apply(options)
                    .into(imagePreview);
        }else {
            Log.d("NormalCamera", "WithoutCamScanner");
            previewLayout.setVisibility(View.GONE);
            isPreviewVisible = false;
            afterPictureTaken(filePath);
        }

    }

    public void afterPictureTaken(String filePath) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilePath(filePath);
        fileInfo.setFileName(filePath.substring(filePath.lastIndexOf("/") + 1));
        fileInfo.setSource(FileInfo.SOURCE.PHONE_CAMERA);
        if (cameraParams.getTagEnabled()) {
            fileInfo.setFileTag(tagModels.get(currentTag));
        }
        if (imageHolderView.getVisibility() != View.VISIBLE) {
            imageHolderView.setVisibility(View.VISIBLE);
        }
        boolean locationRestriction = cameraParams == null || cameraParams.getCustomParameters() == null || cameraParams.getCustomParameters().getLocationRestrictive();
        boolean isUpdated = true;

        if (locationRestriction) {
            isUpdated = updateExifInfo(fileInfo);
        } else {
            isUpdated = updateExifInfoAppName(fileInfo);
        }
        if (isUpdated) {
            NeonImagesHandler.getSingletonInstance().putInImageCollection(fileInfo, this);

            if (NeonImagesHandler.getSingletonInstance().getLivePhotosListener() == null) {

                if (NeonImagesHandler.getSingletonInstance().getCameraParam().getCameraViewType() == CameraType.gallery_preview_camera) {
                    ImageView image = new ImageView(this);
                    Bitmap thumbnail = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(filePath), 200, 200);
                    image.setImageBitmap(thumbnail);
                    imageHolderView.addView(image);
                }

                if (cameraParams.getTagEnabled()) {
                    ImageTagModel imageTagModel = tagModels.get(currentTag);
                    if (imageTagModel.getNumberOfPhotos() > 0 && NeonImagesHandler.getSingletonInstance().getNumberOfPhotosCollected(imageTagModel) >= imageTagModel.getNumberOfPhotos()) {
                        onClick(tvNext);
                    }
                }
            }
        } else {
            Toast.makeText(this, "Unable to find location, Please try again later.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        locationTracker.stopLocationUpdates();
        locationTracker.setLocationListener(null);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == LocationHelper.REQUEST_PERMISSIONS_REQUEST_CODE && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
        {
            locationTracker.getLocation();
        }
    }

    @Override
    public boolean updateExifInfo(FileInfo fileInfo) {
        try {
            //if (cameraParams.getTagEnabled()) {
            //ImageTagModel imageTagModel = tagModels.get(currentTag);
            // Save exit attributes to file
            final File file = new File(fileInfo.getFilePath());
            if (!file.exists()) {
                Toast.makeText(this, NeonImagesHandler.getSingletonInstance().getCurrentTag() + " File does not exist", Toast.LENGTH_SHORT).show();
                return false;
            } else {
                String appName = Constants.getAppName(this);
                ExifInterfaceHandling exifInterfaceHandling = new ExifInterfaceHandling(file);
                if(location != null)
                {
                    exifInterfaceHandling.setLocation(location, appName);
                    if ((String.valueOf(location.getLatitude())).equals(exifInterfaceHandling.getAttribute(ExifInterfaceHandling.TAG_GPS_LATITUDE_REF))) {
                        return true;
                    }
                }
            }
            // }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;

    }

    @Override
    public void onNextTag() {
        if (NeonImagesHandler.getSingletonInstance().getLivePhotosListener() != null) {
            if (cameraParams.getTagEnabled()) {
                ImageTagModel imageTagModel = tagModels.get(currentTag);
                if (imageTagModel.getNumberOfPhotos() > 0 && NeonImagesHandler.getSingletonInstance().getNumberOfPhotosCollected(imageTagModel) >= imageTagModel.getNumberOfPhotos()) {
                    onClick(tvNext);
                }
            }
        }
    }

    private boolean updateExifInfoAppName(FileInfo fileInfo) {
        try {
            final File file = new File(fileInfo.getFilePath());
            if (!file.exists()) {
                return false;
            } else {
                String appName = Constants.getAppName(this);
                ExifInterfaceHandling exifInterfaceHandling = new ExifInterfaceHandling(file);
                exifInterfaceHandling.setAppName(appName);
                if ((String.valueOf(appName)).equals(exifInterfaceHandling.getAttribute(ExifInterfaceHandling.TAG_ARTIST))) {
                    return true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d("NormalCamera", "onActivityResult: ");
        if (resultCode == Activity.RESULT_OK){
            if (requestCode == REQ_CODE_CALL_CAMSCANNER){
                camScannerApi.handleResult(requestCode, resultCode, data, new CSOpenApiHandler() {
                    @Override
                    public void onSuccess() {
                        Log.d("NormalCamera", "onSuccess: "+mOutputImagePath);
                        NeonUtils.scanFile(NormalCameraActivityNeon.this, mOutputImagePath);
                        afterPictureTaken(mOutputImagePath);
                        NeonUtils.deleteFile(NormalCameraActivityNeon.this, mInputImagePath);
                    }
                    @Override
                    public void onError(int i) {
                        Log.d("NormalCamera", "onError: "+i);
                        afterPictureTaken(mInputImagePath);
                    }
                    @Override
                    public void onCancel() {
                        Log.d("NormalCamera", "onCancel: ");
                        afterPictureTaken(mInputImagePath);
                    }
                });
            }
        }
        if(requestCode == LocationHelper.REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK)
        {
            locationTracker.getLocation();
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        this.location = location;
    }
}
