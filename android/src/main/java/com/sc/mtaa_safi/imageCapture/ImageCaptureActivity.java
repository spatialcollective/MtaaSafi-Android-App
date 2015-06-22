package com.sc.mtaa_safi.imageCapture;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.sc.mtaa_safi.R;

public class ImageCaptureActivity extends Activity implements SensorEventListener {

    private Camera mCamera;
    private CameraPreview mPreview;

    private int orientation;
    private ExifInterface exif;
    private SensorManager sensorManager = null;

    private ImageButton discardButton;
    private ImageButton acceptButton;
    private ImageButton shootButton;
    private FrameLayout shootButtonContainer, confirmButtonsContainer;
    private File sdRoot;
    private String dir;
    private String fileName;

    public final String IMAGE_FILE_NAME = "IMAGE_FILE_NAME";
    public static final String TAG = "ImageCaptureActivity";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_capture);

        sdRoot = Environment.getExternalStorageDirectory();
        dir = "/DCIM/Camera/";


        discardButton = (ImageButton) findViewById(R.id.discard_button);
        acceptButton = (ImageButton) findViewById(R.id.accept_button);
        shootButton = (ImageButton) findViewById(R.id.shoot_button);
        shootButtonContainer = (FrameLayout) findViewById(R.id.shoot_button_container);
        confirmButtonsContainer = (FrameLayout) findViewById(R.id.confirm_buttons_container);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        shootButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                mCamera.takePicture(null, null, mPicture);
            }
        });

        discardButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                File discardedPhoto = new File(sdRoot, dir + fileName);
                discardedPhoto.delete();
                mCamera.startPreview();
                shootButtonContainer.setVisibility(LinearLayout.VISIBLE);
                confirmButtonsContainer.setVisibility(View.GONE);
            }
        });

        acceptButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(IMAGE_FILE_NAME, sdRoot.getPath() + dir + fileName);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }

    private void createCamera() {
        mCamera = getCameraInstance();
        setOptimalParameters();
        mPreview = new CameraPreview(this, mCamera);
        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);

        preview.addView(mPreview, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!checkCameraHardware(this)) {
            Toast.makeText(this, "No camera!", Toast.LENGTH_LONG ).show();
        } else if (!checkSDCard()) {
            Toast.makeText(this, "No SD card!", Toast.LENGTH_LONG ).show();
            finish();
        }
        createCamera();
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseCamera();

        FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
        preview.removeViewAt(0);
    }

    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    private boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    private boolean checkSDCard() {
        boolean state = false;
        String sd = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(sd)) {
            state = true;
        }

        return state;
    }

    public static Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK);
        } catch (Exception e) {
            Log.e(TAG, "Camera could not open: "+e.getMessage());
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        }
        return c;
    }

    private PictureCallback mPicture = new PictureCallback() {

        public void onPictureTaken(byte[] data, Camera camera) {
            mCamera.stopPreview();
            shootButtonContainer.setVisibility(View.GONE);
            confirmButtonsContainer.setVisibility(View.VISIBLE);

            fileName = "IMG_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()).toString() + ".jpg";

            File mkDir = new File(sdRoot, dir);
            mkDir.mkdirs();

            File pictureFile = new File(sdRoot, dir + fileName);

            try {
                FileOutputStream purge = new FileOutputStream(pictureFile);
                purge.write(data);
                purge.close();
            } catch (FileNotFoundException e) {
                Log.d(TAG, "File not found: " + e.getMessage());
            } catch (IOException e) {
                Log.d(TAG, "Error accessing file: " + e.getMessage());
            }

        }
    };



    public void onSensorChanged(SensorEvent event) {
        synchronized (this) {
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                if (event.values[0] < 4 && event.values[0] > -4) {
                    if (event.values[1] > 0 && orientation != ExifInterface.ORIENTATION_ROTATE_90) {
                        orientation = ExifInterface.ORIENTATION_ROTATE_90;
                        changeDisplayOrientation(90);
                    } else if (event.values[1] < 0 && orientation != ExifInterface.ORIENTATION_ROTATE_270) {
                        orientation = ExifInterface.ORIENTATION_ROTATE_270;
                        changeDisplayOrientation(270);
                    }
                } else if (event.values[1] < 4 && event.values[1] > -4) {
                    if (event.values[0] > 0 && orientation != ExifInterface.ORIENTATION_NORMAL) {
                        orientation = ExifInterface.ORIENTATION_NORMAL;
                        changeDisplayOrientation(0);
                    } else if (event.values[0] < 0 && orientation != ExifInterface.ORIENTATION_ROTATE_180) {
                        orientation = ExifInterface.ORIENTATION_ROTATE_180;
                        changeDisplayOrientation(180);
                    }
                }
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    private void setOptimalParameters(){
        Camera.Parameters params = mCamera.getParameters();

        List<Camera.Size> list = params.getSupportedPictureSizes();
        Camera.Size cs;
        Camera.Size bestcs = null;
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++){
                cs = list.get(i);
                if (cs.height <= 1400) {
                    if( bestcs != null ){
                        if(bestcs.height > cs.height){
                            continue;
                        }
                    }
                    bestcs = cs;
                }
            }
        }

        if (bestcs != null)
            params.setPictureSize(bestcs.width, bestcs.height);
        mCamera.setDisplayOrientation(90);
        mCamera.setParameters(params);
    }

    private void changeDisplayOrientation(int degrees){
        if (mCamera != null) {
            mCamera.stopPreview();
            Camera.Parameters params = mCamera.getParameters();
            params.setRotation(degrees);
            mCamera.setParameters(params);
            mCamera.startPreview();
        }
    }
}