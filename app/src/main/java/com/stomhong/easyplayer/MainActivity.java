package com.stomhong.easyplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("EasyPlayer");
    }

    private SurfaceView mGLSurfaceView;
    private SurfaceHolder surfaceHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    public  native void play();
    public  native int stop();
    public  native void createEngine();
    public native void destroy();


    private void init() {

        mGLSurfaceView = findViewById(R.id.surfaceView);

        surfaceHolder = mGLSurfaceView.getHolder();
        surfaceHolder.addCallback(this);



    }



    public native void setDataResource(Object glSurfaceView);


    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                setDataResource(surfaceHolder.getSurface());
            }
        }).start();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
