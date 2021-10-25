package com.gaadi.neon.activity.gallery;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.gaadi.neon.activity.camera.NormalCameraActivityNeon;
import com.gaadi.neon.enumerations.CameraFacing;
import com.gaadi.neon.enumerations.CameraOrientation;
import com.gaadi.neon.enumerations.CameraType;
import com.gaadi.neon.PhotosLibrary;
import com.gaadi.neon.activity.ImageShow;
import com.gaadi.neon.adapter.GalleryHoriontalAdapter;
import com.gaadi.neon.enumerations.ResponseCode;
import com.gaadi.neon.interfaces.ICameraParam;
import com.gaadi.neon.interfaces.OnImageClickListener;
import com.gaadi.neon.interfaces.OnPermissionResultListener;
import com.gaadi.neon.model.ImageTagModel;
import com.gaadi.neon.model.PhotosMode;
import com.gaadi.neon.util.Constants;
import com.gaadi.neon.util.CustomParameters;
import com.gaadi.neon.util.FileInfo;
import com.gaadi.neon.util.ManifestPermission;
import com.gaadi.neon.util.NeonException;
import com.gaadi.neon.util.PermissionType;
import com.gaadi.neon.util.NeonImagesHandler;
import com.scanlibrary.R;
import com.scanlibrary.databinding.HorizontalGalleryLayoutBinding;

import java.util.ArrayList;
import java.util.List;

/**
 * @author princebatra
 * @version 1.0
 * @since 6/2/17
 */
public class HorizontalFilesActivity extends NeonBaseGalleryActivity implements OnImageClickListener {

    HorizontalGalleryLayoutBinding binder;
    ArrayList<FileInfo> fileInfos;
    MenuItem textViewDone, menuItemCamera;
    List<FileInfo> recentelyImageCollection;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recentelyImageCollection = new ArrayList<>();
        bindXml();
        String title = getIntent().getStringExtra(Constants.BucketName);
        if (title == null || title.length() <= 0) {
            title = getString(R.string.gallery);
        }
        setTitle(title);
    }

    public void addImageToRecentelySelected(FileInfo fileInfo) {
        recentelyImageCollection.add(fileInfo);
    }

    public boolean removeImageFromRecentCollection(FileInfo fileInfo) {
        if (recentelyImageCollection == null || recentelyImageCollection.size() <= 0) {
            return true;
        }
        for (int i = 0; i < recentelyImageCollection.size(); i++) {
            if (recentelyImageCollection.get(i).getFilePath().equals(fileInfo.getFilePath())) {
                return recentelyImageCollection.remove(i) != null;
            }
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done_file, menu);
        textViewDone = menu.findItem(R.id.menu_next);
        menuItemCamera = menu.findItem(R.id.menuCamera);
        menuItemCamera.setVisible(NeonImagesHandler.getSingletonInstance().getGalleryParam().galleryToCameraSwitchEnabled());
        textViewDone.setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menu_next) {
            if (NeonImagesHandler.getSingletonInstance().getImagesCollection() == null ||
                    NeonImagesHandler.getSingletonInstance().getImagesCollection().size() <= 0) {
                Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            } else {
                if (!NeonImagesHandler.getSingletonInstance().isNeutralEnabled()) {
                    if(NeonImagesHandler.getSingletonInstance().getGalleryParam().enableImageEditing()
                            || NeonImagesHandler.getSingletonInstance().getGalleryParam().getTagEnabled()) {
                        Intent intent = new Intent(this, ImageShow.class);
                        startActivity(intent);
                        setResult(Constants.destroyPreviousActivity);
                        finish();
                    }else{
                        if (NeonImagesHandler.getSingletonInstance().validateNeonExit(this)) {
                            NeonImagesHandler.getSingletonInstance().sendImageCollectionAndFinish(this, ResponseCode.Success);
                            finish();
                        }
                    }
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
            }
        } else if (id == R.id.menuCamera) {
            performCameraOperation();
            setResult(Constants.destroyPreviousActivity);
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (recentelyImageCollection != null && recentelyImageCollection.size() > 0) {
            new AlertDialog.Builder(this).setTitle("Are you sure want to loose all selected images?")
                    .setCancelable(true).setIcon(android.R.drawable.ic_dialog_alert).setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    for (int i = 0; i < recentelyImageCollection.size(); i++) {
                        NeonImagesHandler.getSingletonInstance().removeFromCollection(recentelyImageCollection.get(i));
                    }
                    finish();
                }
            }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            }).show();
            return;
        }

        if (NeonImagesHandler.getSingletonInstance().isNeutralEnabled()) {
            super.onBackPressed();
        } else {
            if (!NeonImagesHandler.getSingletonInstance().getGalleryParam().enableFolderStructure()) {
                NeonImagesHandler.getSingletonInstance().showBackOperationAlertIfNeeded(this);
            } else {
                super.onBackPressed();
            }

        }
    }

    private void performCameraOperation() {

        ICameraParam cameraParam = NeonImagesHandler.getSingletonInstance().getCameraParam();
        if (cameraParam == null) {
            cameraParam = new ICameraParam() {
                @Override
                public CameraFacing getCameraFacing() {
                    return CameraFacing.front;
                }

                @Override
                public CameraOrientation getCameraOrientation() {
                    return CameraOrientation.portrait;
                }

                @Override
                public boolean getFlashEnabled() {
                    return true;
                }

                @Override
                public boolean getCameraSwitchingEnabled() {
                    return true;
                }

                @Override
                public boolean getVideoCaptureEnabled() {
                    return false;
                }

                @Override
                public CameraType getCameraViewType() {
                    return CameraType.normal_camera;
                }

                @Override
                public boolean cameraToGallerySwitchEnabled() {
                    return true;
                }

                @Override
                public int getNumberOfPhotos() {
                    return NeonImagesHandler.getSingleonInstance().getGalleryParam().getNumberOfPhotos();
                }

                @Override
                public boolean getTagEnabled() {
                    return NeonImagesHandler.getSingleonInstance().getGalleryParam().getTagEnabled();
                }

                @Override
                public List<ImageTagModel> getImageTagsModel() {
                    return NeonImagesHandler.getSingletonInstance().getGalleryParam().getImageTagsModel();
                }

                @Override
                public ArrayList<FileInfo> getAlreadyAddedImages() {
                    return null;
                }

                @Override
                public boolean enableImageEditing() {
                    return NeonImagesHandler.getSingletonInstance().getGalleryParam().enableImageEditing();
                }

                @Override
                public CustomParameters getCustomParameters() {
                    return NeonImagesHandler.getSingletonInstance().getGalleryParam().getCustomParameters();
                }

            };
        }
        try {
            PhotosLibrary.collectPhotos(NeonImagesHandler.getSingletonInstance().getRequestCode(),this,NeonImagesHandler.getSingleonInstance().getLibraryMode(), PhotosMode.setCameraMode().setParams(cameraParam), NeonImagesHandler.getSingleonInstance().getImageResultListener());
        } catch (NeonException e) {
            e.printStackTrace();
        }
    }


    private void bindXml() {
        try {
            askForPermissionIfNeeded(PermissionType.write_external_storage, new OnPermissionResultListener() {
                @Override
                public void onResult(boolean permissionGranted) {
                    if (permissionGranted) {
                        binder = DataBindingUtil.inflate(getLayoutInflater(), R.layout.horizontal_gallery_layout, frameLayout, true);
                        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(HorizontalFilesActivity.this, LinearLayoutManager.HORIZONTAL, false);
                        binder.galleryHorizontalRv.setLayoutManager(linearLayoutManager);
                        fileInfos = getFileFromBucketId(getIntent().getStringExtra(Constants.BucketId));
                        if (fileInfos != null && fileInfos.size() > 0) {
                            GalleryHoriontalAdapter adapter = new GalleryHoriontalAdapter(HorizontalFilesActivity.this, fileInfos, HorizontalFilesActivity.this);
                            binder.galleryHorizontalRv.setAdapter(adapter);
                            onClick(fileInfos.get(0));
                        }
                    } else {
                        if (NeonImagesHandler.getSingletonInstance().isNeutralEnabled()) {
                            finish();
                        }else{
                            NeonImagesHandler.getSingletonInstance().sendImageCollectionAndFinish(HorizontalFilesActivity.this,
                                    ResponseCode.Write_Permission_Error);
                        }
                        Toast.makeText(HorizontalFilesActivity.this, R.string.permission_error, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } catch (ManifestPermission manifestPermission) {
            manifestPermission.printStackTrace();
        }


    }


    @Override
    public void onClick(FileInfo fileInfo) {
        Glide.with(this).load(fileInfo.getFilePath())
                .placeholder(R.drawable.default_placeholder)
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(binder.fullScreenImage);

    }
}
