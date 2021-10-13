package com.gaadi.neon.fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.gaadi.neon.activity.CameraActivity1;
import com.gaadi.neon.activity.GalleryActivity;
import com.gaadi.neon.activity.ImageReviewActivity;
import com.gaadi.neon.adapter.PhotosGridAdapter;
import com.gaadi.neon.interfaces.UpdateSelection;
import com.gaadi.neon.util.ApplicationController;
import com.gaadi.neon.util.Constants;
import com.gaadi.neon.util.FileInfo;
import com.gaadi.neon.util.NeonConstants;
import com.gaadi.neon.util.NeonUtils;
import com.gaadi.neon.util.PhotoParams;
import com.scanlibrary.R;

import java.util.ArrayList;

/**
 * Created by Lakshay
 *
 * @since 13-02-2015
 */
public class NeutralFragment extends Fragment implements View.OnClickListener, UpdateSelection,
        AdapterView.OnItemClickListener {
    public static final String ADD_PHOTOS = "addPhotos";
    public static final String UPDATE_GRID = "update_grid";
    public static final String IMG_LOAD_DEF_BIG = "IMG_LOAD_DEF_BIG";
    public static final String IMG_LOAD_DEF_SMALL = "IMG_LOAD_DEF_SMALL";
    public static final String PHOTO_PARAMS = "photoParams";
    private static final int OPEN_IMAGE_VIEW_PAGER_SCREEN = 102;
    private static final String SELECTED_IMAGES = "alreadySelected";
    public static int CODE_CAMERA = 148;
    public static int loadDefImgBig;
    public static int loadDefImgSmall;
    private int maxPhotos = 20;
    private TextView tvCount;
    private GridView gvPhotos;
    private PhotosGridAdapter photosGridAdapter;
    private PhotoParams params;
    private ArrayList<FileInfo> cameraItemsFiles = new ArrayList<>();
    private Context context;

    public static NeutralFragment newInstance(Context context, PhotoParams params,
                                              ArrayList<?> uploadedImages, int loadDefaultResBig, int loadDefaultResSmall) {
        NeutralFragment fragment = new NeutralFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(PHOTO_PARAMS, params);
        bundle.putSerializable(SELECTED_IMAGES, uploadedImages);
        bundle.putInt(IMG_LOAD_DEF_BIG, loadDefaultResBig);
        bundle.putInt(IMG_LOAD_DEF_SMALL, loadDefaultResSmall);
        fragment.setArguments(bundle);
        fragment.context = context;
        return fragment;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
       Intent intent = new Intent(getActivity(), ImageReviewActivity.class);
        intent.putExtra(Constants.IMAGE_MODEL_FOR__REVIEW, cameraItemsFiles);
        intent.putExtra(Constants.IMAGE_REVIEW_POSITION, position);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivityForResult(intent, OPEN_IMAGE_VIEW_PAGER_SCREEN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             @Nullable
                                     ViewGroup container,
                             @Nullable
                                     Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.neutral_fragment, container, false);

        rootView.findViewById(R.id.addPhotoCamera).setOnClickListener(this);
        rootView.findViewById(R.id.addPhotoGallary).setOnClickListener(this);
        rootView.findViewById(R.id.done).setOnClickListener(this);
        rootView.findViewById(R.id.ivBack).setOnClickListener(this);
        tvCount = (TextView) rootView.findViewById(R.id.photosCount);


        params = (PhotoParams) getArguments().getSerializable(PHOTO_PARAMS);
        loadDefImgBig = getArguments().getInt(IMG_LOAD_DEF_BIG);
        loadDefImgSmall = getArguments().getInt(IMG_LOAD_DEF_SMALL);
        setUpPhotosGrid(rootView);
        if (params.getNoOfPhotos() > 0) {
            maxPhotos = params.getNoOfPhotos();
        }

        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        tvCount.setText(String.valueOf(cameraItemsFiles.size()));
    }

    private void setUpPhotosGrid(View rootView) {
        gvPhotos = (GridView) rootView.findViewById(R.id.gvImages);
        //photosGridAdapter = new PhotosGridAdapter(getActivity(), cameraItemsFiles, this, loadDefImgSmall);

        gvPhotos.setAdapter(photosGridAdapter);
        // gvPhotos.setOnItemLongClickListener(this);
        gvPhotos.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent intent = new Intent(getActivity(), ImageReviewActivity.class);
                 //intent.putExtra(Constants.IMAGE_TAGS_FOR_REVIEW, mImageTags);
                intent.putExtra(Constants.IMAGE_MODEL_FOR__REVIEW, cameraItemsFiles);
                intent.putExtra(Constants.IMAGE_REVIEW_POSITION, position);
                //intent.putExtra(Constants.SINGLE_TAG_SELECTION,singleTagSelection);
                //intent.putExtra(Constants.ALREADY_SELECTED_TAGS,alreadySelectedTags);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivityForResult(intent, OPEN_IMAGE_VIEW_PAGER_SCREEN);
            }
        });

       /* gvPhotos.setOnDropListener(new DynamicGridView.OnDropListener()
        {
            @Override
            public void onActionDrop()
            {
                gvPhotos.stopEditMode();
            }
        });
        gvPhotos.setOnDragListener(new DynamicGridView.OnDragListener()
        {
            @Override
            public void onDragStarted(int position)
            {
            }

            @Override
            public void onDragPositionsChanged(int oldPosition, int newPosition)
            {

                if(newPosition < cameraItemsFiles.size())
                {
                    FileInfo old = cameraItemsFiles.remove(oldPosition);
                    cameraItemsFiles.add(newPosition, old);
                }
            }
        });*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        cameraItemsFiles.clear();
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ArrayList<FileInfo> cameraList;
        ArrayList<FileInfo> galleryList;
        assert getView() != null;
        switch (requestCode) {
            case 148:
                if (data != null) {
                    cameraList = (ArrayList<FileInfo>) data.getSerializableExtra(NeonConstants.COLLECTED_IMAGES);
                    updateGrid(cameraList, ADD_PHOTOS);
                }
                break;

            case 256:
                if (data != null) {
                    galleryList = (ArrayList<FileInfo>) data.getSerializableExtra(GalleryActivity.GALLERY_SELECTED_PHOTOS);
                    setSource(galleryList, FileInfo.SOURCE.PHONE_GALLERY);
                    checkForDeletedFiles(cameraItemsFiles);
                    updateGrid(galleryList, ADD_PHOTOS);
                }
                break;

            case Constants.REQUEST_PERMISSION_CAMERA:
                onClick(getView().findViewById(R.id.addPhotoCamera));
                break;

            case Constants.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE:
                onClick(getView().findViewById(R.id.addPhotoGallary));
                break;
            case OPEN_IMAGE_VIEW_PAGER_SCREEN:
                galleryList = (ArrayList<FileInfo>) data.getSerializableExtra(Constants.IMAGE_MODEL_FOR__REVIEW);
                setSource(galleryList, FileInfo.SOURCE.PHONE_GALLERY);
                checkForDeletedFiles(galleryList);
                updateGrid(galleryList, ADD_PHOTOS);
              /* if(resultCode == ScanConstants.RESULT_FROM_IMAGE_REVIEW_ACTIVITY)
              {
                    if(null != data)
                    {
                        int index = data.getIntExtra(ScanConstants.IMAGE_INDEX_SENT_FOR_CROPPING, 0);
                        cameraItemsFiles.set(index, (FileInfo) data.getSerializableExtra(ScanConstants.IMAGE_RECEIVED_AFTER_CROPPING));
                        photosGridAdapter.set(cameraItemsFiles);
                        photosGridAdapter.notifyDataSetChanged();
                        gvPhotos.invalidate();
                    }
                }*/
                break;
        }
    }

    //To remove files from main adapter which were unselected from the gallery folder.
    private void checkForDeletedFiles(ArrayList<FileInfo> cameraItemsFiles) {
        ArrayList<FileInfo> deleteFiles = new ArrayList<>();
        for (FileInfo fileInfo : cameraItemsFiles) {
            if (fileInfo.getSource() == FileInfo.SOURCE.PHONE_GALLERY) {
                if ((ApplicationController.selectedFiles != null) && !ApplicationController.selectedFiles.contains(fileInfo.getFilePath())) {
                    deleteFiles.add(fileInfo);
                }
            }
        }
        cameraItemsFiles.removeAll(deleteFiles);
    }

    //To set the source of the image
    private void setSource(ArrayList<FileInfo> list1, FileInfo.SOURCE phoneGallery) {
        for (FileInfo fileInfo : list1) {
            fileInfo.setSource(phoneGallery);
        }
    }

    private void updateGrid(ArrayList<FileInfo> listAdd, String action) {
        if (action.equals(ADD_PHOTOS)) {
            NeonUtils.removeFileInfo(listAdd, cameraItemsFiles);
            cameraItemsFiles.addAll(listAdd);
            //photosGridAdapter.set(cameraItemsFiles);
            photosGridAdapter.notifyDataSetChanged();
        }else if(action.equals(UPDATE_GRID)){
            cameraItemsFiles.clear();
            cameraItemsFiles.addAll(listAdd);
           // photosGridAdapter.updateGridImages(listAdd);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.addPhotoCamera) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !NeonUtils.checkForPermission(context,
                    new String[]{Manifest.permission.CAMERA,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    Constants.REQUEST_PERMISSION_CAMERA,
                    "Camera and Storage")) {
                return;
            }
            if (cameraItemsFiles.size() >= maxPhotos) {
                Toast.makeText(getActivity().getApplicationContext(), "Maximum photos can be : " + maxPhotos, Toast.LENGTH_SHORT).show();
                return;
            }
            //            imagesHandler.gaHandler(Constants.SCREEN_CAMERA_ITEMS, Constants.CATEGORY_CAMERA, Constants.ACTION_CLICK, Constants.TAKE_PHOTO, 0L);
            Intent intent = new Intent(getActivity(), CameraActivity1.class);
            intent.putExtra(GalleryActivity.MAX_COUNT, maxPhotos - cameraItemsFiles.size());
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(PHOTO_PARAMS, params);
            startActivityForResult(intent, CODE_CAMERA);
        } else if (v.getId() == R.id.addPhotoGallary) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                    !NeonUtils.checkForPermission(context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                            Constants.REQUEST_PERMISSION_READ_EXTERNAL_STORAGE, "Storage")) {
                return;
            }
            if (cameraItemsFiles.size() >= maxPhotos) {
                Toast.makeText(getActivity().getApplicationContext(), "Maximum photos can be : " + maxPhotos, Toast.LENGTH_SHORT).show();
                return;
            }
            //            imagesHandler.gaHandler(Constants.SCREEN_CAMERA_ITEMS, Constants.CATEGORY_GALLERY, Constants.ACTION_CLICK, Constants.TAKE_FROM_GALLERY, 0L);
            Intent intent1 = new Intent(getActivity(), GalleryActivity.class);
            intent1.putExtra(PHOTO_PARAMS, params);
            //intent1.putExtra(GalleryActivity.MAX_COUNT, maxPhotos-cameraItemsFiles.size());
            intent1.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(intent1, 256);
        } else if (v.getId() == R.id.done) {
            getActivity().setResult(Activity.RESULT_OK, new Intent().putExtra(NeonConstants.COLLECTED_IMAGES, cameraItemsFiles));
            getActivity().finish();
        } else if (v.getId() == R.id.ivBack) {
            try {
                if (ApplicationController.selectedFiles != null) {
                    ApplicationController.selectedFiles.clear();
                }
            } catch (Exception e) {
                Log.d("", "onClick: " + e.getLocalizedMessage());
            } finally {
                getActivity().finish();
            }
        }
    }

 /*   @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id)
    {
        gvPhotos.startEditMode(position);
        return true;
    }*/

    @Override
    public void updateSelected(String imagePath, Boolean selected) {
        if (!selected) {
            for (FileInfo fileInfo1 : cameraItemsFiles) {
                if (fileInfo1.getFilePath().equals(imagePath)) {
                    cameraItemsFiles.remove(fileInfo1);
//                    if(fileInfo1.getFromServer())
//                    {
//                        deletedImages.add(fileInfo1);
//                    }
                    if (ApplicationController.selectedFiles != null) {
                        ApplicationController.selectedFiles.remove(imagePath);
                    }
                    //                    ApplicationController.selectedFilesMark.remove(imagePath);
                    break;
                }
            }

            tvCount.setText(String.valueOf(cameraItemsFiles.size()));
        }
    }
}

