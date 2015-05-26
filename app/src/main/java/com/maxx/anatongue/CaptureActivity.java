package com.maxx.anatongue;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.hardware.Camera;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by maxx on 5/21/15.
 */
public class CaptureActivity extends ActionBarActivity implements SurfaceHolder.Callback, View.OnClickListener {

    private static final String TAG = "Capture";

    private int cameraId = 0;

    Camera camera;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    boolean previewing = false;

    Button snapButton;
    Button changeCameraButton;

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

            File storagePath = new File(Environment.getExternalStorageDirectory() + String.format(
                    "/AnaTongue/%d.png", System.currentTimeMillis()));

            try {

                FileOutputStream outputStream = new FileOutputStream(storagePath);
                cameraBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

                outputStream.flush();
                outputStream.close();
                Log.d(TAG, "onPictureTaken - wrote bytes: " + data.length);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + storagePath.getAbsolutePath()), "image/*");
            startActivity(intent);
        }
    };
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture);

        surfaceView = (SurfaceView) findViewById(R.id.camerapreview);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        snapButton = (Button) findViewById(R.id.snap);
        changeCameraButton = (Button) findViewById(R.id.changeCamera);

        snapButton.setOnClickListener(this);
        changeCameraButton.setOnClickListener(this);

        mkDirectory();
    }

    private void mkDirectory() {
        File imageDirectory = new File(Environment.getExternalStorageDirectory() + "/AnaTongue/");
        // Check if the directory exists
        if (!imageDirectory.exists()) {
            // have the object build the directory structure, if needed.
            imageDirectory.mkdirs();
        }
    }


    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
// TODO Auto-generated method stub
        if (previewing) {
            camera.stopPreview();
            previewing = false;
        }

        if (camera != null) {
            try {
                camera.setPreviewDisplay(surfaceHolder);
                camera.startPreview();
                previewing = true;
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
// TODO Auto-generated method stub
        /*int numberOfCameras = Camera.getNumberOfCameras();
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT)
                camera = Camera.open(i);
        }*/

        cameraId = findFrontFacingCamera();
        if (cameraId < 0) {
            camera = Camera.open();
            Toast.makeText(this, "No front facing camera found.",
                    Toast.LENGTH_LONG).show();
        } else {
            camera = Camera.open(cameraId);
        }
    }

    private int findFrontFacingCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                Log.d(TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
// TODO Auto-generated method stub
        camera.stopPreview();
        camera.release();
        camera = null;
        previewing = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.snap:
                camera.takePicture(shutterCallback, rawCallback, jpegCallback);
                break;
            case R.id.changeCamera:

                break;
            default:
                break;
        }
    }
}
