package com.gaadi.neon.activity.neutral;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.gaadi.neon.PhotosLibrary;
import com.gaadi.neon.enumerations.CameraFacing;
import com.gaadi.neon.enumerations.CameraOrientation;
import com.gaadi.neon.enumerations.CameraType;
import com.gaadi.neon.enumerations.GalleryType;
import com.gaadi.neon.fragment.ImageShowFragment;
import com.gaadi.neon.interfaces.ICameraParam;
import com.gaadi.neon.interfaces.IGalleryParam;
import com.gaadi.neon.model.ImageTagModel;
import com.gaadi.neon.model.PhotosMode;
import com.gaadi.neon.util.CustomParameters;
import com.gaadi.neon.util.FileInfo;
import com.gaadi.neon.util.NeonException;
import com.gaadi.neon.util.NeonImagesHandler;
import com.scanlibrary.R;

import java.util.ArrayList;
import java.util.List;

/**
 * @author princebatra
 * @version 1.0
 * @since 3/2/17
 */
public class NeonNeutralActivity extends NeonBaseNeutralActivity implements View.OnClickListener {

    ArrayAdapter<String> adapter;
    private TextView txtTagTitle, showMinCount;
    private ListView tabList;
    private LinearLayout addPhotoCamera, addPhotoGallary;
    private FrameLayout imageShowFragmentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getLayoutInflater().inflate(R.layout.neutral_activity_layout, frameLayout);
        txtTagTitle = findViewById(R.id.txtTagTitle);
        tabList = findViewById(R.id.tabList);
        imageShowFragmentContainer = findViewById(R.id.imageShowFragmentContainer);
        addPhotoCamera = findViewById(R.id.addPhotoCamera);
        showMinCount = findViewById(R.id.show_min_count);
        addPhotoGallary = findViewById(R.id.addPhotoGallary);
        addPhotoGallary.setOnClickListener(this);
        addPhotoCamera.setOnClickListener(this);
        bindXml();
    }

    @Override
    public void onPostResume() {
        super.onPostResume();
        if (NeonImagesHandler.getSingletonInstance().getImagesCollection() == null ||
                NeonImagesHandler.getSingletonInstance().getImagesCollection().size() <= 0) {
            if (NeonImagesHandler.getSingletonInstance() != null && NeonImagesHandler.getSingletonInstance().getGenericParam() != null
                    && NeonImagesHandler.getSingletonInstance().getGenericParam().getCustomParameters() != null &&
                    NeonImagesHandler.getSingletonInstance().getGenericParam().getCustomParameters().getTitleName() != null) {
                setTitle(NeonImagesHandler.getSingletonInstance().getGenericParam().getCustomParameters().getTitleName());
            } else {
                setTitle(R.string.photos);
            }
            tabList.setVisibility(View.VISIBLE);
            //txtTagTitle.setVisibility(View.VISIBLE);
            if (adapter == null) {
                List<ImageTagModel> tagModels = new ArrayList<>();
                if (NeonImagesHandler.getSingletonInstance().getGenericParam() != null &&
                        NeonImagesHandler.getSingletonInstance().getGenericParam().getCustomParameters().getClickMinimumNumberOfImages() != 0 &&
                        !NeonImagesHandler.getSingletonInstance().getNeutralParam().getTagEnabled()) {
                    showMinCount.setVisibility(View.VISIBLE);
                    txtTagTitle.setVisibility(View.GONE);
                    showMinCount.setText(String.format("Minimum number of images required is %s", String.valueOf(NeonImagesHandler.getSingletonInstance().getGenericParam().getCustomParameters().getClickMinimumNumberOfImages())));
                }  else {
                    showMinCount.setVisibility(View.GONE);
                }
                if (NeonImagesHandler.getSingletonInstance().getNeutralParam() != null && NeonImagesHandler.getSingletonInstance().getNeutralParam().getImageTagsModel() != null)
                    tagModels = NeonImagesHandler.getSingletonInstance().getNeutralParam().getImageTagsModel();
                if (tagModels == null || tagModels.size() <= 0) {
                    return;
                }
                tagModels = getMandetoryTags(tagModels);
                if (tagModels == null || tagModels.size() <= 0) {
                    txtTagTitle.setVisibility(View.GONE);
                } else {
                    txtTagTitle.setVisibility(View.VISIBLE);
                }
                String[] tags = new String[tagModels.size()];
                for (int i = 0; i < tagModels.size(); i++) {
                    tags[i] = " • " + tagModels.get(i).getTagName();

                }
                adapter = new ArrayAdapter<>(this, R.layout.single_textview, R.id.tagText, tags);
            }
            tabList.setAdapter(adapter);
        } else {
            tabList.setVisibility(View.GONE);
           // txtTagTitle.setVisibility(View.GONE);
            showMinCount.setVisibility(View.GONE);
            imageShowFragmentContainer.setVisibility(View.VISIBLE);
            if (NeonImagesHandler.getSingletonInstance() != null &&
                    NeonImagesHandler.getSingletonInstance().getGenericParam() != null &&
                    NeonImagesHandler.getSingletonInstance().getGenericParam().getCustomParameters() != null &&
                    NeonImagesHandler.getSingletonInstance().getGenericParam().getCustomParameters().getTitleName() != null) {
                setTitle(NeonImagesHandler.getSingletonInstance().getGenericParam().getCustomParameters().getTitleName() + " (" + NeonImagesHandler.getSingletonInstance().getImagesCollection().size() + ")");
            } else {
                setTitle(getString(R.string.photos_count, NeonImagesHandler.getSingletonInstance().getImagesCollection().size()));
            }
        }
    }

    private List<ImageTagModel> getMandetoryTags(List<ImageTagModel> tagModels) {
        List<ImageTagModel> fileterdList = new ArrayList<>();
        for (ImageTagModel singleModel :
                tagModels) {
            if (singleModel.isMandatory()) {
                fileterdList.add(singleModel);
            }
        }
        return fileterdList;
    }

    private void bindXml() {
        if (NeonImagesHandler.getSingletonInstance().getNeutralParam() != null && NeonImagesHandler.getSingletonInstance().getNeutralParam().getCustomParameters() != null) {
            addPhotoCamera.setVisibility(NeonImagesHandler.getSingletonInstance().getNeutralParam().getCustomParameters()
                    .gethideCameraButtonInNeutral() ? View.GONE : View.VISIBLE);
            addPhotoGallary.setVisibility(NeonImagesHandler.getSingletonInstance().getNeutralParam().getCustomParameters()
                    .getHideGalleryButtonInNeutral() ? View.GONE : View.VISIBLE);
        }
        ImageShowFragment imageShowFragment = new ImageShowFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.imageShowFragmentContainer, imageShowFragment).commit();
    }

    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.addPhotoCamera) {
            try {
                PhotosLibrary.collectPhotos(NeonImagesHandler.getSingletonInstance().getRequestCode(), this, NeonImagesHandler.getSingletonInstance().getLibraryMode(), PhotosMode.setCameraMode().setParams(new ICameraParam() {
                    @Override
                    public CameraFacing getCameraFacing() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().getCameraFacing();
                    }

                    @Override
                    public CameraOrientation getCameraOrientation() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().getCameraOrientation();
                    }

                    @Override
                    public boolean getFlashEnabled() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().getFlashEnabled();
                    }

                    @Override
                    public boolean getCameraSwitchingEnabled() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().getCameraSwitchingEnabled();
                    }

                    @Override
                    public boolean getVideoCaptureEnabled() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().getVideoCaptureEnabled();
                    }

                    @Override
                    public CameraType getCameraViewType() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().getCameraViewType();
                    }

                    @Override
                    public boolean cameraToGallerySwitchEnabled() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().cameraToGallerySwitchEnabled();
                    }

                    @Override
                    public int getNumberOfPhotos() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().getNumberOfPhotos();
                    }

                    @Override
                    public boolean getTagEnabled() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam() != null && NeonImagesHandler.getSingletonInstance().getNeutralParam().getTagEnabled();
                    }

                    @Override
                    public List<ImageTagModel> getImageTagsModel() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().getImageTagsModel();
                    }

                    @Override
                    public List<FileInfo> getAlreadyAddedImages() {
                        return null;
                    }

                    @Override
                    public boolean enableImageEditing() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().enableImageEditing();
                    }

                    @Override
                    public CustomParameters getCustomParameters() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().getCustomParameters();
                    }
                }), NeonImagesHandler.getSingletonInstance().getImageResultListener());
            } catch (NeonException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == R.id.addPhotoGallary) {
            try {
                PhotosLibrary.collectPhotos(NeonImagesHandler.getSingletonInstance().getRequestCode(), this, NeonImagesHandler.getSingletonInstance().getLibraryMode(), PhotosMode.setGalleryMode().setParams(new IGalleryParam() {
                    @Override
                    public boolean selectVideos() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().selectVideos();
                    }

                    @Override
                    public GalleryType getGalleryViewType() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().getGalleryViewType();
                    }

                    @Override
                    public boolean enableFolderStructure() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().enableFolderStructure();
                    }

                    @Override
                    public boolean galleryToCameraSwitchEnabled() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().galleryToCameraSwitchEnabled();
                    }

                    @Override
                    public boolean isRestrictedExtensionJpgPngEnabled() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().isRestrictedExtensionJpgPngEnabled();
                    }

                    @Override
                    public int getNumberOfPhotos() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().getNumberOfPhotos();
                    }

                    @Override
                    public boolean getTagEnabled() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam() != null && NeonImagesHandler.getSingletonInstance().getNeutralParam().getTagEnabled();
                    }

                    @Override
                    public List<ImageTagModel> getImageTagsModel() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().getImageTagsModel();
                    }

                    @Override
                    public List<FileInfo> getAlreadyAddedImages() {
                        return null;
                    }

                    @Override
                    public boolean enableImageEditing() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().enableImageEditing();
                    }

                    @Override
                    public CustomParameters getCustomParameters() {
                        return NeonImagesHandler.getSingletonInstance().getNeutralParam().getCustomParameters();
                    }
                }), NeonImagesHandler.getSingletonInstance().getImageResultListener());
            } catch (NeonException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (id == android.R.id.home) {
            onBackPressed();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        NeonImagesHandler.getSingletonInstance().showBackOperationAlertIfNeeded(this);
    }

}
