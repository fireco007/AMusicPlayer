package com.x2m.amusicplayer;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;

import com.x2m.adapter.MusicListViewAdapter;
import com.x2m.service.IPlayerAidlInf;
import com.x2m.service.PlayService;

import java.lang.ref.WeakReference;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";

    private ListView musicListView;
    private MusicListViewAdapter adapter;
    private TabHost.TabSpec localTab;

    //扫描时播放的动画的控件
    private LinearLayout animLayout;
    private AnimationDrawable loadFileAnim;
    private ImageView aniDis;

    //接受消息通知
    public static final int FILE_LOAD_FINISH = 1;
    public static final int FILE_LOAD_FAILED = 2;

    //修复 : The handler class should be static or leaks might occur
    //参考 : http://www.cnblogs.com/zoejiaen/p/4580572.html
    private MainUIHandler mainUIhandler = new MainUIHandler(this);
    private static class MainUIHandler extends Handler {

        private WeakReference<MainActivity> activity;

        public MainUIHandler(MainActivity context) {
            activity = new WeakReference<>(context);
        }

        @Override
        public void handleMessage(Message msg) {

            switch (msg.what){
                case FILE_LOAD_FINISH:

                    MainActivity temp = activity.get();
                    if (temp != null)
                        temp.loadFileFinished();

                    break;
                default:
                    break;
            }

            super.handleMessage(msg);
        }
    }

    //music文件加载完成
    private void loadFileFinished() {
        localTab.setContent(R.id.musicList);
        animLayout.setVisibility(View.GONE);
        musicListView.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TabHost tabHost = (TabHost)findViewById(R.id.tabhost);
        tabHost.setup();

        //加载动画播放
        localTab = tabHost.newTabSpec("tab1");
        localTab.setIndicator("本地").setContent(R.id.firstView);
        animLayout = (LinearLayout)findViewById(R.id.firstView);
        aniDis = (ImageView)findViewById(R.id.loadAnim);

        //getResource().getDrawable和setBackgroundDrawable 过时
        loadFileAnim = (AnimationDrawable)ContextCompat.getDrawable(this, R.drawable.load_anim);
        aniDis.setBackground(ContextCompat.getDrawable(this, R.drawable.load_anim));
        loadFileAnim.start();

        //绑定底部tab和中间显示的view
        tabHost.addTab(localTab);

        tabHost.addTab(tabHost.newTabSpec("tab2").setIndicator("歌单").setContent(R.id.view2));

        tabHost.addTab(tabHost.newTabSpec("tab3").setIndicator("我的").setContent(R.id.view3));

        //初始化歌曲列表
        musicListView = (ListView)findViewById(R.id.musicList);
        adapter = new MusicListViewAdapter(this, mainUIhandler);
        musicListView.setAdapter(adapter);
        musicListView.setVisibility(View.GONE);
        adapter.loadData();

        //绑定服务
        bindPlayService();
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
