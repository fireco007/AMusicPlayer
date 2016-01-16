package com.x2m.adapter;

import android.os.Environment;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Administrator on 2016/1/11.
 */
public class FileUtils {


    public static List<String> searchFile(String keyword) {

        File sdCardFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
        String path = sdCardFile.getAbsolutePath();

        List<String> fileList = new ArrayList<String>();

        String result = "";

        File[] files = new File(path + "/netease/cloudmusic/Music/").listFiles();
        for (File f : files) {

            //判断文件名f中是否包含keyword
            if (f.getName().indexOf(keyword) >= 0) {
                //f.getPath()返回文件的路径
                fileList.add(f.getPath());
            }
        }

        return fileList;
    }

    /**
     * 遍历 "system/etc/vold.fstab” 文件，获取全部的Android的挂载点信息
     *
     * @return
     */
/*    private static ArrayList<String> getDevMountList() {
        String[] toSearch = FileUtils.readFile("/etc/vold.fstab").split(" ");
        ArrayList<String> out = new ArrayList<String>();
        for (int i = 0; i < toSearch.length; i++) {
            if (toSearch[i].contains("dev_mount")) {
                if (new File(toSearch[i + 2]).exists()) {
                    out.add(toSearch[i + 2]);
                }
            }
        }
        return out;
    }*/
}
