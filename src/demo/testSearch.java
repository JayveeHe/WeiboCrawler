package demo;

import java.io.*;

import spider.BasicTools;
import spider.FollowCrawler;
import spider.KeywordCrawler;

public class testSearch {

    public static void main(String[] args) throws IOException, InterruptedException {
        //测试关键词的搜索
        String keyword = "NLP";
        long starttime = System.currentTimeMillis();
        String text = KeywordCrawler.KeywordSearch(keyword);
        File file = new File("关键词搜索-" + keyword + "-" + System.currentTimeMillis() + ".txt");
        FileOutputStream fos = null;
        fos = new FileOutputStream(file);
        fos.write(text.getBytes("utf-8"));
        System.out.println("用时：" + (System.currentTimeMillis() - starttime)
                + "毫秒");

    }

}
