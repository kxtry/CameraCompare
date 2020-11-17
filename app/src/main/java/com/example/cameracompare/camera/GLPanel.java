package com.example.cameracompare.camera;

import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.security.keystore.StrongBoxUnavailableException;
import android.util.AttributeSet;

import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@SuppressLint("NewApi")
public class GLPanel extends GLSurfaceView
{
    private GLGraphics mGLGraphics;
    private AtomicReference<ByteBuffer> mBufferImage = new AtomicReference<>(null);

    private boolean bWork = true;
    private int mFrameWidth;
    private int mFrameHeight;
    private boolean mRgb = false;

	@SuppressLint("NewApi")
    public GLPanel(Context context) {
        super(context);
        init(context);
    }

    public GLPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        setEGLContextClientVersion(2);
        setRenderer(new Scene());
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void paint(ByteBuffer buffer, int width, int height, boolean rgb32){
		if(mGLGraphics == null){
			return;
		}
        mFrameWidth = width;
        mFrameHeight = height;
        mRgb = rgb32;
        mBufferImage.set(buffer);
        if(bWork){
            requestRender();
        }
	}

    @Override
    public void onPause() {
        super.onPause();
        bWork = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        bWork = true;
    }

    @SuppressLint("NewApi")
    private class Scene implements Renderer {
        public void onDrawFrame(GL10 gl) {
            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            ByteBuffer tmp = mBufferImage.get();
            if (tmp != null) {
                tmp.position(0);
                mGLGraphics.buildTextures(tmp, mFrameWidth, mFrameHeight, mRgb);
            }
            mGLGraphics.draw();
        }

        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
        }

        public void onSurfaceCreated(GL10 gl, EGLConfig config) {
            GLES20.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
            mGLGraphics = new GLGraphics();
            if (!mGLGraphics.isProgramBuilt()) {
                mGLGraphics.buildProgram();
                mGLGraphics.setVertexData(null);
            }
            GLES20.glEnable(GLES20.GL_DEPTH_TEST);
            GLES20.glEnable(GLES20.GL_CULL_FACE);
        }
    }

}
