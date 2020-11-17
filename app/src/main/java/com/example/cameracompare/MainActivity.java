package com.example.cameracompare;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.example.cameracompare.camera.Camera1Helper;
import com.example.cameracompare.camera.Camera1Listener;
import com.example.cameracompare.camera.Camera2Helper;
import com.example.cameracompare.camera.Camera2Listener;
import com.example.cameracompare.camera.GLPanel;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();
    private GLPanel mPreview2;
    private GLPanel mReplay;
    private TextureView mPreview1;
    private Button mStart;
    private Button mStop;
    private RadioButton mChecker;
    private RadioGroup mGroup;
    private Camera1Helper camera1;
    private Camera2Helper camera2;
    private int cameraId = 0;
    private int rotateAngle = 0;
    private boolean flip = false;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStart = (Button) findViewById(R.id.btnStart);
        mStop = (Button)findViewById(R.id.btnStop);
        mPreview1 = (TextureView)findViewById(R.id.preview1);
        mPreview2 = (GLPanel) findViewById(R.id.preview2);
        mReplay = (GLPanel)findViewById(R.id.replay);
        mChecker = (RadioButton)findViewById(R.id.camera1);
        mPreview1.setVisibility(mChecker.isChecked() ? View.VISIBLE : View.GONE);
        mPreview2.setVisibility(mChecker.isChecked() ? View.GONE : View.VISIBLE);
        mGroup = (RadioGroup)findViewById(R.id.camera);
        mGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int id) {
                mPreview1.setVisibility(mChecker.isChecked() ? View.VISIBLE : View.GONE);
                mPreview2.setVisibility(mChecker.isChecked() ? View.GONE : View.VISIBLE);
            }
        });
        mStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mChecker.isChecked()) {
                    camera1.init();
                    camera1.start();
                }else{
                    camera2.start();
                }
            }
        });
        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mChecker.isChecked()) {
                    camera1.stop();
                }else{
                    camera2.stop();
                }
            }
        });

        Camera1Listener listener1 = new Camera1Listener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {

            }

            @Override
            public void onPreview(byte[] data, Camera camera) {
                Camera.Size sz = camera.getParameters().getPreviewSize();
                Log.d(TAG, String.format("width:%d - height:%d", sz.width, sz.height));
            }

            @Override
            public void onCameraClosed() {

            }

            @Override
            public void onCameraError(Exception e) {

            }

            @Override
            public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {

            }
        };



        camera1 = new Camera1Helper.Builder()
                .previewViewSize(new Point(640,480))
                .rotation(rotateAngle)
                .specificCameraId(cameraId)
                .isMirror(false)
                .previewOn(mPreview1)
                .cameraListener(listener1)
                .build();

        Camera2Listener listener2 = new Camera2Listener() {
            @Override
            public void onCameraOpened(CameraDevice camera, String cameraId, int width, int height) {

            }

            @Override
            public void onPreview(byte[] nv21, int width, int height, CameraDevice camera) {
                Log.d(TAG, String.format("width:%d - height:%d", width, height));
            }

            @Override
            public void onCameraClosed() {
                Log.i(TAG, "onCameraClosed: ");
            }

            @Override
            public void onCameraError(Exception e) {
                Log.i(TAG, "onCameraError: " + e.getMessage());
            }
        };

        camera2 = new Camera2Helper.Builder()
                .specificCameraId(String.valueOf(cameraId))
                .context(this.getBaseContext())
                .maxPreviewSize(new Size(800, 600))
                .previewSize(new Size(640, 480))
                .cameraListener(listener2)
                .build();
    }
}