package com.x2m.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Administrator on 2016/1/13.
 */
public class PlayService extends Service {

    private static String TAG = "MyPlayService";

    private MediaPlayer mediaPlayer;

    class PlayerBinder extends IPlayerAidlInf.Stub {

        @Override
        public void play(String filePath) throws RemoteException {
            startPlay(filePath);
        }

        @Override
        public void stop() throws RemoteException {
            stopPlay();
        }

        @Override
        public void pause() throws RemoteException {
            mediaPlayer.pause();
        }

        @Override
        public void seek(int offset) throws RemoteException {
            mediaPlayer.seekTo(offset);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "bind service");
        return new PlayerBinder();
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "create service");
        super.onCreate();

        initPlay();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "destroy service");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }

    private void initPlay() {
        mediaPlayer = new MediaPlayer();
        mediaPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        Log.i(TAG, "play complete");

                        //默认播放完成会切换到stop状态
                        //在stop状态下不能setDataSource
                        //详细请参看mediaplayer生命周期
                        mp.reset();
                    }
                });
    }

    public void startPlay(String filePath) {
        try {

            //如果正在播放音乐，需要先重置
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.reset();
            }

            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopPlay() {
        if (mediaPlayer.isPlaying())
            mediaPlayer.stop();
    }
}
