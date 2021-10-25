package com.customise.gaadi.camera;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gaadi.neon.PhotosLibrary;
import com.gaadi.neon.enumerations.CameraFacing;
import com.gaadi.neon.enumerations.CameraOrientation;
import com.gaadi.neon.enumerations.CameraType;
import com.gaadi.neon.enumerations.GalleryType;
import com.gaadi.neon.enumerations.ResponseCode;
import com.gaadi.neon.fragment.ImageShowFragment;
import com.gaadi.neon.interfaces.ICameraParam;
import com.gaadi.neon.interfaces.IGalleryParam;
import com.gaadi.neon.interfaces.OnImageCollectionListener;
import com.gaadi.neon.model.ImageTagModel;
import com.gaadi.neon.model.NeonResponse;
import com.gaadi.neon.model.PhotosMode;
import com.gaadi.neon.util.CustomParameters;
import com.gaadi.neon.util.FileInfo;
import com.gaadi.neon.util.NeonException;
import com.gaadi.neon.util.NeonImagesHandler;

import java.util.HashMap;
import java.util.List;

/**
 * @author dipanshugarg
 * @version 1.0
 * @since 2/3/17
 */
public class CameraActivity extends AppCompatActivity {

    private List<FileInfo> totalList;
    private View camera, gallary;
    private FrameLayout frame;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_layout);
        camera = (View) findViewById(R.id.camera);
        gallary = (View) findViewById(R.id.gallary);
        frame = (FrameLayout) findViewById(R.id.frame);

        gallary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PhotosLibrary.collectPhotos(NeonImagesHandler.getSingletonInstance().getRequestCode(),CameraActivity.this,NeonImagesHandler.getSingletonInstance().getLibraryMode(), PhotosMode.setGalleryMode().setParams(new IGalleryParam() {
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
                            return false;
                        }

                        @Override
                        public boolean isRestrictedExtensionJpgPngEnabled() {
                            return true;
                        }

                        @Override
                        public int getNumberOfPhotos() {
                            return 0;
                        }

                        @Override
                        public boolean getTagEnabled() {
                            return false;
                        }

                        @Override
                        public List<ImageTagModel> getImageTagsModel() {
                            return null;
                        }

                        @Override
                        public List<FileInfo> getAlreadyAddedImages() {
                            return null;
                        }

                        @Override
                        public boolean enableImageEditing() {
                            return false;
                        }

                        @Override
                        public CustomParameters getCustomParameters() {
                            return null;
                        }
                    }), new OnImageCollectionListener() {

                        @Override
                        public void imageCollection(NeonResponse neonResponse) {
                            if (totalList == null) {
                                totalList = neonResponse.getImageCollection();
                            } else {
                                appendAllImages(neonResponse.getImageCollection());
                            }
                            showImages();

                        }

                    });
                } catch (NeonException e) {
                    e.printStackTrace();
                }
            }
        });


        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    PhotosLibrary.collectPhotos(NeonImagesHandler.getSingletonInstance().getRequestCode(),CameraActivity.this,NeonImagesHandler.getSingleonInstance().getLibraryMode(), PhotosMode.setCameraMode().setParams(cameraParam), new OnImageCollectionListener() {
                        @Override
                        public void imageCollection(NeonResponse neonResponse) {
                            if (totalList == null) {
                                totalList = neonResponse.getImageCollection();
                            } else {
                                appendAllImages(neonResponse.getImageCollection());
                            }
                            showImages();
                        }
                    });
                } catch (NeonException e) {
                    e.printStackTrace();
                }
            }
        });


    }


    private ICameraParam cameraParam = new ICameraParam() {
        @Override
        public CameraFacing getCameraFacing() {
            return CameraFacing.back;
        }

        @Override
        public CameraOrientation getCameraOrientation() {
            return CameraOrientation.landscape;
        }

        @Override
        public boolean getFlashEnabled() {
            return true;
        }

        @Override
        public boolean getCameraSwitchingEnabled() {
            return false;
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
            return false;
        }

        @Override
        public int getNumberOfPhotos() {
            return 0;
        }

        @Override
        public boolean getTagEnabled() {
            return false;
        }

        @Override
        public List<ImageTagModel> getImageTagsModel() {
            return null;
        }

        @Override
        public List<FileInfo> getAlreadyAddedImages() {
            return null;
        }

        @Override
        public boolean enableImageEditing() {
            return false;
        }

        @Override
        public CustomParameters getCustomParameters() {
            return null;
        }
    };

    private void showImages() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                NeonImagesHandler.getSingletonInstance().setImagesCollection(totalList);
                NeonImagesHandler.getSingletonInstance().setCameraParam(cameraParam);
                ImageShowFragment fragment = new ImageShowFragment();
                getSupportFragmentManager().beginTransaction().add(R.id.frame,fragment).commitNowAllowingStateLoss();

            }
        },100);


    }

    private void appendAllImages(List<FileInfo> imageCollection) {
        if (imageCollection != null && imageCollection.size() > 0) {
            for (int i = 0; i < imageCollection.size(); i++) {
                totalList.add(imageCollection.get(i));
            }
        }
    }
}
