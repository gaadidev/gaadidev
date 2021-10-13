package com.gaadi.neon.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.FragmentManager;

import com.gaadi.neon.PhotosLibrary;
import com.gaadi.neon.fragment.ImageShowFragment;
import com.gaadi.neon.interfaces.ICameraParam;
import com.gaadi.neon.interfaces.IGalleryParam;
import com.gaadi.neon.model.PhotosMode;
import com.gaadi.neon.util.NeonException;
import com.gaadi.neon.util.NeonImagesHandler;
import com.scanlibrary.R;
import com.scanlibrary.databinding.ActivityImageShowBinding;

public class ImageShow extends NeonBaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.image_review);
        bindXml();
    }


    private void bindXml() {
        ActivityImageShowBinding binder = DataBindingUtil.inflate(getLayoutInflater(), R.layout.activity_image_show,frameLayout,true);
        ImageShowFragment imageShowFragment = new ImageShowFragment();
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().add(R.id.imageShowFragmentContainer, imageShowFragment).commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            return goBackForImageCollection();
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    private boolean goBackForImageCollection() {
        try {
            IGalleryParam galleryParam = NeonImagesHandler.getSingleonInstance().getGalleryParam();
            ICameraParam cameraParam = NeonImagesHandler.getSingleonInstance().getCameraParam();
            if (galleryParam != null) {
                PhotosLibrary.collectPhotos(NeonImagesHandler.getSingletonInstance().getRequestCode(),this,NeonImagesHandler.getSingleonInstance().getLibraryMode(), PhotosMode.setGalleryMode().setParams(galleryParam), NeonImagesHandler.getSingleonInstance().getImageResultListener());
            } else {
                PhotosLibrary.collectPhotos(NeonImagesHandler.getSingletonInstance().getRequestCode(),this,NeonImagesHandler.getSingleonInstance().getLibraryMode(), PhotosMode.setCameraMode().setParams(cameraParam), NeonImagesHandler.getSingleonInstance().getImageResultListener());
            }
            finish();
        } catch (NeonException e) {
        }
        return true;
    }


    @Override
    public void onBackPressed() {
       goBackForImageCollection();
    }

}
