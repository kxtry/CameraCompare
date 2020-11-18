package com.example.cameracompare;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.util.Size;
import android.view.TextureView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cameracompare.camera.Camera1Helper;
import com.example.cameracompare.camera.Camera1Listener;
import com.example.cameracompare.camera.Camera2Helper;
import com.example.cameracompare.camera.Camera2Listener;
import com.example.cameracompare.camera.FormatConvert;
import com.example.cameracompare.camera.GLPanel;

import java.nio.ByteBuffer;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getCanonicalName();
    private GLPanel mPreview2;
    private GLPanel mReplay;
    private TextureView mPreview1;
    private Button mStart;
    private Button mStop;
    private RadioButton mChecker;
    private RadioButton mRgba;
    private RadioGroup mGroup;
    private Camera1Helper camera1;
    private Camera2Helper camera2;
    private Spinner mRotateAngle;
    private Spinner mCameraList;
    private CheckBox mFlip;
    private int cameraId = 0;
    private int rotateAngle = 0;
    private boolean flip = true;
    private Camera1Listener listener1;
    private Camera2Listener listener2;
    private FormatConvert mConvert1;
    private FormatConvert mConvert2;
    private CountDownTimer mTimer;
    private TextView mInfo;
    private long mFrameCount = 0;
    private int mPreviewWidth = 0;
    private int mPreviewHeight = 0;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mStart = (Button) findViewById(R.id.btnStart);
        mStart.setEnabled(true);
        mStop = (Button)findViewById(R.id.btnStop);
        mStop.setEnabled(false);
        mPreview1 = (TextureView)findViewById(R.id.preview1);
        mPreview2 = (GLPanel) findViewById(R.id.preview2);
        mReplay = (GLPanel)findViewById(R.id.replay);
        mChecker = (RadioButton)findViewById(R.id.camera1);
        mInfo = (TextView) findViewById(R.id.info);
        mPreview1.setVisibility(mChecker.isChecked() ? View.VISIBLE : View.GONE);
        mPreview2.setVisibility(mChecker.isChecked() ? View.GONE : View.VISIBLE);
        mRgba =(RadioButton)findViewById(R.id.rgba);
        mGroup = (RadioGroup)findViewById(R.id.camera);
        mFlip = (CheckBox)findViewById(R.id.flip);
        flip = mFlip.isChecked();
        mFlip.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                flip = b;
            }
        });
        mRotateAngle = (Spinner)findViewById(R.id.rotateAngle);
        mCameraList = (Spinner)findViewById(R.id.cameraList);
        Integer[] angle = new Integer[4];
        angle[0] = 0;
        angle[1] = 90;
        angle[2] = 180;
        angle[3] = 270;
        mRotateAngle.setAdapter(new ArrayAdapter<Integer>(this, android.R.layout.simple_list_item_1, angle));
        mRotateAngle.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> var1, View var2, int var3, long var4){
                rotateAngle = (int)mRotateAngle.getSelectedItem();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        mCameraList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, Camera2Helper.cameraList(this)));
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
                if(!Camera2Helper.checkPermission(MainActivity.this)) {
                    return;
                }
                String[] ids = Camera2Helper.cameraList(MainActivity.this.getBaseContext());
                if(ids.length == 0) {
                    Toast.makeText(MainActivity.this.getApplicationContext(), "No Camera to start.", Toast.LENGTH_LONG);
                    return;
                }
                String vid = (String) mCameraList.getSelectedItem();
                cameraId = Integer.parseInt(vid);
                if(mChecker.isChecked()) {
                    camera1 = new Camera1Helper.Builder()
                            .previewSize(new Point(640, 480))
                            .previewViewSize(new Point(640,480))
                            .rotation(rotateAngle)
                            .specificCameraId(cameraId)
                            .isMirror(false)
                            .previewOn(mPreview1)
                            .cameraListener(listener1)
                            .build();
                    camera1.init();
                    camera1.start();
                }else{
                    camera2 = new Camera2Helper.Builder()
                            .rgb32Format(mRgba.isChecked())
                            .specificCameraId(String.valueOf(cameraId))
                            .context(MainActivity.this.getBaseContext())
                            .maxPreviewSize(new Size(640, 480))
                            .previewSize(new Size(640, 480))
                            .cameraListener(listener2)
                            .build();
                    camera2.start();
                }
                mStart.setEnabled(false);
                mStop.setEnabled(true);
                mTimer.start();
            }
        });
        mStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(camera1 != null) {
                    camera1.stop();
                }
                if(camera2 != null) {
                    camera2.stop();
                }
                mStart.setEnabled(true);
                mStop.setEnabled(false);
                mTimer.cancel();
                camera1 = null;
                camera2 = null;
            }
        });

        listener1 = new Camera1Listener() {
            @Override
            public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {

            }

            @Override
            public void onPreview(byte[] data, Camera camera) {
                Camera.Size sz = camera.getParameters().getPreviewSize();
                Log.d(TAG, String.format("camera1.Id:%d - rotate:%d - flip:%d - width:%d - height:%d", cameraId, rotateAngle, flip ? 1 : 0, sz.width, sz.height));
                Bitmap bmp = mConvert1.nv21ToBitmap(data, sz.width, sz.height);
                Bitmap front = mConvert1.rotateBitmap(bmp, rotateAngle, flip);
                ByteBuffer buf = mConvert1.bitmapBuffer(front);
                mPreviewWidth = front.getWidth();
                mPreviewHeight = front.getHeight();
                mReplay.paint(buf, front.getWidth(), front.getHeight(), true);
                mFrameCount++;
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

        listener2 = new Camera2Listener() {
            @Override
            public void onCameraOpened(CameraDevice camera, String cameraId, int width, int height) {
                int rotate = rotateAngle / 90 * 90;
                mPreviewWidth = width;
                mPreviewHeight = height;
                if(rotate == 90 || rotate == 270) {
                    mPreviewWidth = height;
                    mPreviewHeight = width;
                }
            }

            @Override
            public void onPreview(byte[] data, int width, int height, boolean isRgb32, CameraDevice camera) {
                Log.d(TAG, String.format("camera2.Id:%d - rotate:%d - flip:%d - width:%d - height:%d", cameraId, rotateAngle, flip ? 1 : 0, width, height));
                Bitmap bmp = isRgb32 ? mConvert2.rgbaToBitmap(data, width, height) : mConvert2.nv21ToBitmap(data, width, height);
                Bitmap front = mConvert2.rotateBitmap(bmp, rotateAngle, flip);
                ByteBuffer buf = mConvert2.bitmapBuffer(front);
                mPreview2.paint(buf.duplicate(), front.getWidth(), front.getHeight(), true);
                mReplay.paint(buf, front.getWidth(), front.getHeight(), true);
                mFrameCount++;
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

        mTimer = new CountDownTimer(10*1000, 1000) {
            private long frameLast;
            @Override
            public void onTick(long l) {
                long qps = mFrameCount - frameLast;
                frameLast = mFrameCount;
                String msg = String.format("camera2.Id:%d - QPS:%d - rotate:%d - flip:%d - width:%d - height:%d", cameraId, qps, rotateAngle, flip ? 1 : 0, mPreviewWidth, mPreviewHeight);
                Log.d(TAG, msg);
                mInfo.setText(msg);
            }

            @Override
            public void onFinish() {
                start();
            }
        };

        mConvert1 = new FormatConvert(this);
        mConvert2 = new FormatConvert(this);
    }
}