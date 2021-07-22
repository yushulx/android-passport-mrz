package com.dynamsoft.mrz;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.dynamsoft.dce.CameraDLSLicenseVerificationListener;
import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraState;
import com.dynamsoft.dce.CameraView;

public class MainActivity extends AppCompatActivity {
    CameraEnhancer mCameraEnhancer;
    CameraView cameraView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cameraView = findViewById(R.id.cameraView);
        mCameraEnhancer = new CameraEnhancer(MainActivity.this);
        mCameraEnhancer.addCameraView(cameraView);
        //Initialize your license
        com.dynamsoft.dce.DMDLSConnectionParameters info = new com.dynamsoft.dce.DMDLSConnectionParameters();
        info.organizationID = "200001";
        mCameraEnhancer.initLicenseFromDLS(info,new CameraDLSLicenseVerificationListener() {
            @Override
            public void DLSLicenseVerificationCallback(boolean isSuccess, Exception error) {
                if(!isSuccess){
                    error.printStackTrace();
                }
            }
        });
        //Turn on the camera
        mCameraEnhancer.setCameraDesiredState(CameraState.CAMERA_STATE_ON);
        //Start scanning
        mCameraEnhancer.startScanning();
    }
}