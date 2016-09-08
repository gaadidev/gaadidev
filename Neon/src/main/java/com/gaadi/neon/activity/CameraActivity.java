package com.gaadi.neon.activity;

import android.content.Intent;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.gaadi.neon.fragment.CameraFragment;
import com.gaadi.neon.fragment.NeutralFragment;
import com.gaadi.neon.util.CameraPreview;
import com.gaadi.neon.util.Constants;
import com.gaadi.neon.util.FileInfo;
import com.gaadi.neon.util.PhotoParams;
import com.scanlibrary.R;
import com.scanlibrary.ScanActivity;
import com.scanlibrary.ScanConstants;
import com.scanlibrary.ScanFragment;

import java.io.File;
import java.util.ArrayList;
/**
 * @author lakshaygirdhar
 * @version 1.0
 * @since 8/9/16
 */
@SuppressWarnings("deprecation,unchecked")
public class CameraActivity extends AppCompatActivity implements CameraFragment.PictureTakenListener
{
    private static final String TAG = "CameraActivity";
    public static final int GALLERY_PICK = 99;

    private Camera camera;
    private PhotoParams photoParams;
    public boolean readyToTakePicture;
    private CameraPreview cameraPreview;
    private ArrayList<FileInfo> imagesList = new ArrayList<>();
    private ArrayList<String> outputImages = new ArrayList<>();

    @Override
    protected void onCreate(
            @Nullable
            Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        photoParams = (PhotoParams) getIntent().getSerializableExtra(NeutralFragment.PHOTO_PARAMS);
        CameraFragment fragment = CameraFragment.getInstance(photoParams);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            camera.setPreviewCallback(null);
            cameraPreview.getHolder().removeCallback(cameraPreview);
            camera.stopPreview();
            camera.release();
            camera = null;
            cameraPreview = null;
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Constants.IMAGES_SELECTED, imagesList);
    }

    @Override
    public void onPictureTaken(String filePath)
    {
        outputImages.clear();
        outputImages.add(filePath);
        setResult(RESULT_OK, new Intent().putStringArrayListExtra(Constants.RESULT_IMAGES, outputImages));
        finish();
    }

    @Override
    public void onGalleryPicsCollected(ArrayList<FileInfo> infos)
    {
        getSupportFragmentManager().popBackStackImmediate();
        if(infos.size() > 0)
        {
            setResult(ScanConstants.MULTIPLE_CAPTURED, new Intent().putExtra(ScanConstants.CAMERA_IMAGES, infos));
            finish();
        }
        else
        {
            Toast.makeText(this, getString(R.string.click_photo), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void sendPictureForCropping(File file)
    {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.putExtra(ScanConstants.IMAGE_FILE_FOR_CROPPING,file);
        startActivityForResult(intent,ScanActivity.REQUEST_REVIEW);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK)
        {
            if(requestCode == GALLERY_PICK)
            {
                imagesList = (ArrayList<FileInfo>) data.getSerializableExtra(GalleryActivity.GALLERY_SELECTED_PHOTOS);
            }
            else
            {
                readyToTakePicture = true;
            }
        }
        else if(resultCode == RESULT_CANCELED)
        {
            FragmentManager manager = getSupportFragmentManager();
            manager.popBackStack(ScanFragment.class.toString(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
            if(null == photoParams)
            {
                finish();
            }
        }
    }
}
