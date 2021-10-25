package com.gaadi.neon.activity;

import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.gaadi.neon.util.Constants;
import com.scanlibrary.R;

import java.io.File;

/**
 * @author lakshaygirdhar
 * @since 13-08-2016
 */
public class ReviewImageActivity extends AppCompatActivity implements View.OnClickListener {
    protected Toolbar toolbar;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        imagePath = extras.getString(Constants.IMAGE_PATH);
        String imageName = "";
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.review_image_activity);
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(R.id.relativeLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitle("");
        TextView tvImageName = (TextView) findViewById(R.id.imageName);
        boolean flag = extras.getBoolean(Constants.FLAG);

        if (flag) {
            toolbar.setVisibility(View.VISIBLE);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
            tvImageName.setVisibility(View.GONE);
            final Drawable upArrow = ContextCompat.getDrawable(ReviewImageActivity.this, R.drawable.ic_action_done);
            upArrow.setColorFilter(ContextCompat.getColor(ReviewImageActivity.this, R.color.white), PorterDuff.Mode.SRC_ATOP);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
            relativeLayout.setVisibility(View.GONE);
        } else {
            toolbar.setVisibility(View.GONE);
            tvImageName.setVisibility(View.VISIBLE);
            ImageView bDone = (ImageView) findViewById(R.id.bDone);
            ImageView bCancel = (ImageView) findViewById(R.id.bCancel);
            relativeLayout.setVisibility(View.VISIBLE);
            bCancel.setOnClickListener(this);
            bDone.setOnClickListener(this);
        }

        ImageView ivReview = (ImageView) findViewById(R.id.ivReviewImage);
        tvImageName.setVisibility(View.VISIBLE);
        tvImageName.setText(imageName);

        if (imagePath != null) {
            Glide.with(this).load(imagePath).into(ivReview);
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.bDone) {
            FragmentManager manager = getSupportFragmentManager();
            manager.popBackStackImmediate();
            Intent intent = new Intent();
            intent.putExtra(Constants.IMAGE_PATH, imagePath);
            setResult(RESULT_OK, intent);
            finish();
        } else if (id == R.id.bCancel) {
            Uri uri = Uri.parse(imagePath);
            File fdelete = new File(uri.getPath());
            if (fdelete.exists()) {
                if (fdelete.delete()) {
                    System.out.println("file Deleted :" + imagePath);
                } else {
                    System.out.println("file not Deleted :" + imagePath);
                }
            }
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
            setResult(RESULT_CANCELED);
            finish();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return true;
    }


}
