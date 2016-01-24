package com.x2m.adapter;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.x2m.amusicplayer.MainActivity;
import com.x2m.amusicplayer.R;
import com.x2m.db.TB_Music;
import com.x2m.db.dao.DaoMaster;
import com.x2m.db.dao.DaoSession;
import com.x2m.db.dao.TB_MusicDao;
import com.x2m.service.IPlayerAidlInf;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import de.greenrobot.dao.query.QueryBuilder;

import static com.x2m.adapter.FileUtils.searchFile;


/**
 * Created by Administrator on 2016/1/11.
 */
public class MusicListViewAdapter extends BaseAdapter {

    private static final String TAG = "MusicListViewAdapter";

    private LayoutInflater mInflater = null;//得到一个LayoutInfalter对象用来导入布局 /*构造函数*/
    private List<TB_Music> mFileList = new ArrayList<>(); //避免第一次加载抛异常
    private IPlayerAidlInf playSrvInf = null;
    private Handler notifyHandler;
    private Context ctx;

    public MusicListViewAdapter(Context context, Handler uiHandler) {
        this.mInflater = LayoutInflater.from(context);
        notifyHandler = uiHandler;
        ctx = context;
    }

    public void setPlayInf(IPlayerAidlInf playInf) {
        playSrvInf = playInf;
    }

    @Override
    public int getCount() {
        return mFileList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        final ViewHolder holder;

        //观察convertView随ListView滚动情况
        Log.v("MyListViewBase", "getView " + position + " " + convertView);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.music_list_view_item, null);
            holder = new ViewHolder();

            /*得到各个控件的对象*/
            holder.title = (TextView) convertView.findViewById(R.id.ItemTitle);
            holder.text = (TextView) convertView.findViewById(R.id.ItemText);
            holder.bt = (Button) convertView.findViewById(R.id.ItemButton);
            convertView.setTag(holder);//绑定ViewHolder对象
        } else {
            holder = (ViewHolder) convertView.getTag();//取出ViewHolder对象
        }

        /*设置TextView显示的内容，即我们存放在动态数组中的数据*/
        final TB_Music music = mFileList.get(position);

        holder.title.setText(music.getTitle());
        holder.text.setText(music.getArtist());


        /*为Button添加点击事件*/
        holder.bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (playSrvInf == null) {
                    Log.e(TAG, "play service not bind yet!");
                    return;
                }

                try {
                    playSrvInf.play(music.getPath());
                } catch (RemoteException e) {
                    Log.e(TAG, e.toString());
                }
            }
        });

        return convertView;
    }

    public void loadData() {
        DBUtils db = new DBUtils();
        mFileList = db.getAllMusic();

        if (mFileList == null || mFileList.size() <= 0) {
            Log.i(TAG, "rescan device");
            rescanLocalFile(Environment.getExternalStorageDirectory().getAbsolutePath());
        } else {
            Log.i(TAG, "load from app database");
            Message msg = new Message();
            msg.what = MainActivity.FILE_LOAD_FINISH;
            notifyHandler.sendMessage(msg);
        }

    }

    public void rescanLocalFile(final String path) {
        mFileList.clear();
        Thread th = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        List<File> fileList = new ArrayList<>();

                        FileUtils.searchFileRecur(path, ".mp3,.ape,.flac", fileList);
                        DBUtils db = new DBUtils();

                        for (File file : fileList) {
                            db.insertMusic2DB(file);
                        }

                        mFileList = db.getAllMusic();

                        Message msg = new Message();
                        msg.what = MainActivity.FILE_LOAD_FINISH;
                        notifyHandler.sendMessage(msg);
                    }
                }
        );
        th.setDaemon(true);
        th.start();
    }

    public final class ViewHolder {
        public TextView title;
        public TextView text;
        public Button bt;
    }

    public class DBUtils {

        //数据库操作
        private SQLiteOpenHelper dbHelper;
        private SQLiteDatabase db;
        private DaoMaster daoMaster;

        public DBUtils() {
            dbHelper = new DaoMaster.DevOpenHelper(ctx, "musicdb", null);
            db = dbHelper.getWritableDatabase();
            daoMaster = new DaoMaster(db);
        }

        public void insertMusic2DB(File file) {
            DaoSession daoSession = daoMaster.newSession();
            TB_MusicDao musicDao = daoSession.getTB_MusicDao();

            String path = file.getAbsolutePath();
            TB_Music music = new TB_Music();
            music.setPath(path);

            AudioFile af = null;
            try {
                af = AudioFileIO.read(file);
                Tag tag = af.getTag();

                //see http://www.jthink.net/jaudiotagger/examples_read.jsp for more tags
                String artist = tag.getFirst(FieldKey.ARTIST);
                String title = tag.getFirst(FieldKey.TITLE);
                String album = tag.getFirst(FieldKey.ALBUM);
                String type = tag.getFirst(FieldKey.TAGS);

                music.setArtist(artist.length() <= 1 ? "未知" : artist);
                music.setTitle(title.length() <= 1 ? path.substring(path.lastIndexOf("/") + 1) : title);
                music.setAlbum(album);
                music.setType(type);
                music.setStatus(1);

                musicDao.insert(music);
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

        public List<TB_Music> getAllMusic() {
            DaoSession daoSession = daoMaster.newSession();
            TB_MusicDao musicDao = daoSession.getTB_MusicDao();
            QueryBuilder queryAll = musicDao.queryBuilder().where(TB_MusicDao.Properties.Status.ge(1));
            return queryAll.list();
        }
    }
}
