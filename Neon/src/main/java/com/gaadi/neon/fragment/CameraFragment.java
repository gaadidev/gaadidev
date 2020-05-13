package com.gaadi.neon.fragment;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.gaadi.neon.activity.GalleryActivity;
import com.gaadi.neon.adapter.FlashModeRecyclerHorizontalAdapter;
import com.gaadi.neon.util.CameraPreview;
import com.gaadi.neon.util.Constants;
import com.gaadi.neon.util.DrawingView;
import com.gaadi.neon.util.FileInfo;
import com.gaadi.neon.util.NeonConstants;
import com.gaadi.neon.util.NeonUtils;
import com.gaadi.neon.util.PhotoParams;
import com.gaadi.neon.util.PrefsUtils;
import com.scanlibrary.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions.withCrossFade;

/**
 * Created by Lakshay
 * @since 13-08-2016
 * @version 1.0
 *
 */
@SuppressWarnings("deprecation,unchecked")
public class CameraFragment extends Fragment implements View.OnClickListener, View.OnTouchListener, Camera.PictureCallback {

    public static final int GALLERY_PICK = 99;

    private static final String TAG = "CameraFragment";
    private static final int REQUEST_REVIEW = 100;
    private PhotoParams mPhotoParams;
    private String imageName;
    private int maxNumberOfImages;
    private DrawingView drawingView;
    private ImageView buttonCapture, buttonGallery, buttonDone;
    private TextView tvImageName;
    private ImageView currentFlashMode;
    private ArrayList<String> supportedFlashModes;

    private RecyclerView rcvFlash;
    private LinearLayout scrollView;
    private ArrayList<FileInfo> imagesList = new ArrayList<>();
    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private boolean readyToTakePicture;
    private FrameLayout mCameraLayout;
    private View fragmentView;
    private Activity mActivity;
    private PictureTakenListener mPictureTakenListener;
    private boolean permissionAlreadyRequested;

    private boolean useFrontFacingCamera;
    private boolean enableCapturedReview;
    private float mDist;
    private PhotoParams.CameraFacing cameraFacing;

    public interface PictureTakenListener {
         void onPictureTaken(String filePath);
         void onPicturesFinalized(ArrayList<FileInfo> infos);
         void sendPictureForCropping(File file);
    }

    public static CameraFragment getInstance(PhotoParams photoParams) {
        CameraFragment fragment = new CameraFragment();
        Bundle extras = new Bundle();
        extras.putSerializable(NeonConstants.PHOTO_PARAMS, photoParams);
        fragment.setArguments(extras);
        return fragment;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mPictureTakenListener = (PictureTakenListener) activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentView = LayoutInflater.from(getContext()).inflate(R.layout.camera_fragment, container, false);
        mPhotoParams = (PhotoParams) getArguments().getSerializable(NeonConstants.PHOTO_PARAMS);
        mActivity = getActivity();
        if(mPhotoParams != null){
            imageName = mPhotoParams.getImageName();
            maxNumberOfImages = mPhotoParams.getNoOfPhotos();
            enableCapturedReview = mPhotoParams.isEnableCapturedReview();
            PhotoParams.CameraOrientation orientation = mPhotoParams.getOrientation();
            cameraFacing = mPhotoParams.getCameraFace();
            Log.d(TAG, "onCreateView: " + cameraFacing);
            boolean isGalleryEnabled = mPhotoParams.isGalleryFromCameraEnabled();
            //View to add rectangle on tap to focus
            drawingView = new DrawingView(mActivity);

            setOrientation(mActivity, orientation);

            LinearLayoutManager layoutManager
                    = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);

            currentFlashMode = (ImageView) fragmentView.findViewById(R.id.currentFlashMode);

            rcvFlash = (RecyclerView)fragmentView.findViewById(R.id.flash_listview);
            rcvFlash.setLayoutManager(layoutManager);

            buttonCapture = (ImageView) fragmentView.findViewById(R.id.buttonCapture);
            buttonGallery = (ImageView) fragmentView.findViewById(R.id.buttonGallery);
            buttonDone = (ImageView) fragmentView.findViewById(R.id.buttonDone);
            tvImageName = (TextView) fragmentView.findViewById(R.id.imageName);

            ImageView mSwitchCamera = (ImageView) fragmentView.findViewById(R.id.switchCamera);
            if(NeonUtils.isFrontCameraAvailable() != Camera.CameraInfo.CAMERA_FACING_FRONT) {
                mSwitchCamera.setVisibility(View.GONE);
                useFrontFacingCamera = false;
            }
            if (!isGalleryEnabled) {
                buttonGallery.setVisibility(View.GONE);
            }

            mSwitchCamera.setOnClickListener(this);

            scrollView = (LinearLayout) fragmentView.findViewById(R.id.imageHolderView);

            //for handling screen orientation
            if (savedInstanceState != null) {
                Log.e(Constants.TAG, "savedInstanceState not null");
                imagesList = (ArrayList<FileInfo>) savedInstanceState.getSerializable(Constants.IMAGES_SELECTED);
                addInScrollView(imagesList);
            }
            fragmentView.setOnTouchListener(this);

        } else {
            Toast.makeText(getContext(),getString(R.string.pass_params),Toast.LENGTH_SHORT).show();
        }
        return fragmentView;
    }

    @Override
    public void onPause()
    {
        super.onPause();
        try
        {
            mCamera.setPreviewCallback(null);
            mCameraPreview.getHolder().removeCallback(mCameraPreview);
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
            mCameraPreview = null;
        }
        catch(Exception e)
        {
            Log.e(TAG, e.getMessage());
        }
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        ViewGroup.LayoutParams layoutParamsDrawing
                = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT,
                ViewGroup.LayoutParams.FILL_PARENT);

        getActivity().addContentView(drawingView, layoutParamsDrawing);
    }

    private void setFlashLayoutAndMode() {
        // flashLayout=(LinearLayout)view.findViewById(R.id.flashLayout);
        currentFlashMode.setOnClickListener(this);
        String flashMode = PrefsUtils.getStringSharedPreference(getActivity(), Constants.FLASH_MODE, "");
        if (flashMode.equals("")) {
            currentFlashMode.setImageResource(R.drawable.ic_flash_off);
        } else {
            if (supportedFlashModes != null && supportedFlashModes.size() > 0) {
                if (supportedFlashModes.contains(flashMode)) {
                    setFlash(flashMode);
                } else {
                    setFlash(supportedFlashModes.get(0));
                }
            }
        }
    }

    public void setFlash(String mode) {
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(mode);

        if ("off".equals(mode)) {
            currentFlashMode.setImageResource(R.drawable.ic_flash_off);
        } else if ("on".equals(mode)) {
            currentFlashMode.setImageResource(R.drawable.ic_flash_on);
        } else if ("auto".equals(mode)) {
            currentFlashMode.setImageResource(R.drawable.ic_flash_auto);
        } else if ("red-eye".equals(mode)) {
            currentFlashMode.setImageResource(R.drawable.ic_flash_red_eye);
        } else if ("torch".equals(mode)) {
            currentFlashMode.setImageResource(R.drawable.ic_flash_torch);
        }
        else{
            currentFlashMode.setImageResource(R.drawable.ic_flash_off);
        }
        PrefsUtils.setStringSharedPreference(getActivity(), Constants.FLASH_MODE, mode);
        //Toast.makeText(getActivity(),mode,Toast.LENGTH_LONG).show();
        //currentFlashMode.setText("Flash:" + mode);
        mCamera.setParameters(parameters);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {

            if (requestCode == GALLERY_PICK) {
//                if (maxNumberOfImages == 0) {
                imagesList = (ArrayList<FileInfo>) data.getSerializableExtra(GalleryActivity.GALLERY_SELECTED_PHOTOS);
                if (imagesList != null && imagesList.size()>0) {
                    buttonCapture.setTag("done");
                    onClick(buttonCapture);
                    if(enableCapturedReview) {
                        mPictureTakenListener.onPicturesFinalized(imagesList);
                        imagesList.clear();
                    }
                }
            } else if (requestCode == REQUEST_REVIEW) {
                String capturedFilePath = "";
                mPictureTakenListener.onPictureTaken(capturedFilePath);
            }
        } else {
            if (requestCode == REQUEST_REVIEW) {
                readyToTakePicture = true;
                buttonCapture.setEnabled(true);
            } else if (requestCode != 101) {
                mActivity.setResult(resultCode);
                mActivity.finish();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mCamera == null) {
            try {
                if (!permissionAlreadyRequested && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                        && !NeonUtils.checkForPermission(mActivity,
                                                           new String[]{Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                                           Constants.REQUEST_PERMISSION_CAMERA, "Camera and Storage")) {
                    permissionAlreadyRequested = true;
                    return;
                }
                if (cameraFacing == PhotoParams.CameraFacing.FRONT && NeonUtils.isFrontCameraAvailable() == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    Log.d(TAG, "onResume: open front");
                    mCamera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
//                    mSwitchCamera.setVisibility(View.GONE);
                } else {
                    mCamera = Camera.open();
                }

                //To set hardware camera rotation
                setCameraRotation();
                Log.d(TAG, "onResume: setRotation " );
                Camera.Parameters parameters = mCamera.getParameters();
                createSupportedFlashList(parameters);

                setFlashLayoutAndMode();

                mCameraPreview = new CameraPreview(mActivity, mCamera);
                mCameraPreview.setReadyListener(new CameraPreview.ReadyToTakePicture() {
                    @Override
                    public void readyToTakePicture(boolean ready) {
                        readyToTakePicture = ready;
                    }
                });

                mCameraPreview.setOnTouchListener(this);

                mCameraLayout = (FrameLayout) fragmentView.findViewById(R.id.camera_preview);
                mCameraLayout.addView(mCameraPreview);

                //set the screen layout to fullscreen
                mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);

                buttonCapture.setOnClickListener(this);
                enableDoneButton(false);
                buttonGallery.setOnClickListener(this);

            } catch (Exception e) {
                Log.e("Camera Open Exception", "" + e.getMessage());
            }

            //To make sure that name appears only after animation ends
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (maxNumberOfImages == 0) {
                        buttonDone.setVisibility(View.VISIBLE);
                        buttonDone.setOnClickListener(CameraFragment.this);
                    }
                    if (mPhotoParams.getImageName() != null && !"".equals(mPhotoParams.getImageName())) {
                        tvImageName.setVisibility(View.VISIBLE);
                        tvImageName.setText(String.valueOf(mPhotoParams.getImageName()));
                        tvImageName.setOnClickListener(CameraFragment.this);
                    }
                }
            }, 1000);

        } else {
            enableDoneButton(false);
            Log.e(TAG, "camera not null");
        }
    }

    private void createSupportedFlashList(Camera.Parameters parameters) {
        supportedFlashModes = (ArrayList<String>) parameters.getSupportedFlashModes();
        if (supportedFlashModes == null) {
            currentFlashMode.setVisibility(View.GONE);
            rcvFlash.setVisibility(View.GONE);
        } else {
            currentFlashMode.setVisibility(View.VISIBLE);
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.buttonCapture) {
            if (v.getTag().equals("capture")) {
                if (readyToTakePicture) {
                    if(mCamera != null) {
                        mCamera.takePicture(null, null, this);
                    }
                    readyToTakePicture = false;
                    //llActionsCamera.setEnabled(false);
//                    buttonCapture.setEnabled(false);
                    if (maxNumberOfImages == 1)
                        buttonGallery.setEnabled(false);
                    if (maxNumberOfImages > 1 || maxNumberOfImages == 0) {
//                        buttonDone.setVisibility(View.VISIBLE);
                        buttonDone.setOnClickListener(this);
                    }
                }
            } else if (v.getTag().equals("done")) {
                if (imagesList.size() > 0) {
                    mPictureTakenListener.onPicturesFinalized(imagesList);
                } else {
                    Toast.makeText(mActivity, getString(R.string.please_select_atleast_one), Toast.LENGTH_SHORT).show();
                }
            }
        } else if (v.getId() == R.id.buttonGallery) {
            Intent intent = new Intent(mActivity, GalleryActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(GalleryActivity.MAX_COUNT, maxNumberOfImages);
            intent.putExtra(NeonConstants.PHOTO_PARAMS, mPhotoParams);
            startActivityForResult(intent, GALLERY_PICK);

        } else if (v.getId() == R.id.buttonDone) {
//            if (enableCapturedReview ) {
//                mPictureTakenListener.onPicturesCompleted();
//                return;
//            }
            if (imagesList.size() == 0) {
                Toast.makeText(mActivity, getString(R.string.no_images), Toast.LENGTH_SHORT).show();
            } else {
                buttonCapture.setTag("done");
                onClick(buttonCapture);
            }
        } /*else if (v.getId() == R.id.auto) {
            Camera.Parameters params = mCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
            mCamera.setParameters(params);
        }*/ /*else if (v.getId() == R.id.on) {
            Camera.Parameters params = mCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
            mCamera.setParameters(params);

        } else if (v.getId() == R.id.off) {
            Camera.Parameters params = mCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            mCamera.setParameters(params);

        } else if (v.getId() == R.id.torch) {
            Camera.Parameters params = mCamera.getParameters();
            params.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            mCamera.setParameters(params);
        } */else if (v.getId() == R.id.switchCamera) {
            int cameraFacing = initCameraId();
            if (cameraFacing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                stopCamera();
                useFrontFacingCamera = true;
                startCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
            } else {
                stopCamera();
                useFrontFacingCamera = false;
                startCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
        } else if (v.getId() == R.id.currentFlashMode) {
            if(rcvFlash.getVisibility()==View.GONE)
                createFlashModesDropDown();
            else
                rcvFlash.setVisibility(View.GONE);
        }
    }

    private void createFlashModesDropDown() {
        FlashModeRecyclerHorizontalAdapter flashModeAdapter = new FlashModeRecyclerHorizontalAdapter(getActivity(), supportedFlashModes);
        rcvFlash.setAdapter(flashModeAdapter);
        rcvFlash.setVisibility(View.VISIBLE);
        flashModeAdapter.setOnItemClickListener(new FlashModeRecyclerHorizontalAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                setFlash(supportedFlashModes.get(position));
                rcvFlash.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mCamera != null) {

            Camera.Parameters params = mCamera.getParameters();
            int action = event.getAction();


            if (event.getPointerCount() > 1) {
// handle multi-touch events
                if (action == MotionEvent.ACTION_POINTER_DOWN) {
                    mDist = getFingerSpacing(event);
                } else if (action == MotionEvent.ACTION_MOVE && params.isZoomSupported()) {
                    mCamera.cancelAutoFocus();
                    handleZoom(event, params);
                }
            } else {
// handle single touch events
                if (action == MotionEvent.ACTION_UP) {
                    handleFocus(event, params);
                }
            }
            if (event.getPointerCount() > 1){
                return true;
            }

//            Camera camera = mCamera.getCamera();
//            mCamera.cancelAutoFocus();
            final Rect focusRect = calculateTapArea(event.getX(), event.getY(), 1f);

//            Camera.Parameters parameters = mCamera.getParameters();
//            if (parameters.getFocusMode() != Camera.Parameters.FOCUS_MODE_AUTO) {
//                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
//            }
//            if (parameters.getMaxNumFocusAreas() > 0) {
//                List<Camera.Area> mylist = new ArrayList<Camera.Area>();
//                mylist.add(new Camera.Area(focusRect, 1000));
//                parameters.setFocusAreas(mylist);
//            }

            try {
                mCamera.autoFocus(null);
//                mCamera.cancelAutoFocus();
//                mCamera.setParameters(parameters);
//                mCamera.startPreview();
//                mCamera.autoFocus(new Camera.AutoFocusCallback() {
//                    @Override
//                    public void onAutoFocus(boolean success, Camera camera) {
////                        if (camera.getParameters().getFocusMode() != Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE) {
////                            Camera.Parameters parameters = camera.getParameters();
////                            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
////                            if (parameters.getMaxNumFocusAreas() > 0) {
////                                parameters.setFocusAreas(null);
////                            }
////                            camera.setParameters(parameters);
////                            camera.startPreview();   //causing crash here
////                        }
//                    }
//                });

                drawingView.setHaveTouch(true, focusRect);
                drawingView.invalidate();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        drawingView.setHaveTouch(false, focusRect);
                        drawingView.invalidate();
                    }
                }, 1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return true;
    }



    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        new ImagePostProcessing(mActivity, data).execute();
    }

    private class ImagePostProcessing extends AsyncTask<Void, Void, File> {

        private Context context;
        private byte[] data;
        private ProgressDialog progressDialog;

        ImagePostProcessing(Context context, byte[] data) {
            this.context = context;
            this.data = data;
        }

        @Override
        protected File doInBackground(Void... params) {
            File pictureFile = Constants.getMediaOutputFile(getActivity(),Constants.TYPE_IMAGE, null);

            if(pictureFile == null)
                return null;

            try {
                FileOutputStream fos = new FileOutputStream(pictureFile);
                Bitmap bm;

                // COnverting ByteArray to Bitmap - >Rotate and Convert back to Data
                if (data != null) {
                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    int screenHeight = getResources().getDisplayMetrics().heightPixels;
                    bm = BitmapFactory.decodeByteArray(data, 0, (data != null) ? data.length : 0);
                    if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        // Notice that width and height are reversed
                        Bitmap scaled = Bitmap.createScaledBitmap(bm,screenWidth,screenHeight,true);
                        int w = scaled.getWidth();
                        int h = scaled.getHeight();
                        // Setting post rotate to 90
                        Matrix mtx = new Matrix();
                        int cameraId;
                        if (cameraFacing == PhotoParams.CameraFacing.FRONT) {
                            cameraId = getBackFacingCameraId();
                        } else {
                            cameraId = initCameraId();
                        }
                        int CameraEyeValue = setPhotoOrientation(getActivity(),cameraId); // CameraID = 1 : front 0:back
                        if(cameraId == Camera.CameraInfo.CAMERA_FACING_FRONT) { // As Front camera is Mirrored so Fliping the Orientation
                            if (CameraEyeValue == 270) {
                                mtx.postRotate(90);
                            } else if (CameraEyeValue == 90) {
                                mtx.postRotate(270);
                            }
                        }else{
                            mtx.postRotate(CameraEyeValue); // CameraEyeValue is default to Display Rotation
                        }
                        bm = Bitmap.createBitmap(scaled, 0, 0, w, h, mtx, true);
                    }else{// LANDSCAPE MODE
                        //No need to reverse width and height
                        int maxWidth = 1024; int maxHeight = 512;
                        int width = bm.getWidth();
                        int height = bm.getHeight();
                        float ratioBitmap = (float) width / (float) height;
                        float ratioMax = (float) maxWidth / (float) maxHeight;

                        int finalWidth = maxWidth;
                        int finalHeight = maxHeight;
                        if (ratioMax > ratioBitmap) {
                            finalWidth = (int) ((float)maxHeight * ratioBitmap);
                        } else {
                            finalHeight = (int) ((float)maxWidth / ratioBitmap);
                        }
                        bm = Bitmap.createScaledBitmap(bm, finalWidth, finalHeight, true);
                    }
                } else {
                    return null;
                }
                // COnverting the Die photo to Bitmap

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 90, stream);
                byte[] byteArray = stream.toByteArray();
                fos.write(byteArray);
                //fos.write(data);
                fos.close();
                /*Uri pictureFileUri = Uri.parse("file://" + pictureFile.getAbsolutePath());
                mActivity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                        pictureFileUri));*/
                NeonUtils.scanFile(context, pictureFile.getAbsolutePath());

            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

            mCamera.startPreview();
            return pictureFile;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(context, null, "Saving Picture", true);
        }

        @Override
        protected void onPostExecute(File file) {
            super.onPostExecute(file);
            if (progressDialog != null)
                progressDialog.dismiss();
            if (file != null) {
                if(!enableCapturedReview || mPhotoParams.getMode() == PhotoParams.MODE.NEUTRAL) {
                    updateCapturedPhotos(file);
                    return;
                }
                mPictureTakenListener.sendPictureForCropping(file);
//                capturedFilePath = file.getPath();
//                Intent intent = new Intent(mActivity, ReviewImageActivity.class);
//                intent.putExtra(Constants.IMAGE_NAME, mPhotoParams.getImageName());
//                intent.putExtra(Constants.IMAGE_PATH, file.getPath());
//                mActivity.startActivityForResult(intent, REQUEST_REVIEW);
                mCamera.startPreview();
            } else {
                Toast.makeText(context, "Camera Error. Kindly try again", Toast.LENGTH_SHORT).show();
                readyToTakePicture = true;
                buttonCapture.setEnabled(true);
                mCamera.startPreview();
            }
        }

    }

    //updates the listview with the photos clicked by the camera
    private void updateCapturedPhotos(File pictureFile) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setFilePath(pictureFile.getAbsolutePath());
        fileInfo.setFileName(pictureFile.getAbsolutePath().substring(pictureFile.getAbsolutePath().lastIndexOf("/") + 1));
        fileInfo.setSource(FileInfo.SOURCE.PHONE_CAMERA);
        imagesList.add(fileInfo);
        if (maxNumberOfImages == 1) {
            buttonCapture.setTag("done");
            onClick(buttonCapture);
        } else {
            Log.e(Constants.TAG, "updateCapturedPhotos");
            if (imagesList.size() >= 1)
                scrollView.setVisibility(View.VISIBLE);
            else
                scrollView.setVisibility(View.GONE);
            addInScrollView(fileInfo);

            if (maxNumberOfImages > 0) {
                updateView(imagesList.size() < maxNumberOfImages);
            }
            mCamera.startPreview();
            readyToTakePicture = true;
            buttonCapture.setEnabled(true);
        }
    }


    private void setCameraRotation() {
        //STEP #1: Get rotation degrees
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(Camera.CameraInfo.CAMERA_FACING_BACK, info);
        int rotation = mActivity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; //Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; //Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;//Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;//Landscape right
        }
        int rotate = (info.orientation - degrees + 360) % 360;

        //STEP #2: Set the 'rotation' parameter
        Camera.Parameters params = mCamera.getParameters();
        params.setRotation(rotate);
        mCamera.setParameters(params);
    }

    //It is called when configuration(orientation) of screen changes
    private void addInScrollView(ArrayList<FileInfo> infos) {
        if (infos != null && infos.size() > 0) {
            for (FileInfo info : infos) {
                scrollView.addView(createImageView(info));
            }
            scrollView.setVisibility(View.VISIBLE);
        }
        Log.e(Constants.TAG, "Add multiple items in scroll ");
    }

    private void addInScrollView(FileInfo info) {
        Log.e(Constants.TAG, " add in scroll View ");
        scrollView.addView(createImageView(info));
        scrollView.setVisibility(View.VISIBLE);
    }

    private View createImageView(final FileInfo info) {
        final File file = new File(info.getFilePath());
        if (!file.exists())
            return null;
        final View outerView = View.inflate(getContext(),R.layout.camera_priority_overlay,null);
        outerView.findViewById(R.id.ivRemoveImage).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollView.removeView(outerView);
                imagesList.remove(info);
                if (maxNumberOfImages > 0)
                    updateView(imagesList.size() < maxNumberOfImages);
                if (imagesList.size() < 1) {
                    buttonDone.setVisibility(View.GONE);
                    scrollView.setVisibility(View.GONE);
                }
            }
        });

        RequestOptions options = new RequestOptions()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop()
                .placeholder(R.drawable.image_load_default_small);
        Glide.with(this).load("file://" + info.getFilePath())
                .apply(options)
                .transition(withCrossFade())
                .into((ImageView) outerView.findViewById(R.id.ivCaptured));

        /*Glide.with(this).load("file://" + info.getFilePath())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade()
                .centerCrop()
                .placeholder(R.drawable.image_load_default_small)
                .into((ImageView) outerView.findViewById(R.id.ivCaptured));*/
        return outerView;
    }

    private void setOrientation(Activity activity, PhotoParams.CameraOrientation orientation) {
        if (orientation != null) {
            if (orientation.equals(PhotoParams.CameraOrientation.LANDSCAPE)) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else if (orientation.equals(PhotoParams.CameraOrientation.PORTRAIT)) {
                activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            Log.e(Constants.TAG, "No orientation set");
        }
    }

    private void updateView(boolean status) {
//        enableDoneButton(!status);
        if (!status) {
            buttonCapture.setVisibility(View.GONE);
        } else {
            buttonCapture.setVisibility(View.VISIBLE);
        }
        buttonDone.setVisibility(View.VISIBLE);
        tvImageName.setText(status ? imageName : "Press Done");
    }

    private void enableDoneButton(boolean enable) {
        buttonCapture.setImageResource(enable ? R.drawable.ic_camera_switch : R.drawable.ic_camera);
        buttonCapture.setTag(enable ? "done" : "capture");
    }

    private Rect calculateTapArea(float x, float y, float coefficient) {
        int FOCUS_AREA_SIZE = 200;
        int areaSize = Float.valueOf(FOCUS_AREA_SIZE * coefficient).intValue();

        int left = clamp((int) x - areaSize / 2, 0, mCameraPreview.getWidth() - areaSize);
        int top = clamp((int) y - areaSize / 2, 0, mCameraPreview.getHeight() - areaSize);

        RectF rectF = new RectF(left, top, left + areaSize, top + areaSize);
//        matrix.mapRect(rectF);

        return new Rect(Math.round(rectF.left), Math.round(rectF.top), Math.round(rectF.right), Math.round(rectF.bottom));
    }

    private int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    private void handleZoom(MotionEvent event, Camera.Parameters params) {
        int maxZoom = params.getMaxZoom();
        int zoom = params.getZoom();
        float newDist = getFingerSpacing(event);
        if (newDist > mDist) {
            if (zoom < maxZoom)
                zoom++;
        } else if (newDist < mDist) {
            if (zoom > 0)
                zoom--;
        }
        mDist = newDist;
        params.setZoom(zoom);
        mCamera.setParameters(params);
    }

    public void handleFocus(MotionEvent event, Camera.Parameters params) {
        Log.d(TAG, "handleFocus: " + event);
//        int pointerId = event.getPointerId(0);
//        int pointerIndex = event.findPointerIndex(pointerId);
// Get the pointer's current position
//        float x = event.getX(pointerIndex);
//        float y = event.getY(pointerIndex);

        List<String> supportedFocusModes = params.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            mCamera.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean b, Camera camera) {
// currently set to auto-focus on single touch
                }
            });
        }
    }

    /** Determine the space between the first two fingers */
    private float getFingerSpacing(MotionEvent event) {
// ...
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float)Math.sqrt(x * x + y * y);
    }

    public void stopCamera () {
        try {
            if (null == mCamera) {
                return;
            }
            mCamera.setPreviewCallback(null);
            mCameraPreview.getHolder().removeCallback(mCameraPreview);
            mCamera.stopPreview();
            mCameraLayout.removeAllViews();
            mCamera.release();
            mCamera = null;
            mCameraPreview = null;
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopCamera();
    }

    private void startCamera(int cameraFacing) {
        if (mCamera == null) {
            try {
                mCamera = Camera.open(cameraFacing);


                //To set hardware camera rotation
                setCameraRotation();

                mCameraPreview = new CameraPreview(mActivity, mCamera);
                mCameraPreview.setReadyListener(new CameraPreview.ReadyToTakePicture() {
                    @Override
                    public void readyToTakePicture(boolean ready) {
                        readyToTakePicture = ready;
                    }
                });

                mCameraPreview.setOnTouchListener(this);

                mCameraLayout = (FrameLayout) fragmentView.findViewById(R.id.camera_preview);
                mCameraLayout.addView(mCameraPreview);

                //set the screen layout to fullscreen
                mActivity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);

                buttonCapture.setOnClickListener(this);
                enableDoneButton(false);
                buttonGallery.setOnClickListener(this);

            } catch (Exception e) {
                Log.e("Camera Open Exception", "" + e.getMessage());
            }
        } else {
            enableDoneButton(false);
            Log.e(TAG, "camera not null");
        }
    }

    private int initCameraId() {
        int count = Camera.getNumberOfCameras();
        int result = -1;

        if (count > 0) {
            result = 0; // if we have a camera, default to this one

            Camera.CameraInfo info = new Camera.CameraInfo();

            for (int i = 0; i < count; i++) {
                Camera.getCameraInfo(i, info);

                if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK
                        && !useFrontFacingCamera) {
                    result = i;
                    break;
                }
                else if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT
                        && useFrontFacingCamera) {
                    result = i;
                    break;
                }
            }
        }

        return result;
    }

    public int setPhotoOrientation(Activity activity, int cameraId) {
        Camera.CameraInfo info = new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        // do something for phones running an SDK before lollipop
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360; // compensate the mirror
        } else { // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        return result;
    }

    private int getBackFacingCameraId() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                cameraId = i;
                break;
            } else if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

}
