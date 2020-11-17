package com.example.cameracompare.camera;

import android.hardware.Camera;
import android.hardware.camera2.CameraDevice;
import android.util.Size;


public interface Camera2Listener {
    /**
     * 当打开时执行
     * @param camera 相机实例
     * @param cameraId 相机ID
     */
    void onCameraOpened(CameraDevice camera, String cameraId, int width, int height);

    /**
     * 预览数据回调
     * @param nv21 预览数据
     * @param camera 相机实例
     */
    void onPreview(byte[] nv21, int width, int height, CameraDevice camera);

    /**
     * 当相机关闭时执行
     */
    void onCameraClosed();

    /**
     * 当出现异常时执行
     * @param e 相机相关异常
     */
    void onCameraError(Exception e);
}
