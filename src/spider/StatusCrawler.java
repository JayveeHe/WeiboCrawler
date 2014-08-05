package spider;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
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

public class StatusCrawler {

    /**
     * 爬取指定用户的所有微博，所得数据保存在根目录下txt
     *
     * @param username 用户的screen_name
     * @throws InterruptedException
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    public static String userWeiboCrawler(String username)
            throws InterruptedException,
            IOException {
        String uid = BasicTools.username2uid(username);
        String URL = "http://weibo.cn/" + uid;
        Map<String, String> data = new HashMap<>();
        int page = 1;
        int count = 0;
        boolean hasNext = true;
        boolean isBusy = false;

        //检测是否存入了cookie
        if (null == BasicTools.www_cookie || null == BasicTools.wap_cookie) {
            if (BasicTools.loadCookies() < 0) {
                System.exit(0);
            }

        }

        // File file = new File(username + "-" + "微博内容" + "-"
        // + System.currentTimeMillis() + ".txt");
        // FileOutputStream fos = new FileOutputStream(file);
        Date date = new Date(System.currentTimeMillis());
        String taskTime = date.toString();
        System.out.println("开始读取任务，用户：" + username);

        // fos.write(("用户：" + username + newline + "任务开始时间：" + taskTime +
        // newline)
        // .getBytes("utf-8"));
        long startTime = System.currentTimeMillis();
        Random r = new Random(System.currentTimeMillis());

        // JSON格式转换
        JSONObject root = new JSONObject();
        JSONArray jStatuses = new JSONArray();
        try {
            root.put("username", username);
            root.put("task_time", taskTime);
        } catch (JSONException e4) {
            // TODO Auto-generated catch block
            e4.printStackTrace();
        }

        do {
            data.put("page", page + "");
            Document doc = null;
            // 获取HTML内容，检测超时异常
            System.out.println("正在读取第" + page + "页");
            try {
                doc = BasicTools.getHTML(URL, data, false);
            } catch (SocketTimeoutException e) {
                System.out.println("第" + page + "页读取超时，3秒后重连……");
                Thread.sleep(3000);
                System.out.println("第1次重连……");
                try {
                    doc = BasicTools.getHTML(URL, data, false);
                } catch (SocketTimeoutException e1) {
                    System.out.println("第" + page + "页读取超时，3秒后重连……");
                    Thread.sleep(3000);
                    System.out.println("第2次重连");
                    try {
                        doc = BasicTools.getHTML(URL, data, false);
                    } catch (SocketTimeoutException e2) {
                        System.out.println("第" + page + "页读取超时，3秒后重连……");
                        Thread.sleep(3000);
                        System.out.println("第3次重连");
                        try {
                            doc = BasicTools.getHTML(URL, data, false);
                        } catch (SocketTimeoutException e3) {
                            System.out.println("第" + page + "页读取超时，请检查网络！");
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
            // 判断是否有下一页
            Elements pagelist = null;
            if (doc != null) {
                pagelist = doc.select("div.pa");
            }
            hasNext = pagelist.select("div:contains(下页)").size() != 0;
            isBusy = pagelist.select("div:contains(系统繁忙)").size() != 0;
            // 开始处理这一页的微博
            Elements status = doc.select("div.c[id]");
            // for (Element weibo : status) {
            for (int j = 0; j < status.size(); j++) {
                JSONObject wstatus = new JSONObject();
                Element weibo = status.get(j);
                boolean isRepost = false;
//                String text_weibo = count + ":" + BasicTools.newline;
                // 判断是否为转发微博
                if (weibo.select("span.cmt").size() != 0) {
                    // 是转发微博
                    isRepost = true;
                    // 原微博发送者
                    String source_name = weibo.select("span.cmt").get(0).text();
                    Matcher sm = Pattern.compile(" .* ").matcher(source_name);
                    if (sm.find()) {
                        source_name = sm.group().replaceAll(" ", "");
                    }
//                    text_weibo = text_weibo + source_name + BasicTools.newline;
                    // System.out.println(source_name);
                    // 原微博内容
                    String source_text = weibo.select("span.ctt").get(0).text();
//                    text_weibo = text_weibo + source_text + BasicTools.newline;
                    // System.out.println(source_text);
                    // 转发理由
                    Element repost_reason = weibo.select("div:contains(转发理由)")
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
//                            text_weibo = text_weibo + reason
//                                    + BasicTools.newline;
                        }// System.out.println(reason);
                    }

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
                    // oringin_text = oringin_text.substring(1);
//                    text_weibo = text_weibo + oringin_text + BasicTools.newline;
                    // System.out.println(sweibo.text());

                    // 进行Json格式转换
                    try {
                        // wstatus.put("isRepost", isRepost);
                        wstatus.put("oringin_text", oringin_text);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }
                // 读取发送时间
                String post_time = weibo.select("span.ct").text();
                // 进行Json格式转换
                try {
                    wstatus.put("isRepost", isRepost);
                    wstatus.put("post_time", post_time);
                    jStatuses.put(wstatus);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
//                text_weibo = text_weibo + post_time + BasicTools.newline;
                // System.out.println(text_weibo);
                // fos.write(text_weibo.getBytes("utf-8"));
                count++;
            }
            float sleep = (float) (r.nextDouble() * 500);
            System.out.println("为了防止被封，sleep	" + sleep / 1000 + "秒……");
            // System.out.println("为了防止被封，sleep	" + crawlTime + "毫秒……");
            Thread.sleep((long) sleep);
            // Thread.sleep((long) crawlTime * 10);
            if (!hasNext && isBusy) {
                System.out.println(doc.html());
            }
            if (!isBusy && hasNext)
                page++;
        } while (hasNext || isBusy);
        // json格式转换
        try {
            root.put("statuses", jStatuses);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        System.out.println("爬取成功！共爬取" + count + "条微博，用时"
                + (endTime - startTime) / 1000 + "秒");
        // fos.write(root.toString().getBytes("utf-8"));
        // fos.flush();
        // fos.close();

        return root.toString();

    }

//    private Document loadHTML()
//    {
//
//    }

}
