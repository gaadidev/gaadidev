package com.gaadi.neon.util;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.scanlibrary.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author lakshay girdhar
 * @version 1.0
 * @since 18/02/15
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview";
    private static final int DEGREES_0 = 0;
    private static final int DEGREES_90 = 90;
    private static final int DEGREES_180 = 180;
    private static final int DEGREES_270 = 270;
    private Camera mCamera;
    Camera.AutoFocusCallback myAutoFocusCallback = new Camera.AutoFocusCallback() {

        @Override
        public void onAutoFocus(boolean arg0, Camera arg1) {
            if (arg0) {
                mCamera.cancelAutoFocus();
            }
        }
    };
    private int cameraOrientation;
    private List<Camera.Size> mSupportedPreviewSizes;
    private List<Camera.Size> mSupportedPictureSizes;
    private Camera.Size mPreviewSize;
    private Camera.Size mPictureSize;
    private SurfaceHolder holder;
    private Display display;
    private Activity mActivity;
    private ReadyToTakePicture readyListener = null;

    public CameraPreview(Context context) {
        super(context);
    }

    public CameraPreview(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CameraPreview(Activity context, Camera camera) {
        super(context);
        mActivity = context;
        Log.e(TAG, "constructor camera preview");
        mCamera = camera;
        holder = getHolder();
        holder.addCallback(this);
        Camera.Parameters parameters = mCamera.getParameters();
        setFocusable(true);
        setFocusableInTouchMode(true);
        mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
        mSupportedPictureSizes = parameters.getSupportedPictureSizes();
        display = ((WindowManager) context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
        configureCamera(getResources().getConfiguration());
    }

    private void configureCamera(Configuration configuration) {
        try {

            if (mCamera != null) {
                int width = getScreenWidth();
                int height = getScreenHeight();

                int displayOrientationDegrees = getDisplayOrientationDegrees(display);
                mCamera.setDisplayOrientation(displayOrientationDegrees);

                Camera.Size previewSize = getOptimalPreviewSizeByAspect(mSupportedPreviewSizes, width, height);
                float aspect = (float) previewSize.width / previewSize.height;

                Log.e(Constants.TAG, "Aspect : " + aspect);

                FrameLayout.LayoutParams cameraHolderParams = (FrameLayout.LayoutParams) getLayoutParams();
                cameraOrientation = configuration.orientation;

                if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    if (cameraHolderParams == null) {
                        cameraHolderParams = new FrameLayout.LayoutParams(width, (int) (width * aspect));
                    } else {
                        cameraHolderParams.height = (int) (width * aspect);
                        cameraHolderParams.width = width;
                    }
                    Log.e(Constants.TAG, " Camera width : " + cameraHolderParams.width + " Camera Height : " + cameraHolderParams.height);

                } else {
                    if (cameraHolderParams == null) {
                        cameraHolderParams = new FrameLayout.LayoutParams(width, (int) (width / aspect));
                    } else {
                        cameraHolderParams.width = width;
                        cameraHolderParams.height = (int) (width / aspect);
                    }
                    Log.e(Constants.TAG, " Camera width : " + cameraHolderParams.width + " Camera Height : " + cameraHolderParams.height);
                }

                cameraHolderParams.gravity = Gravity.CENTER;
                setLayoutParams(cameraHolderParams);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private int getScreenWidth() {
        Point size = new Point();
        display.getSize(size);
        return size.x;
    }

    private int getDisplayOrientationDegrees(Display display) {
        int displayOrientationDegrees;
        int orientation = getResources().getConfiguration().orientation;

        switch (display.getRotation()) {
            case Surface.ROTATION_0:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    displayOrientationDegrees = DEGREES_90;
                else displayOrientationDegrees = DEGREES_0;
                break;
            case Surface.ROTATION_90:
                if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    displayOrientationDegrees = DEGREES_0;
                else displayOrientationDegrees = DEGREES_270;
                break;
            case Surface.ROTATION_180:
                if (orientation == Configuration.ORIENTATION_PORTRAIT)
                    displayOrientationDegrees = DEGREES_270;
                else displayOrientationDegrees = DEGREES_180;
                break;
            case Surface.ROTATION_270:
                if (orientation == Configuration.ORIENTATION_LANDSCAPE)
                    displayOrientationDegrees = DEGREES_180;
                else displayOrientationDegrees = DEGREES_90;
                break;
            default:
                displayOrientationDegrees = DEGREES_0;
        }

        return displayOrientationDegrees;
    }

    private int getScreenHeight() {
        Point size = new Point();
        display.getSize(size);
        return size.y;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {
            Log.e(TAG, "surface created");
            //set camera to continually auto-focus
            Camera.Parameters params = mCamera.getParameters();
            mCamera.stopPreview();
            Camera.Parameters p = mCamera.getParameters();
            p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

            mCamera.setParameters(params);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();

            mCamera.autoFocus(null);

        } catch (Exception e) {
            Log.e("Exception ", "" + e.getMessage());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreview(mSupportedPreviewSizes, width, height);
        }
        if (mSupportedPictureSizes != null) {
            mPictureSize = getOptimalPreview(mSupportedPictureSizes, width, height);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.e(TAG, "Surface changed");
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
            Log.e(TAG, e.getMessage());
        }


        setParametersToCamera();
        //mCamera.setParameters(p);
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
            mCamera.autoFocus(null);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
//        try {
//            Camera.Parameters parameters = mCamera.getParameters();
//            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
//            holder.addCallback(this);
//            mCamera.setPreviewDisplay(holder);
//            mCamera.setParameters(parameters);
//            mCamera.startPreview();
//            ScanActivity.readyToTakePicture = true;
        if (readyListener != null)
            readyListener.readyToTakePicture(true);
//
//        } catch (Exception e) {
//            Log.e(TAG, "" + e.getMessage());
//        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
//        holder.removeCallback(this);
//        mCamera.stopPreview();
        if (readyListener != null)
            readyListener.readyToTakePicture(false);
//        mCamera.release();
        Log.e("Camera Preview", "Surface Destroyed");
    }

//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//
//        if(event.getAction() == MotionEvent.ACTION_DOWN){
//            float x = event.getX();
//            float y = event.getY();
//
//            Rect touchRect = new Rect(
//                    (int)(x - 100),
//                    (int)(y - 100),
//                    (int)(x + 100),
//                    (int)(y + 100));
//
//
//            final Rect targetFocusRect = new Rect(
//                    touchRect.left * 2000/this.getWidth() - 1000,
//                    touchRect.top * 2000/this.getHeight() - 1000,
//                    touchRect.right * 2000/this.getWidth() - 1000,
//                    touchRect.bottom * 2000/this.getHeight() - 1000);
//
//            doTouchFocus(targetFocusRect);
////            if (drawingViewSet) {
////                drawingView.setHaveTouch(true, touchRect);
////                drawingView.invalidate();
////
////                // Remove the square indicator after 1000 msec
////                Handler handler = new Handler();
////                handler.postDelayed(new Runnable() {
////
////                    @Override
////                    public void run() {
////                        drawingView.setHaveTouch(false, new Rect(0,0,0,0));
////                        drawingView.invalidate();
////                    }
////                }, 1000);
////            }
//        }
//        return true;
//    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    public void doTouchFocus(final Rect tfocusRect) {
        try {
            List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
            focusList.add(focusArea);

            Camera.Parameters param = mCamera.getParameters();
            param.setFocusAreas(focusList);
            param.setMeteringAreas(focusList);
            mCamera.setParameters(param);

            mCamera.autoFocus(myAutoFocusCallback);
        } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Unable to auto focus");
        }
    }

    public void setReadyListener(ReadyToTakePicture listener) {
        this.readyListener = listener;
    }

    public void setParametersToCamera() {
        if (mCamera == null) {
            return;
        }

        Camera.Parameters parameters = mCamera.getParameters();
        WindowManager wm = (WindowManager) mActivity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        mSupportedPictureSizes = mCamera.getParameters().getSupportedPictureSizes();
        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSizeByAspect(mSupportedPreviewSizes, width, height);
            mPictureSize = getOptimalPreviewSizeByAspect(mSupportedPictureSizes, width, height);
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            parameters.setPictureSize(mPictureSize.width, mPictureSize.height);
        }

        if (parameters.getMaxNumFocusAreas() > 0) {
            parameters.setFocusAreas(null);
        }
        List<String> supportedFocusModes = parameters.getSupportedFocusModes();
        if (supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        mCamera.setParameters(parameters);
    }

    private Camera.Size getOptimalPreview(List<Camera.Size> sizes, int w, int h) {
        Camera.Size bestSize = null;
        long used = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
        long availableMemory = Runtime.getRuntime().maxMemory() - used;
        for (Camera.Size currentSize : sizes) {
            int newArea = currentSize.width * currentSize.height;
            long neededMemory = newArea * 4 * 4; // newArea * 4 Bytes/pixel * 4 needed copies of the bitmap (for safety :) )
            boolean isDesiredRatio = (currentSize.width / 4) == (currentSize.height / 3);
            boolean isBetterSize = (bestSize == null || currentSize.width > bestSize.width);
            boolean isSafe = neededMemory < availableMemory;
            if (isDesiredRatio && isBetterSize && isSafe) {
                bestSize = currentSize;
            }
        }
        if (bestSize == null) {
            return sizes.get(0);
        }
        return bestSize;
    }

    private Camera.Size getOptimalPreviewSizeByAspect(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1f;
        double targetRatio;
        int targetHeight;

        if (sizes == null) return null;

        if (w > h) {
            targetHeight = h;
            targetRatio = (double) w / h;
        } else {
            targetHeight = w;
            targetRatio = (double) h / w;
        }

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;


        // Try to find an size match aspect ratio and size
        for (Camera.Size size : sizes) {
            //Log.e(TAG,size.height+" "+size.width);
            double ratio = (double) size.width / size.height;

            Log.e(TAG, size.height + " X " + size.width + " ( " + (ratio) + " )");

            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find the one match the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        // Log.e(TAG,"OPTIMAL "+optimalSize.height+" "+optimalSize.width);
        return optimalSize;
    }

    public interface ReadyToTakePicture {
        void readyToTakePicture(boolean ready);
    }
}
