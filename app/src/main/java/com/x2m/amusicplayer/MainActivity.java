package com.x2m.amusicplayer;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.ListView;
import android.widget.TabHost;

import com.x2m.adapter.MusicListViewAdapter;
import com.x2m.service.IPlayerAidlInf;
import com.x2m.service.PlayService;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private ListView musicListView;
    private MusicListViewAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost.setup();


        tabHost.addTab(tabHost.newTabSpec("tab1").setIndicator("本地").setContent(R.id.view1));

        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("歌单").setContent(R.id.view2));

        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("我的").setContent(R.id.view3));

        musicListView = (ListView)findViewById(R.id.view1);

        bindPlayService();

        adapter = new MusicListViewAdapter(this);
        musicListView.setAdapter(adapter);



    }

    private IPlayerAidlInf playSrvInf = null;

    //这里究竟何时调用？playSrvInf在onStart和onCreate中始终是null
    //不得不将adapter.setPlayInf()赋值移动到这里
    private ServiceConnection srvConn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.i(TAG, "connected to service");
            playSrvInf = IPlayerAidlInf.Stub.asInterface(service);
            adapter.setPlayInf(playSrvInf);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.i(TAG, "disconnect from service");
            playSrvInf = null;
        }
    };

    private void bindPlayService() {
        //绑定服务
        Intent intent = new Intent(this, PlayService.class);
        startService(intent);
        bindService(intent, srvConn, Service.BIND_AUTO_CREATE);
    }
}
