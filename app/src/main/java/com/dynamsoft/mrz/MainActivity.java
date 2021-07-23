package com.dynamsoft.mrz;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.dynamsoft.dce.CameraDLSLicenseVerificationListener;
import com.dynamsoft.dce.CameraEnhancer;
import com.dynamsoft.dce.CameraState;
import com.dynamsoft.dce.CameraView;
import com.dynamsoft.dce.Frame;
import com.dynamsoft.dlr.DLRImageData;
import com.dynamsoft.dlr.DLRLTSLicenseVerificationListener;
import com.dynamsoft.dlr.DLRLineResult;
import com.dynamsoft.dlr.DLRResult;
import com.dynamsoft.dlr.DMLTSConnectionParameters;
import com.dynamsoft.dlr.EnumDLRErrorCode;
import com.dynamsoft.dlr.EnumDLRImagePixelFormat;
import com.dynamsoft.dlr.LabelRecognition;
import com.dynamsoft.dlr.LabelRecognitionException;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.BufferedReader;
import java.io.File;
import android.graphics.Bitmap;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.*;
import android.graphics.*;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;

public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "image";
    private CameraEnhancer mCameraEnhancer;
    private CameraView cameraView;
    private FloatingActionButton mButton;
    private ProgressBar mProgressBar;
    private static LabelRecognition mRecognition;

    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private  ,mExecutor mExecutor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Dynamsoft Camera Enhancer
        initDCE();

        // Initialize Dynamsoft Label Recognizer
        initDLR();

        mButton = findViewById(R.id.fab);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mProgressBar.setVisibility(View.VISIBLE);
                final Frame frame = mCameraEnhancer.AcquireListFrame(true);

                mExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        // Save file to temp dir
                        try {
                            File outputDir = MainActivity.this.getCacheDir();
                            File outputFile = File.createTempFile("tmp", ".png", outputDir);

                            Utils.saveFrame(frame, outputFile.toString());

                            DLRImageData data = new DLRImageData();
                            data.bytes = frame.data;
                            data.format = EnumDLRImagePixelFormat.DLR_IPF_GRAYSCALED;
                            data.stride = frame.strides[0];
                            data.width = frame.width;
                            data.height = frame.height;
                            DLRResult[] results = null;
                            try {
                                results = mRecognition.recognizeByBuffer(data, "locr");
                            }
                            catch (Exception e) {
                                e.printStackTrace();
                            }

                            if (results != null) {
                                for (DLRResult result : results) {
                                    DLRLineResult[] lines = result.lineResults;
                                    for (DLRLineResult line : lines) {
                                        Log.i("DLR", line.text);
                                    }
                                }
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.GONE);
                                    Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                                    intent.putExtra(EXTRA_MESSAGE, outputFile.toString());
                                    startActivity(intent);
                                }
                            });

                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        });

        mProgressBar = findViewById(R.id.indeterminateBar);

        mHandler = new Handler(Looper.getMainLooper());
        mExecutor = Executors.newSingleThreadExecutor();
    }

    private void initDCE() {
        cameraView = findViewById(R.id.cameraView);
        mCameraEnhancer = new CameraEnhancer(MainActivity.this);
        mCameraEnhancer.addCameraView(cameraView);
        com.dynamsoft.dce.DMDLSConnectionParameters info = new com.dynamsoft.dce.DMDLSConnectionParameters();
        info.organizationID = "200001"; // Get the organization ID from https://www.dynamsoft.com/customer/license/trialLicense?product=dce
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
    }

    private void initDLR() {
        try {
            mRecognition = new LabelRecognition();
            DMLTSConnectionParameters parameters = new DMLTSConnectionParameters();
            // The organization id 200001 here will grant you a public trial license good for 7 days.
            // After that, please visit: https://www.dynamsoft.com/customer/license/trialLicense?product=dlr
            // to request for 30 days extension.
            parameters.organizationID = "200001";
            mRecognition.initLicenseFromLTS(parameters, new DLRLTSLicenseVerificationListener() {
                @Override
                public void LTSLicenseVerificationCallback(boolean b, final Exception e) {
                    if (!b) {
                        e.printStackTrace();
                    }
                }
            });
            loadModel();
        } catch (Exception e) {
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCameraEnhancer.pauseCamera();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraEnhancer.resumeCamera();
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mCameraEnhancer.startScanning();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCameraEnhancer.stopScanning();
    }

    private void loadModel() {
        try {
            String[] fileNames = {"NumberUppercase","NumberUppercase_Assist_1lIJ","NumberUppercase_Assist_8B","NumberUppercase_Assist_8BHR","NumberUppercase_Assist_number","NumberUppercase_Assist_O0DQ","NumberUppercase_Assist_upcase"};
            for(int i = 0;i<fileNames.length;i++) {
                AssetManager manager = getAssets();
                InputStream isPrototxt = manager.open("CharacterModel/"+fileNames[i]+".prototxt");
                byte[] prototxt = new byte[isPrototxt.available()];
                isPrototxt.read(prototxt);
                isPrototxt.close();
                InputStream isCharacterModel = manager.open("CharacterModel/"+fileNames[i]+".caffemodel");
                byte[] characterModel = new byte[isCharacterModel.available()];
                isCharacterModel.read(characterModel);
                isCharacterModel.close();
                InputStream isTxt = manager.open("CharacterModel/"+fileNames[i]+".txt");
                byte[] txt = new byte[isTxt.available()];
                isTxt.read(txt);
                isTxt.close();
                mRecognition.appendCharacterModelBuffer(fileNames[i], prototxt, txt, characterModel);
            }

            StringBuilder stringBuilder = new StringBuilder();
            try {
                AssetManager manager = getAssets();
                BufferedReader bf = new BufferedReader(new InputStreamReader(
                        manager.open("wholeImgMRZTemplate.json")));
                String line;
                while ((line = bf.readLine()) != null) {
                    stringBuilder.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            mRecognition.appendSettingsFromString(stringBuilder.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}