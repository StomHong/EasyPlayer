package com.stomhong.easyplayer;

/**
 * Created by StomHong on 2018/3/30.
 */

public final class EasyPlayer {
    static {
        System.loadLibrary("EasyPlayer");
    }

    public void init(String url){
        nativeInit(url);
    }

    public void play(){
        nativePlay();
    }

    public void pause(){
        nativePause();
    }

    public void destroy(){
        nativeDestroy();
    }

    public native void nativeInit(String url);
    public native void nativePlay();
    public native void nativePause();
    public native void nativeDestroy();
}
