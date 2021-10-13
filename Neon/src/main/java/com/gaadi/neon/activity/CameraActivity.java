package com.gaadi.neon.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.gaadi.neon.fragment.CameraFragment;
import com.gaadi.neon.fragment.CameraFragment1;
import com.gaadi.neon.fragment.NeutralFragment;
import com.gaadi.neon.util.Constants;
import com.gaadi.neon.util.FileInfo;
import com.gaadi.neon.util.NeonConstants;
import com.gaadi.neon.util.PhotoParams;
import com.scanlibrary.R;

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
    public static final int GALLERY_PICK = 99;
    private static final String TAG = "CameraActivity";
    public boolean readyToTakePicture;
    private ArrayList<FileInfo> imagesList = new ArrayList<>();
    private ArrayList<String> outputImages = new ArrayList<>();

    @Override
    protected void onCreate(
            Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_priority_items);
        PhotoParams photoParams = (PhotoParams) getIntent().getSerializableExtra(NeutralFragment.PHOTO_PARAMS);
        /*CameraFragment1 fragment = CameraFragment1.getInstance(photoParams);*/
        CameraFragment1 fragment = new CameraFragment1();
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content_frame, fragment).commit();
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
    public void onPicturesFinalized(ArrayList<FileInfo> infos)
    {
        getSupportFragmentManager().popBackStackImmediate();
        if(infos.size() > 0)
        {
            Log.d(TAG, "onPicturesFinalized: " + infos.get(0).getFilePath());
            setResult(RESULT_OK, new Intent().putExtra(NeonConstants.COLLECTED_IMAGES, infos));
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
//        Intent intent = new Intent(this, ScanActivity.class);
//        intent.putExtra(ScanConstants.IMAGE_FILE_FOR_CROPPING,file);
//        startActivityForResult(intent,ScanActivity.REQUEST_REVIEW);
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
//            FragmentManager manager = getSupportFragmentManager();
//            manager.popBackStack(ScanFragment.class.toString(), FragmentManager.POP_BACK_STACK_INCLUSIVE);
//            if(null == photoParams)
//            {
//                finish();
//            }
        }
    }
}
