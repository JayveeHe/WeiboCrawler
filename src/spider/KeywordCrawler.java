package spider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class KeywordCrawler {
    /**
     * 根据关键词爬取微博，每次爬取最新的100页； 爬取下来的数据保存在工程根目录下的“keyword-时间.txt”文件中；
     * 爬取的内容采自新浪微博手机版彩版的搜索页面
     *
     * @param keyword 所需搜索的关键词
     * @throws IOException
     * @throws InterruptedException
     * @author Jayvee
     */
    public static String KeywordSearch(String keyword) throws IOException,
            InterruptedException {
        //检测是否存入了cookie
        if (null == BasicTools.www_cookie || null == BasicTools.wap_cookie) {
            if (BasicTools.loadCookies() < 0) {
                System.exit(0);
            }
        }
        // 根据关键词爬取100页微博
        // File file = new File(keyword + "-关键词搜索" + System.currentTimeMillis()
        // + ".txt");
        // FileOutputStream fos = new FileOutputStream(file);
        Date date = new Date(System.currentTimeMillis());
        String taskTime = date.toString();
        System.out.println("开始读取任务，关键词：" + keyword);
        // FileWriter fw = new FileWriter(file);

        // fos.write(("关键词：" + keyword + BasicTools.newline + "任务开始时间：" +
        // taskTime + BasicTools.newline)
        // .getBytes("utf-8"));
        long startTime = System.currentTimeMillis();
        Random r = new Random(System.currentTimeMillis());

        String URL = "http://weibo.cn/search/mblog";

        // JSON格式转换
        JSONObject root = new JSONObject();
        JSONArray jStatuses = new JSONArray();
        try {
            root.put("keyword", keyword);
            root.put("task_time", taskTime);
        } catch (JSONException e4) {
            // TODO Auto-generated catch block
            e4.printStackTrace();
        }

        int page = 1;
        int count = 0;
        for (int i = 0; i < 100; i++) {
//        do{
            System.out.println("开始读取第" + i + "页");
            // 设置请求表头
            Map<String, String> postdata = new HashMap<String, String>();
            postdata.put("keyword", URLEncoder.encode(keyword, "utf-8"));
            postdata.put("hideSearchFrame", "");
            postdata.put("page", page + "");
            Document doc = null;
            // 获取HTML内容，检测超时异常
            try {
                doc = BasicTools.getHTML(URL, postdata, true);
            } catch (SocketTimeoutException e) {
                System.out.println("第" + i + "页读取超时，3秒后重连……");
                Thread.sleep(3000);
                System.out.println("第1次重连……");
                try {
                    doc = BasicTools.getHTML(URL, postdata, true);
                } catch (SocketTimeoutException e1) {
                    System.out.println("第" + i + "页读取超时，3秒后重连……");
                    Thread.sleep(3000);
                    System.out.println("第2次重连");
                    try {
                        doc = BasicTools.getHTML(URL, postdata, true);
                    } catch (SocketTimeoutException e2) {
                        System.out.println("第" + i + "页读取超时，3秒后重连……");
                        Thread.sleep(3000);
                        System.out.println("第3次重连");
                        try {
                            doc = BasicTools.getHTML(URL, postdata, true);
                        } catch (SocketTimeoutException e3) {
                            System.out.println("第" + i + "页读取超时，请检查网络！");
                            System.exit(0);
                        } catch (IOException e3) {
                            e3.printStackTrace();
                        }
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // Document doc = getHTML(URL, postdata);

            // 开始处理这一页的微博
            Elements status = doc.select("div.c[id]");
            // for (Element weibo : status) {
            for (int j = 0; j < status.size(); j++) {
                JSONObject wstatus = new JSONObject();
                Element weibo = status.get(j);
                boolean isRepost = false;
                String text_weibo = count + ":" + BasicTools.newline;
                // 首先获取用户名
                Elements weibo_head = weibo.select("div:has(a.nk)");
                String name = weibo_head.select("a.nk").text();
                text_weibo = text_weibo + name + BasicTools.newline;
                // System.out.println(name);
                // 判断是否为转发微博
                if (weibo_head.select("span.cmt").size() != 0) {
                    // 是转发微博
                    isRepost = true;
                    // 原微博发送者
                    String source_name = weibo_head.select("span.cmt").get(0)
                            .text();
                    text_weibo = text_weibo + source_name + BasicTools.newline;
                    // System.out.println(source_name);
                    // 原微博内容
                    String source_text = weibo_head.select("span.ctt").get(0)
                            .text();
                    text_weibo = text_weibo + source_text + BasicTools.newline;
                    // System.out.println(source_text);
                    // 转发理由
                    Element repost_reason = weibo.select("div:has(span.ct)")
                            .get(1);
                    String reason = repost_reason.text();
                    // 转发理由 去掉赞、评论、转发这些信息
                    Pattern p = Pattern.compile("赞.*");
                    Matcher m = p.matcher(reason);
                    if (m.find()) {
                        reason = m.replaceAll("");
                        Matcher ma = Pattern.compile("转发理由(:|：)").matcher(
                                reason);
                        if (ma.find()) {
                            reason = ma.replaceAll("");
                            text_weibo = text_weibo + reason
                                    + BasicTools.newline;
                        }// System.out.println(reason);
                    }
                    // if (m.find()) {
                    // reason = m.replaceAll("");
                    // text_weibo = text_weibo + reason + BasicTools.newline;
                    // // System.out.println(reason);
                    // }

                    // 进行Json格式转换
                    try {
                        // wstatus.put("isRepost", isRepost);

                        wstatus.put("source_name", source_name);
                        wstatus.put("source_text", source_text);
                        wstatus.put("repost_reason", reason);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                } else {
                    // 不是转发微博
                    Elements sweibo = weibo.select("span.ctt");
                    String oringin_text = sweibo.text();
                    // 去掉第一个冒号
                    oringin_text = oringin_text.substring(1);
                    text_weibo = text_weibo + oringin_text + BasicTools.newline;
                    // System.out.println(sweibo.text());

                    // 进行Json格式转换
                    try {
                        // wstatus.put("isRepost", isRepost);
                        wstatus.put("oringin_text", oringin_text);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
                // 读取发送时间
                String post_time = weibo.select("span.ct").text();
                // 进行Json格式转换
                try {
                    wstatus.put("username", name);
                    wstatus.put("isRepost", isRepost);
                    wstatus.put("post_time", post_time);
                    jStatuses.put(wstatus);
                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                text_weibo = text_weibo + post_time + BasicTools.newline;
                // fos.write(text_weibo.getBytes("utf-8"));
                count++;
            }
            // System.out.println(status);

            float sleep = (float) (r.nextDouble() * 500 + 000);
            System.out.println("为了防止被封，sleep	" + sleep / 1000 + "秒……");
            Thread.sleep((long) sleep);
        }
        // json格式转换
        try {
            root.put("statuses", jStatuses);
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("爬取成功！共爬取" + count + "条微博，用时"
                + (endTime - startTime) / 1000 + "秒");
        return root.toString();
        // fos.flush();
        // fos.close();
        // fw.close();
    }
}
