package demo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import data.Followdata;
import spider.BasicTools;
import spider.FollowCrawler;

public class testFollower {
    public static void main(String args[]) throws JSONException, IOException {
        //此为测试关注列表的爬取
        String username = "西山居游戏";
        long starttime = System.currentTimeMillis();
        String text = FollowCrawler.FollowerCrawler(username);
        File file = new File("关注列表-" + username + "-" + System.currentTimeMillis() + ".txt");
        FileOutputStream fos = null;
        fos = new FileOutputStream(file);
        fos.write(text.getBytes("utf-8"));
        System.out.println("用时：" + (System.currentTimeMillis() - starttime)
                + "毫秒");

        System.out.println("输出成功");
    }
}
