package com.uni.ppg.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.ImageFormat;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;

import com.otaliastudios.cameraview.CameraView;
import com.otaliastudios.cameraview.controls.Flash;
import com.uni.ppg.R;
import com.uni.ppg.domain.image.PpgFrameProcessor;
import com.androidnetworking.AndroidNetworking;

import java.io.File;
import java.lang.ref.WeakReference;

public class AfibDetection extends AppCompatActivity {



    private CameraView camera;
    private TextView heartRate;
    private TextView prediction;
    private static Context context;
    Button btOpen;

        @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AfibDetection.context = getApplicationContext();

        AndroidNetworking.initialize(getApplicationContext());
        setContentView(R.layout.activity_main);
        initCamera();
        initButtonMeasurement();
        initButtonGoToFolder();

    }

    @SuppressLint("ClickableViewAccessibility")
    private void initButtonGoToFolder() {
        findViewById(R.id.log).setOnClickListener((v) -> {
            ContentValues values = new ContentValues();


            values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);

            Uri extVolumeUri = MediaStore.Files.getContentUri("external");

            Log.i("TEST BUTTON GO TO FOLDER",extVolumeUri.toString());

            Uri fileUri = context.getContentResolver().insert(extVolumeUri, values);
            Intent intent = new Intent(Intent.ACTION_VIEW);

            intent.setDataAndType(Uri.parse(Environment.getExternalStorageDirectory().getPath()
                    +  File.separator + "myFolder" + File.separator), "*/*");
            startActivity(intent);

        });
    }


    public static Context getAppContext() {
        return AfibDetection.context;
    }

    private void initCamera() {
        camera = findViewById(R.id.view_camera);
        camera.setVisibility(View.INVISIBLE);
        camera.setLifecycleOwner(this);
        camera.setFrameProcessingFormat(ImageFormat.YUV_420_888);
        heartRate = findViewById(R.id.text_heart_rate);
        prediction = findViewById(R.id.text_prediction);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initButtonMeasurement() {
        Log.i("TEST","START MEASUREMENT");
        final boolean[] running = {false};
        findViewById(R.id.btn_start_measurement).setOnClickListener((v) -> {
            if (running[0]) {
                running[0] = false;
                startMeasurement();
            } else {
                running[0] = true;
                stopMeasurement();
            }
        });
    }







    /**
     * Starting heart rate measurement:
     * - flash is turned on
     * - processing consecutive frames starts
     * - label showing heart rate will later be updated according to new results
     * - start monitoring movement of the phone
     */
    private void startMeasurement() {
        camera.setFlash(Flash.TORCH);
        camera.addFrameProcessor(new PpgFrameProcessor(new WeakReference<>(heartRate),new WeakReference<>(prediction)));
//        motionMonitoring(MeasurementPhase.START);
    }


    /**
     * Stopping heart rate measurement:
     * - flash is turned off
     * - processing frames stops
     * - label showing heart rate is cleared
     * - stop monitoring movement of the phone
     */
    private void stopMeasurement() {
        camera.setFlash(Flash.OFF);
        camera.clearFrameProcessors();
//        heartRate.setText(R.string.label_empty);
//        motionMonitoring(MeasurementPhase.STOP);
    }

//    private void motionMonitoring(MeasurementPhase phase) {
//        Intent intent = new Intent(GlobalConstants.MEASUREMENT_PHASE_CHANGE);
//        intent.putExtra(GlobalConstants.MEASUREMENT_PHASE_CHANGE, phase.name());
//        sendBroadcast(intent);
//    }


    @Override
    protected void onResume() {
        super.onResume();
        camera.open();
//        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//            startForegroundService(motionMonitoringService());
//        } else {
//            startService(motionMonitoringService());
//        }
//        startForegroundService(motionMonitoringService());
    }

    @Override
    protected void onPause() {
        super.onPause();
        camera.close();
//        stopService(motionMonitoringService());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        camera.destroy();
//        stopService(motionMonitoringService());
    }

}