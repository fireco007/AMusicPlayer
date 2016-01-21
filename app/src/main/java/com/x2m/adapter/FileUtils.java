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

        List<String> fileList = new ArrayList<>();

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

    public static void searchFileRecur(String rootPath, String keyword, List<String> outFileList) {

        File rootFile = new File(rootPath);
        if (rootFile.isFile())
            return;

        File[] files = rootFile.listFiles();
        for (File f : files) {

            if (f.isDirectory()) {
                searchFileRecur(f.getPath(), keyword, outFileList);
            } else {

                //支持多个格式，形如：mp3,ape,flac
                String ptnList[] = keyword.toLowerCase().split(",");
                for (String ptn : ptnList) {
                    //判断文件名f中是否包含keyword
                    if (f.getName().toLowerCase().endsWith(ptn)) {
                        //f.getPath()返回文件的路径
                        outFileList.add(f.getPath());
                    }
                }
            }
        }

        return ;
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
