package demo;

import java.io.*;

import com.sun.org.apache.xpath.internal.SourceTree;
import spider.BasicTools;
import spider.StatusCrawler;

public class testWeibo {

    public static void main(String[] args) {
        //测试微博内容的爬取
        String username = "王思聪";
        String text = null;
        try {
            text = StatusCrawler.userWeiboCrawler(username);
        } catch (InterruptedException | IOException e) {
            e.printStackTrace();
        }
        File file = new File( "微博内容-" +username +"-"+ System.currentTimeMillis() + ".txt");
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            if (text != null) {
                try {
                    fos.write(text.getBytes("utf-8"));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

    }

}
