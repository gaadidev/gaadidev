package com.gaadi.neon.activity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import com.gaadi.neon.fragment.NeutralFragment;
import com.gaadi.neon.util.ApplicationController;
import com.gaadi.neon.util.Constants;
import com.gaadi.neon.util.PhotoParams;
import com.scanlibrary.R;

/**
 *  @author lakshaygirdhar
 *  @version 1.0
 *  @since 13-08-2016
 *
 */

public class LNeutralActivity extends FragmentActivity
{
    private static final String TAG = "LNeutralActivity";
    private NeutralFragment cameraItemsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_neutral);

        PhotoParams params = (PhotoParams) getIntent().getSerializableExtra(NeutralFragment.PHOTO_PARAMS);
        if(savedInstanceState!=null)
            cameraItemsFragment = (NeutralFragment) getSupportFragmentManager().getFragment(savedInstanceState, "mContent");
        else
            cameraItemsFragment = NeutralFragment.newInstance(this, params, null,
                R.drawable.image_load_default_big, R.drawable.image_load_default_small);
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.photoFragment, cameraItemsFragment).commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        getSupportFragmentManager().putFragment(outState,"mContent",cameraItemsFragment);
    }



    @Override
    public void onBackPressed() {

        if (ApplicationController.selectedFiles != null) {
            ApplicationController.selectedFiles.clear();
        }

        super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == Constants.REQUEST_PERMISSION_CAMERA) {
            if (grantResults.length > 1
                    && (grantResults[0] != PackageManager.PERMISSION_GRANTED
                            || grantResults[1] != PackageManager.PERMISSION_GRANTED)) {
                return;
            }
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraItemsFragment.onActivityResult(requestCode,
                        RESULT_OK, null);
            }
        } else if (requestCode == Constants.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                cameraItemsFragment.onActivityResult(requestCode,
                        RESULT_OK, null);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: " + requestCode + " result code " + resultCode);
    }
}
