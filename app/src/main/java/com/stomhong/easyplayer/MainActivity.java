package com.stomhong.easyplayer;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("EasyPlayer");
    }

    private SurfaceView mGLSurfaceView;
    private SurfaceHolder surfaceHolder;
    private AudioTrack trackplayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();

    }

    private void init() {
//        createEngine();
        //根据采样率，采样精度，单双声道来得到frame的大小。
        int bufsize = AudioTrack.getMinBufferSize(80000,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT);

//注意，按照数字音频的知识，这个算出来的是一秒钟buffer的大小。
//创建AudioTrack

        trackplayer = new AudioTrack(AudioManager.STREAM_MUSIC, 80000,
                AudioFormat.CHANNEL_OUT_STEREO,
                AudioFormat.ENCODING_PCM_16BIT,
                bufsize,
                AudioTrack.MODE_STREAM);

        mGLSurfaceView = findViewById(R.id.surfaceView);

        surfaceHolder = mGLSurfaceView.getHolder();
        surfaceHolder.addCallback(this);

        findViewById(R.id.btn_play).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDataResource(surfaceHolder.getSurface());
            }
        });


    }

    public native void setDataResource(Object glSurfaceView);


    @Override
    public void surfaceCreated(SurfaceHolder holder) {


    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    public void play(byte[] data){
        trackplayer.play() ;//开始
        //往track中写数据
        trackplayer.write(data, 0, data.length) ;
    }


}
