package com.x2m.adapter;

import android.content.Context;
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

import static com.x2m.adapter.FileUtils.searchFile;


/**
 * Created by Administrator on 2016/1/11.
 */
public class MusicListViewAdapter extends BaseAdapter {

    private static final String TAG = "MusicListViewAdapter";

    private LayoutInflater mInflater;//得到一个LayoutInfalter对象用来导入布局 /*构造函数*/
    private List<Pair<String, Tag>> mFileList = new ArrayList<>();
    private IPlayerAidlInf playSrvInf;
    private Handler notifyHandler;

    public MusicListViewAdapter(Context context, Handler uiHandler) {
        this.mInflater = LayoutInflater.from(context);
        notifyHandler = uiHandler;
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
        final Pair<String,Tag> meta = mFileList.get(position);
        Log.i(TAG, meta.first);

        //see http://www.jthink.net/jaudiotagger/examples_read.jsp for more tags
        String artist = meta.second.getFirst(FieldKey.ARTIST);
        String title = meta.second.getFirst(FieldKey.TITLE);

        if (artist.isEmpty() || title.isEmpty()) {
            title = meta.first.substring(meta.first.lastIndexOf("/") + 1);
            holder.title.setText(title);
            holder.text.setText("未知");
        } else {
            holder.title.setText(title);
            holder.text.setText(artist);
        }

        /*为Button添加点击事件*/
        holder.bt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (playSrvInf == null) {
                    Log.e(TAG, "play service not bind yet!");
                    return;
                }

                try {
                    playSrvInf.play(meta.first);
                } catch (RemoteException e) {
                    Log.e(TAG, e.toString());
                }
            }
        });

        return convertView;
    }

    public void rescanLocalFile(final String path) {
        mFileList.clear();
        Thread th = new Thread(
                new Runnable() {
                    @Override
                    public void run() {
                        List<File> fileList = new ArrayList<>();

                        FileUtils.searchFileRecur(path, ".mp3,.ape,.flac", fileList);

                        for (File file : fileList) {
                            try {
                                AudioFile af = AudioFileIO.read(file);
                                Tag tag = af.getTag();
                                Pair<String, Tag> pair = new Pair<>(file.getAbsolutePath(), tag);
                                mFileList.add(pair);
                            } catch (CannotReadException e) {
                                e.printStackTrace();
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (TagException e) {
                                e.printStackTrace();
                            } catch (ReadOnlyFileException e) {
                                e.printStackTrace();
                            } catch (InvalidAudioFrameException e) {
                                e.printStackTrace();
                            }
                        }

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
}
