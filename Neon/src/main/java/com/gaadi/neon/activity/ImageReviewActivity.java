package com.gaadi.neon.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;

import com.gaadi.neon.adapter.ImagesReviewViewPagerAdapter;
import com.gaadi.neon.enumerations.LibraryMode;
import com.gaadi.neon.events.ImageEditEvent;
import com.gaadi.neon.interfaces.FragmentListener;
import com.gaadi.neon.model.NeonResponse;
import com.gaadi.neon.util.Constants;
import com.gaadi.neon.util.FindLocations;
import com.gaadi.neon.util.NeonImagesHandler;
import com.scanlibrary.R;

public class ImageReviewActivity extends NeonBaseActivity implements View.OnClickListener, FragmentListener {

    private ImagesReviewViewPagerAdapter mPagerAdapter;

    private ViewPager mPager;
    private ImageView viewPagerRightBtn;
    private ImageView viewPagerLeftBtn;
    private boolean singleTagSelection;

    public boolean isSingleTagSelection() {
        return singleTagSelection;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_review);
        toolbar = (Toolbar) findViewById(R.id.tool_bar);

        if(toolbar!=null){
            setSupportActionBar(toolbar);
            toolbar.setNavigationIcon(R.drawable.ic_left_arrow);
        }
        if (getSupportActionBar() != null){
            getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_left_arrow);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mPager = (ViewPager) findViewById(R.id.pager);
        viewPagerLeftBtn = (ImageView) findViewById(R.id.view_pager_leftbtn);
        viewPagerRightBtn = (ImageView) findViewById(R.id.view_pager_rightbtn);
        viewPagerRightBtn.setOnClickListener(this);
        viewPagerLeftBtn.setOnClickListener(this);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.image_review);
        Intent intent = getIntent();

        singleTagSelection = intent.getBooleanExtra(Constants.SINGLE_TAG_SELECTION, false);
        int position = intent.getIntExtra(Constants.IMAGE_REVIEW_POSITION, 0);
        if (position == 0) {
            viewPagerLeftBtn.setVisibility(View.GONE);
        }
        if (position == NeonImagesHandler.getSingletonInstance().getImagesCollection().size() - 1) {
            viewPagerRightBtn.setVisibility(View.GONE);
        }

        if(NeonImagesHandler.getSingletonInstance().getLivePhotosListener()!=null){
            viewPagerLeftBtn.setVisibility(View.GONE);
            viewPagerRightBtn.setVisibility(View.GONE);
        }

        mPagerAdapter = new ImagesReviewViewPagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        mPager.setCurrentItem(position);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setArrowButton(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });


    }

    private void setArrowButton(int position) {
        if (position == 0 || NeonImagesHandler.getSingleonInstance().getImagesCollection().size() == 1) {
            viewPagerLeftBtn.setVisibility(View.GONE);
        } else {
            viewPagerLeftBtn.setVisibility(View.VISIBLE);
        }
        if (position == NeonImagesHandler.getSingleonInstance().getImagesCollection().size() - 1 ||
                NeonImagesHandler.getSingleonInstance().getImagesCollection().size() == 1) {
            viewPagerRightBtn.setVisibility(View.GONE);
        } else {
            viewPagerRightBtn.setVisibility(View.VISIBLE);
        }
    }

    public void getFragmentChanges(ImageEditEvent event) {
        boolean isViewDirty = false;
        if (event.getImageEventType() == ImageEditEvent.EVENT_DELETE) {
            isViewDirty = true;
            NeonImagesHandler.getSingletonInstance().removeFromCollection(event.getPosition());
            mPagerAdapter.setPagerItems();
            if (NeonImagesHandler.getSingletonInstance().getImagesCollection().size() == 0) {
                super.onBackPressed();
            }
            setArrowButton(mPager.getCurrentItem());
        } else if (event.getImageEventType() == ImageEditEvent.EVENT_ROTATE) {
            isViewDirty = true;

        } else if (event.getImageEventType() == ImageEditEvent.EVENT_REPLACED_BY_CAM) {
            isViewDirty = true;
        } else if (event.getImageEventType() == ImageEditEvent.EVENT_REPLACED_BY_GALLERY) {
            isViewDirty = true;
        } else if (event.getImageEventType() == ImageEditEvent.EVENT_TAG_CHANGED) {
            NeonImagesHandler.getSingletonInstance().getImagesCollection().set(event.getPosition(), event.getModel());
        }
    }


    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.image_review_toolbar_doneBtn) {
            /*Intent i = new Intent();
            i.putExtra(Constants.IMAGE_MODEL_FOR__REVIEW, gallaryItemsFiles);
            setResult(RESULT_OK, i);*/
            finish();
        } else if (id == R.id.view_pager_leftbtn) {
            int position = mPager.getCurrentItem();
            if (position > 0) {
                position--;
                mPager.setCurrentItem(position);
            }


        } else if (id == R.id.view_pager_rightbtn) {
            int position = mPager.getCurrentItem();
            if (position < NeonImagesHandler.getSingletonInstance().getImagesCollection().size() - 1) {
                position++;
                mPager.setCurrentItem(position);
            }


        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_view_pager, menu);
        if(NeonImagesHandler.getSingletonInstance().getLivePhotosListener()!=null && menu!=null){
            menu.findItem(R.id.menu_done).setVisible(false);
            menu.findItem(R.id.menu_retry).setVisible(true);
            menu.findItem(R.id.menu_apply).setVisible(true);
        }

        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public void onBackPressed() {
        if(NeonImagesHandler.getSingletonInstance().getLivePhotosListener()!=null){
            NeonImagesHandler.getSingletonInstance().removeFromCollection(NeonImagesHandler.getSingletonInstance().getImagesCollection().size() - 1);
        }
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:

                if(NeonImagesHandler.getSingletonInstance().getLivePhotosListener()!=null){
                    NeonImagesHandler.getSingletonInstance().removeFromCollection(NeonImagesHandler.getSingletonInstance().getImagesCollection().size() - 1);
                }
                super.onBackPressed();
                return true;

        }
        if (item.getItemId() == R.id.menu_done) {
            /*Intent i = new Intent();
            i.putExtra(Constants.IMAGE_MODEL_FOR__REVIEW, gallaryItemsFiles);
            setResult(RESULT_OK, i);*/

            super.onBackPressed();
            return true;
        }
        if(item.getItemId()==R.id.menu_apply){
            boolean isUpdate=true;
            if(NeonImagesHandler.getSingletonInstance().getLivePhotosListener()!=null){
                int pos=NeonImagesHandler.getSingletonInstance().getImagesCollection().size() - 1;
                isUpdate= NeonImagesHandler.getSingletonInstance().getLivePhotoNextTagListener().updateExifInfo(NeonImagesHandler.getSingletonInstance().getImagesCollection().get(pos));
                if(isUpdate){
                    NeonResponse neonResponse = new NeonResponse();
                    neonResponse.setImageCollection(NeonImagesHandler.getSingletonInstance().getImagesCollection());
                    NeonImagesHandler.getSingletonInstance().getLivePhotosListener().onLivePhotoCollected(neonResponse);
                    NeonImagesHandler.getSingletonInstance().getLivePhotoNextTagListener().onNextTag();
                }
                else{
                    Toast.makeText(this,"Finding location.Please wait.",Toast.LENGTH_SHORT).show();
                }
            }
            if(isUpdate)
            super.onBackPressed();
            return true;
        }
        if (item.getItemId() == R.id.menu_retry) {
            if(NeonImagesHandler.getSingletonInstance().getLivePhotosListener()!=null){
                NeonImagesHandler.getSingletonInstance().removeFromCollection(NeonImagesHandler.getSingletonInstance().getImagesCollection().size() - 1);
            }
            super.onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
