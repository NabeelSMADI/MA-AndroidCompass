package com.htw.compass;

import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;




public class Main extends AppCompatActivity implements SensorEventListener{
    private Camera mCamera = null;
    private CameraView mCameraView = null;
    private boolean cameraOn = true;
    private boolean flashOn = false;
    // define the display assembly compass picture
    private ImageView image;

    // record the compass picture angle turned
    private float currentDegree = 0f;

    // device sensor manager
    private SensorManager mSensorManager;
    private Sensor accelerometer;
    private Sensor ORIENTATION;

    TextView tvHeading;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        // define the display assembly compass picture
        image = (ImageView) findViewById(R.id.compass);

        // TextView that will tell the user what degree is he heading
        tvHeading = (TextView) findViewById(R.id.text);


        // initialize your android device sensor capabilities
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        ORIENTATION = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);

        try{
            mCamera = Camera.open();//you can use open(int) to use different cameras
        } catch (Exception e){
            Log.d("ERROR", "Failed to get camera: " + e.getMessage());
        }
        if(mCamera != null) {
            mCameraView = new CameraView(this, mCamera);//create a SurfaceView to show camera data
            FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
            camera_view.addView(mCameraView);//add the SurfaceView to the layout
        }

        stopCamera();
        cameraOn = false;
        //btn to close the application
        final ImageView btCamera = (ImageView)findViewById(com.htw.compass.R.id.camera);
        btCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              if(cameraOn){
                  btCamera.setImageResource(R.drawable.camera_on);

                 stopCamera();
                 cameraOn = false;
              }else{
                  btCamera.setImageResource(R.drawable.camera_off);

                  startCamera();
                  cameraOn = true;
              }
            }
        });

        final ImageView btFlash = (ImageView)findViewById(com.htw.compass.R.id.flash);
        btFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(flashOn){
                    btFlash.setImageResource(R.drawable.flash_off);

                    flashOff();
                    //   mCameraView.setVisibility(View.VISIBLE);
                    flashOn = false;
                }else{
                    btFlash.setImageResource(R.drawable.flash_on);
                    flashOn();
                    //  mCameraView.setVisibility(View.INVISIBLE);
                    flashOn = true;
                }
            }
        });

    }

    private void stopCamera() {
          mCamera.stopPreview();
        FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
        camera_view.setVisibility(View.GONE);//add the SurfaceView to the layout
    }

    public void startCamera() {
        mCamera.startPreview();
        FrameLayout camera_view = (FrameLayout)findViewById(R.id.camera_view);
        camera_view.setVisibility(View.VISIBLE);//add the SurfaceView to the layout
          }


    public void flashOn() {

        Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        mCamera.setParameters(parameters);


    }


    public void flashOff() {
        Parameters parameters = mCamera.getParameters();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        mCamera.setParameters(parameters);

    }


    @Override
    protected void onResume() {
        super.onResume();

        // for the system's orientation sensor registered listeners
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI);


    }

    @Override
    protected void onPause() {
        super.onPause();

        // to stop the listener and save battery
        mSensorManager.unregisterListener(this);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ORIENTATION){
            // get the angle around the z-axis rotated
            float degree = Math.round(event.values[0]);
            String l = "";
            if (degree > 22.5f && degree < 157.5f) {
                // east
                l="E";
            } else if (degree > 202.5f && degree < 337.5f) {
                // west
                l="W";
            }
            if (degree > 112.5f && degree < 247.5f) {
                // south
                l="S";
            } else if (degree < 67.5 || degree > 292.5f) {
                // north
                l="N";
            }
            tvHeading.setText(l + " " +Float.toString(degree) +"°" );

            // create a rotation animation (reverse turn degree degrees)
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    -degree,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            // how long the animation will take place
            ra.setDuration(210);

            // set the animation after the end of the reservation status
            ra.setFillAfter(true);

            // Start the animation
            image.startAnimation(ra);
            currentDegree = -degree;
        }

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];

            if(x < 4 && y < 4){
                if(cameraOn){
                    image.setImageResource(R.drawable.compass2);
                }else{
                    image.setImageResource(R.drawable.compass4);
                }
            }
            if(x > 5 || y > 5){
                if(cameraOn){
                    image.setImageResource(R.drawable.compass);
                }else{
                    image.setImageResource(R.drawable.compass3);
                }
            }

        }


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // not in use
    }
}