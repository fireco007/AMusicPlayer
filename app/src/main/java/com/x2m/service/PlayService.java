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
public class PlayService extends Service{

    private static String TAG = "MyPlayService";

    private MediaPlayer mediaPlayer = new MediaPlayer();

    class PlayerBinder extends IPlayerAidlInf.Stub
    {

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
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
    }


    public void startPlay(String filePath) {
        try {
            mediaPlayer.setDataSource(filePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stopPlay() {
        mediaPlayer.stop();
    }
}
