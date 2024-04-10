package com.gaadi.neon.activity.gallery;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.gaadi.neon.activity.camera.NormalCameraActivityNeon;
import com.gaadi.neon.enumerations.CameraFacing;
import com.gaadi.neon.enumerations.CameraOrientation;
import com.gaadi.neon.enumerations.CameraType;
import com.gaadi.neon.PhotosLibrary;
import com.gaadi.neon.activity.ImageShow;
import com.gaadi.neon.adapter.ImagesFoldersAdapter;
import com.gaadi.neon.enumerations.GalleryType;
import com.gaadi.neon.enumerations.ResponseCode;
import com.gaadi.neon.enumerations.Sorting_Type;
import com.gaadi.neon.interfaces.ICameraParam;
import com.gaadi.neon.interfaces.OnPermissionResultListener;
import com.gaadi.neon.model.BucketModel;
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
import com.scanlibrary.databinding.ActivityGridFoldersBinding;

import java.util.ArrayList;
import java.util.List;

public class GridFoldersActivity extends NeonBaseGalleryActivity {

    private ImagesFoldersAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.gallery);
    }

    @Override
    protected void onResume() {
        super.onResume();
        bindXml();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_done_file, menu);

        MenuItem textViewDone = menu.findItem(R.id.menu_next);
        MenuItem textViewCamera = menu.findItem(R.id.menuCamera);
        if(NeonImagesHandler.getSingletonInstance().getGalleryParam() != null){
            if (NeonImagesHandler.getSingletonInstance().getGalleryParam().galleryToCameraSwitchEnabled()) {
                textViewCamera.setVisible(true);
            } else {
                textViewCamera.setVisible(false);
            }
        }
        textViewDone.setVisible(true);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.menuCamera) {
            performCameraOperation();
        } else if (id == R.id.menu_next) {
            if (NeonImagesHandler.getSingletonInstance().isNeutralEnabled()) {
                finish();
            } else if (NeonImagesHandler.getSingletonInstance().getImagesCollection() == null ||
                    NeonImagesHandler.getSingletonInstance().getImagesCollection().size() <= 0) {
                Toast.makeText(this, R.string.no_image_selected, Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
            } else {
                if(NeonImagesHandler.getSingletonInstance().getGalleryParam()!=null && (NeonImagesHandler.getSingletonInstance().getGalleryParam().enableImageEditing()
                        || NeonImagesHandler.getSingletonInstance().getGalleryParam().getTagEnabled())) {
                    Intent intent = new Intent(this, ImageShow.class);
                    startActivity(intent);
                    finish();
                }else{
                    if (NeonImagesHandler.getSingletonInstance().validateNeonExit(this)) {
                        NeonImagesHandler.getSingletonInstance().sendImageCollectionAndFinish(this, ResponseCode.Success);
                        finish();
                    }
                }


            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (NeonImagesHandler.getSingleonInstance().isNeutralEnabled()) {
            super.onBackPressed();
        } else {
            NeonImagesHandler.getSingleonInstance().showBackOperationAlertIfNeeded(this);
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
                    return NeonImagesHandler.getSingletonInstance().getGalleryParam().getNumberOfPhotos();
                }

                @Override
                public boolean getTagEnabled() {
                    return NeonImagesHandler.getSingleonInstance().getGalleryParam().getTagEnabled();
                }

                @Override
                public List<ImageTagModel> getImageTagsModel() {
                    return NeonImagesHandler.getSingleonInstance().getGalleryParam().getImageTagsModel();
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
            PhotosLibrary.collectPhotos(NeonImagesHandler.getSingletonInstance().getRequestCode(),this,NeonImagesHandler.getSingletonInstance().getLibraryMode(), PhotosMode.setCameraMode().setParams(cameraParam), NeonImagesHandler.getSingleonInstance().getImageResultListener());
        } catch (NeonException e) {
            e.printStackTrace();
        }
        finish();
    }


    private void bindXml() {
        if(NeonImagesHandler.getSingletonInstance().getGalleryParam().getGalleryViewType() == GalleryType.Sorting_Enabled_Structure){
            sortFilter.setVisibility(View.VISIBLE);
            sortFilter.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showSortingOptions(v);
                }
            });
        }

        PermissionType permissionType = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) ? PermissionType.read_external_storage:PermissionType.write_external_storage;

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                askForPermissionIfNeeded(permissionType, new OnPermissionResultListener() {
                    @Override
                    public void onResult(boolean permissionGranted) {
                        if (permissionGranted) {
                            ActivityGridFoldersBinding binder = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_grid_folders, frameLayout, true);
                            adapter = new ImagesFoldersAdapter(GridFoldersActivity.this,
                                    NeonImagesHandler.getSingletonInstance().getGalleryParam().getGalleryViewType() == GalleryType.Sorting_Enabled_Structure ?
                                            getSortedImageBuckets(NeonImagesHandler.getSingletonInstance().getGalleryParam().getCustomParameters().getFolderSortingType()) :
                                            getImageBuckets()
                            );
                            binder.gvFolders.setAdapter(adapter);
                        } else {
                            if (NeonImagesHandler.getSingletonInstance().isNeutralEnabled()) {
                                finish();
                            }else{
                                NeonImagesHandler.getSingletonInstance().sendImageCollectionAndFinish(GridFoldersActivity.this,
                                        ResponseCode.Write_Permission_Error);
                            }
                            Toast.makeText(GridFoldersActivity.this, R.string.permission_error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            } else {
                ActivityGridFoldersBinding binder = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_grid_folders, frameLayout, true);
                adapter = new ImagesFoldersAdapter(GridFoldersActivity.this,
                        NeonImagesHandler.getSingletonInstance().getGalleryParam().getGalleryViewType() == GalleryType.Sorting_Enabled_Structure ?
                                getSortedImageBuckets(NeonImagesHandler.getSingletonInstance().getGalleryParam().getCustomParameters().getFolderSortingType()) :
                                getImageBuckets()
                );
                binder.gvFolders.setAdapter(adapter);
            }
        } catch (ManifestPermission manifestPermission) {
            manifestPermission.printStackTrace();
        }

    }

    private void showSortingOptions(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        MenuInflater inflater = popupMenu.getMenuInflater();
        inflater.inflate(R.menu.menu_sort, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Sorting_Type sorting  = Sorting_Type.Date_Descending;
                int itemId = item.getItemId();
                if (itemId == R.id.action_sort_date_desc) {
                    sorting = Sorting_Type.Date_Descending;
                } else if (itemId == R.id.action_sort_date_asc) {
                    sorting = Sorting_Type.Date_Ascending;
                } else if (itemId == R.id.action_sort_alphabet_desc) {
                    sorting = Sorting_Type.Alphabetical_Descending;
                } else if (itemId == R.id.action_sort_alphabet_asc) {
                    sorting = Sorting_Type.Alphabetical_Ascending;
                }
                updateFolderData(sorting);
                return true;
            }
        });
        popupMenu.show();
    }

    private void updateFolderData(Sorting_Type sortingType){
        NeonImagesHandler.getSingletonInstance().getSortingSelectedListener().onFolderSortingSelected(sortingType);
        ArrayList<BucketModel> sortedImageBuckets = getSortedImageBuckets(sortingType);
        if(sortedImageBuckets != null){
            adapter.updateData(sortedImageBuckets,
                    sortingType);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.destroyPreviousActivity && requestCode == Constants.destroyPreviousActivity) {
            finish();
        }
    }
}
