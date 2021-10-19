package com.gaadi.neon.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.gaadi.neon.fragment.CameraFragment1;
import com.gaadi.neon.fragment.NeutralFragment;
import com.gaadi.neon.model.ImageTagModel;
import com.gaadi.neon.util.AnimationUtils;
import com.gaadi.neon.util.Constants;
import com.gaadi.neon.util.FileInfo;
import com.gaadi.neon.util.FileUtils;
import com.gaadi.neon.util.NeonConstants;
import com.gaadi.neon.util.PhotoParams;
import com.scanlibrary.R;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
/**
 * @author lakshaygirdhar
 * @version 1.0
 * @since 8/9/16
 */
@SuppressWarnings("deprecation,unchecked")
public class CameraActivity1 extends AppCompatActivity implements CameraFragment1.PictureTakenListener, View.OnClickListener
{
    public static final int GALLERY_PICK = 99;
    private ArrayList<FileInfo> imagesList = new ArrayList<>();
    private ImageView buttonCapture;
    private ImageView buttonDone;
    private CameraFragment1 mFragment;
    private PhotoParams photoParams;
    private int maxNumberOfImages;
    private LinearLayout scrollView;
    private TextView tvImageName;
    private String imageName;
    private ImageView buttonGallery;
    private ArrayList<ImageTagModel> mTagList;
    private int currentTag;
    private TextView tvTag;
    private TextView tvNext;
    private HashMap<ImageTagModel, List<FileInfo>> imagesWithTags;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.camera_activity_layout);

        initialize();

        customize();

        enableDoneButton(false);

        //To make sure that name appears only after animation ends
        new Handler().postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                if(maxNumberOfImages == 0)
                {
                    buttonDone.setVisibility(View.VISIBLE);
                    buttonDone.setOnClickListener(CameraActivity1.this);
                }
                if(photoParams.getImageName() != null && !"".equals(photoParams.getImageName()))
                {
                    tvImageName.setVisibility(View.VISIBLE);
                    tvImageName.setText(String.valueOf(photoParams.getImageName()));
                    tvImageName.setOnClickListener(CameraActivity1.this);
                }
            }
        }, 1000);

        //mFragment = CameraFragment1.getInstance(photoParams);
        FragmentManager manager = getSupportFragmentManager();
        manager.beginTransaction().replace(R.id.content_frame, mFragment).commit();

        //for handling screen orientation
        if(savedInstanceState != null)
        {
            imagesList = (ArrayList<FileInfo>) savedInstanceState.getSerializable(Constants.IMAGES_SELECTED);
            addInScrollView(imagesList);
        }
    }

    private void initialize()
    {
        buttonCapture = (ImageView) findViewById(R.id.buttonCapture);
        buttonGallery = (ImageView) findViewById(R.id.buttonGallery);
        buttonDone = (ImageView) findViewById(R.id.buttonDone);
        scrollView = (LinearLayout) findViewById(R.id.imageHolderView);
        tvImageName = (TextView) findViewById(R.id.tvImageName);
        tvTag = (TextView) findViewById(R.id.tvTag);
        tvNext = (TextView) findViewById(R.id.tvSkip);
        findViewById(R.id.rlTags).setOnClickListener(this);
        tvNext.setOnClickListener(this);

        buttonCapture.setOnClickListener(this);
        buttonGallery.setOnClickListener(this);
    }

    private void customize()
    {
        photoParams = (PhotoParams) getIntent().getSerializableExtra(NeutralFragment.PHOTO_PARAMS);
        if(photoParams.isTagEnabled())
        {
            tvImageName.setVisibility(View.GONE);
            mTagList = photoParams.getImageTags();
            if(mTagList == null)
            {
                Toast.makeText(this, getString(R.string.no_tags_sent), Toast.LENGTH_SHORT).show();
                return;
            }
            setTag(mTagList.get(currentTag));
            imagesWithTags = new HashMap<>();
        }
        else
        {
            findViewById(R.id.rlTags).setVisibility(View.GONE);
        }

        maxNumberOfImages = photoParams.getNoOfPhotos();
        if(maxNumberOfImages == 1)
            buttonDone.setVisibility(View.GONE);
        imageName = photoParams.getImageName();

        boolean isGalleryEnabled = photoParams.isGalleryFromCameraEnabled();

        if(!isGalleryEnabled)
        {
            buttonGallery.setVisibility(View.GONE);
        }
    }

    public void setTag(ImageTagModel imageTagModel)
    {
        tvTag.setText(imageTagModel.getTagName());
    }

    public ImageTagModel getNextTag()
    {
        if(photoParams.isTagEnabled())
        {
            if(mTagList.get(currentTag).isMandatory())
            {
                if(imagesWithTags.get(mTagList.get(currentTag)) == null || imagesWithTags.get(mTagList.get(currentTag)).size() == 0)
                {
                    Toast.makeText(this, String.format(getString(R.string.tag_mandatory_error), mTagList.get(currentTag).getTagName()),
                                   Toast.LENGTH_SHORT).show();
                }
                else
                {
                    currentTag++;
                }
            }
            else
            {
                currentTag++;
            }
        }
        else
        {
            if(currentTag < mTagList.size() && !mTagList.get(currentTag).isMandatory())
            {
                currentTag++;
            }
            else
            {
                Toast.makeText(this, String.format(getString(R.string.tag_mandatory_error), mTagList.get(currentTag).getTagName()),
                               Toast.LENGTH_SHORT).show();
            }
        }

        if(currentTag == mTagList.size() - 1)
        {
            tvNext.setText(getString(R.string.finish));
        }
        if(currentTag == mTagList.size())
        {
            if(photoParams.isTagEnabled())
            {
                onPicturesFinalized(imagesWithTags);
            }
            else
            {
                onPicturesFinalized(imagesList);
            }
            return mTagList.get(currentTag - 1);
        }
        return mTagList.get(currentTag);
    }

    public ImageTagModel getPreviousTag()
    {
        if(currentTag > 0)
        {
            currentTag--;
        }
        return mTagList.get(currentTag);
    }

    private void enableDoneButton(boolean enable)
    {
        buttonCapture.setImageResource(enable ? R.drawable.camera_switch : R.drawable.ic_camera);
        buttonCapture.setTag(enable ? getString(R.string.done) : getString(R.string.capture));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
        outState.putSerializable(Constants.IMAGES_SELECTED, imagesList);
    }

    private void handleCameraButtons(FileInfo fileInfo)
    {
        if(maxNumberOfImages == 1)
        {
            buttonCapture.setTag("done");
            onClick(buttonCapture);
        }
        else
        {
            if(imagesList.size() >= 1 && photoParams.isCameraHorizontalPreviewEnabled())
            {
                scrollView.setVisibility(View.VISIBLE);
                addInScrollView(fileInfo);
            }
            else
            {
                scrollView.setVisibility(View.GONE);
            }

            if(maxNumberOfImages > 0)
            {
                updateView(imagesList.size() < maxNumberOfImages);
            }
            mFragment.startPreview();
            buttonCapture.setEnabled(true);
        }
    }

    private void updateView(boolean status)
    {
        if(!status)
        {
            buttonCapture.setVisibility(View.GONE);
        }
        else
        {
            buttonCapture.setVisibility(View.VISIBLE);
        }
        buttonDone.setVisibility(View.VISIBLE);
        tvImageName.setText(status ? imageName : getString(R.string.press_done));
    }

    //It is called when configuration(orientation) of screen changes
    private void addInScrollView(ArrayList<FileInfo> infos)
    {
        if(infos != null && infos.size() > 0)
        {
            for(FileInfo info : infos)
            {
                scrollView.addView(createImageView(info));
            }
            scrollView.setVisibility(View.VISIBLE);
        }
    }

    private void addInScrollView(FileInfo info)
    {
        scrollView.addView(createImageView(info));
        scrollView.setVisibility(View.VISIBLE);
    }

    private View createImageView(final FileInfo info)
    {
        final File file = new File(info.getFilePath());
        if(!file.exists())
        {
            return null;
        }
        final View outerView = View.inflate(this, R.layout.camera_priority_overlay, null);
        outerView.findViewById(R.id.ivRemoveImage).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                scrollView.removeView(outerView);
                imagesList.remove(info);
                if(maxNumberOfImages > 0)
                {
                    updateView(imagesList.size() < maxNumberOfImages);
                }
                if(imagesList.size() < 1)
                {
                    buttonDone.setVisibility(View.GONE);
                    scrollView.setVisibility(View.GONE);
                }
            }
        });

        Glide.with(this)
             .load("file://" + info.getFilePath())
             .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(new DrawableTransitionOptions().crossFade())
             .centerCrop()
             .placeholder(R.drawable.image_load_default_small)
             .into((ImageView) outerView.findViewById(R.id.ivCaptured));/**/
        return outerView;
    }

    @Override
    public void onPictureTaken(Uri uri)
    {
        FileInfo fileInfo = new FileInfo();
        String filePath =  FileUtils.getPath(this, uri);
        fileInfo.setFilePath(filePath);
        fileInfo.setFileName(filePath.substring(filePath.lastIndexOf("/") + 1));
        fileInfo.setDisplayName(imageName);
        fileInfo.setSource(FileInfo.SOURCE.PHONE_CAMERA);
        if(photoParams.isTagEnabled())
        {

            if(imagesWithTags.get(mTagList.get(currentTag)) == null)
            {
                List<FileInfo> fileInfos = new ArrayList<>();
                fileInfos.add(fileInfo);
                imagesWithTags.put(mTagList.get(currentTag), fileInfos);
            }
            else
            {
                List<FileInfo> listFiles = imagesWithTags.get(mTagList.get(currentTag));
                listFiles.add(fileInfo);
                imagesWithTags.put(mTagList.get(currentTag), listFiles);
            }
        }
        else
        {
            imagesList.add(fileInfo);
            handleCameraButtons(fileInfo);
        }
    }

    @Override
    public void onPicturesFinalized(ArrayList<FileInfo> infos)
    {
        getSupportFragmentManager().popBackStackImmediate();

        if(infos.size() > 0)
        {
            setResult(RESULT_OK, new Intent().putExtra(NeonConstants.COLLECTED_IMAGES, infos));
            finish();
        }
        else
        {
            Toast.makeText(this, getString(R.string.click_photo), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPicturesFinalized(Map<ImageTagModel, List<FileInfo>> filesMap)
    {
        setResult(RESULT_OK, new Intent().putExtra(NeonConstants.COLLECTED_IMAGES, imagesWithTags));
        finish();
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
        }
    }

    @Override
    public void onClick(View v)
    {
        if(v.getId() == R.id.buttonCapture)
        {
            if(v.getTag().equals("capture"))
            {
                mFragment.clickPicture();
            }
            else if(v.getTag().equals("done"))
            {
                if(photoParams.isTagEnabled())
                {
                    onPicturesFinalized(imagesWithTags);
                }
                else
                {
                    onPicturesFinalized(imagesList);
                }
            }
        }
        else if(v.getId() == R.id.buttonGallery)
        {
            Intent intent = new Intent(this, GalleryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(GalleryActivity.MAX_COUNT, maxNumberOfImages);
            intent.putExtra(NeonConstants.PHOTO_PARAMS, photoParams);
            startActivityForResult(intent, GALLERY_PICK);
        }
        else if(v.getId() == R.id.buttonDone)
        {
            if(!photoParams.isTagEnabled())
            {
                if(imagesList.size() == 0)
                {
                    Toast.makeText(this, getString(R.string.no_images), Toast.LENGTH_SHORT).show();
                }
                else
                {
                    buttonCapture.setTag("done");
                    onClick(buttonCapture);
                }
            }
            else
            {
                buttonCapture.setTag("done");
                onClick(buttonCapture);
            }
        }
        else if(v.getId() == R.id.tvSkip)
        {
            setTag(getNextTag());
            AnimationUtils.translateOnXAxis(tvTag, 200, 0);
        }
    }
}
