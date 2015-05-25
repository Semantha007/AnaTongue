package com.maxx.anatounge;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by maxx on 5/21/15.
 */
public class CaptureActivity extends ActionBarActivity{

    private Preview mPreview;
    private static final String TAG = "TongueTest";
    Camera mCamera;
    RelativeLayout preview;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_capture);

        File imageDirectory = new File(Environment.getExternalStorageDirectory() + "/TongueTest/");
        // Check if the directory exists
        if (!imageDirectory.exists()) {
            // have the object build the directory structure, if needed.
            imageDirectory.mkdirs();
        }

        //setUpViewPager();
    }



    @Override
    protected void onResume() {
        super.onResume();

        mPreview = new Preview(this);
        preview = (RelativeLayout) findViewById(R.id.preview);
        preview.addView(mPreview);
    }

    @Override
    protected void onPause() {
        super.onPause();
        /*if (null == mCamera) {
            return;
        }*/
        Log.i("Pause", "onpause");
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mPreview.getHolder().removeCallback(mPreview);

            mCamera.release();
            mCamera = null;
        }
    }

    public void snap(View view) {
        mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
    }

    Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
        public void onShutter() {
            Log.d(TAG, "onShutter'd");
        }
    };

    Camera.PictureCallback rawCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] _data, Camera _camera) {
            Log.d(TAG, "onPictureTaken - raw");
        }
    };

    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera _camera) {
            Bitmap cameraBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Bitmap cameraScaledBitmap = Bitmap.createScaledBitmap(cameraBitmap, 1280, 720, true);
            int wid = cameraScaledBitmap.getWidth();
            int hgt = cameraScaledBitmap.getHeight();
            Bitmap newImage = Bitmap.createBitmap(wid, hgt, Bitmap.Config.ARGB_8888);

            FrameLayout view = (FrameLayout) findViewById(R.id.image);
            view.setDrawingCacheEnabled(true);
            Bitmap overlayBitmap = view.getDrawingCache();
            Bitmap overlayScaledBitmap = Bitmap.createScaledBitmap(overlayBitmap, wid, hgt, true);

            // Bitmap overlayBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.hat);
            // Bitmap overlayScaledBitmap = Bitmap.createScaledBitmap(overlayBitmap, wid, hgt, true);

            Canvas canvas = new Canvas(newImage);
            canvas.drawBitmap(cameraScaledBitmap, 0, 0, null);
            canvas.drawBitmap(overlayScaledBitmap, 0, 0, null);

            File storagePath = new File(Environment.getExternalStorageDirectory() + String.format(
                    "/TongueTest/%d.png", System.currentTimeMillis()));

            try {
                    /*FrameLayout view = (FrameLayout) findViewById(R.id.image);
                view.setDrawingCacheEnabled(true);
                Bitmap bitmap = view.getDrawingCache();*/
                FileOutputStream outputStream = new FileOutputStream(storagePath);
                newImage.compress(Bitmap.CompressFormat.PNG, 95, outputStream);

                outputStream.flush();
                outputStream.close();
                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            /*camera.startPreview();
            newImage.recycle();
            newImage = null;*/

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + storagePath.getAbsolutePath()), "image/*");
            startActivity(intent);
        }
    };

    class Preview extends SurfaceView implements SurfaceHolder.Callback {

        SurfaceHolder mHolder;
        protected Activity mActivity;

        public Preview(Context context) {
            super(context);
            // Install a SurfaceHolder.Callback so we get notified when the
            // underlying surface is created and destroyed.
            mHolder = getHolder();
            mHolder.addCallback(this);
            mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
                int numberOfCameras = Camera.getNumberOfCameras();
                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                        mCamera = Camera.open(i);
                }
            }
            /*if (mCamera == null)
                mCamera = Camera.open();*/

            try {
                mCamera.setPreviewDisplay(holder);
                mCamera.setPreviewCallback(new Camera.PreviewCallback() {

                    public void onPreviewFrame(byte[] data, Camera arg1) {
                        Log.d(TAG, "onPreviewFrame - wrote bytes: "
                                + data.length);

                        Preview.this.invalidate();
                    }
                });
            } catch (IOException e) {
                Log.i("Exce", e.toString());
                mCamera.release();
                mCamera = null;
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
            Camera.Parameters parameters = mCamera.getParameters();
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            Camera.Size optimalSize = getOptimalPreviewSize(sizes, w, h);

            Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
            if (display.getRotation() == Surface.ROTATION_0) {
                parameters.setPreviewSize(optimalSize.height, optimalSize.width);
                mCamera.setDisplayOrientation(90);
                Log.i("0", "90");
            }
            if (display.getRotation() == Surface.ROTATION_90) {
                parameters.setPreviewSize(optimalSize.width, optimalSize.height);
                mCamera.setDisplayOrientation(0);
                Log.i("90", "0");
            }
            if (display.getRotation() == Surface.ROTATION_180) {
                parameters.setPreviewSize(optimalSize.width, optimalSize.height);
                mCamera.setDisplayOrientation(270);
                Log.i("180", "270");
            }
            if (display.getRotation() == Surface.ROTATION_270) {
                parameters.setPreviewSize(optimalSize.width, optimalSize.height);
                mCamera.setDisplayOrientation(180);
                Log.i("270", "180");
            }
            mCamera.setParameters(parameters);
            mCamera.startPreview();
        }

        private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
            final double ASPECT_TOLERANCE = 0.05;
            double targetRatio = (double) w / h;
            if (sizes == null) return null;
            Camera.Size optimalSize = null;
            double minDiff = Double.MAX_VALUE;
            int targetHeight = h;
            // Try to find an size match aspect ratio and size
            for (Camera.Size size : sizes) {
                double ratio = (double) size.width / size.height;
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
            return optimalSize;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mCamera.stopPreview();
            mCamera.release();
            mCamera = null;
        }
    }
}
