package spider;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import data.Followdata;

public class FollowCrawler {
    /**
     * 根据uid进行关注列表的爬取
     *
     * @param uid 该指定用户的uid
     * @return 包含 Followdata的arraylist
     * @throws JSONException
     */
    public static ArrayList<Followdata> FollowDataCrawler(String uid)
            throws JSONException {

        //检测是否存入了cookie
        if (null == BasicTools.www_cookie || null == BasicTools.wap_cookie) {
            if (BasicTools.loadCookies() < 0) {
                System.exit(0);
            }
        }

        ArrayList<Followdata> list_follows = new ArrayList<>();

        // 首先根据uid获取对应的page_id
        // // 防止特殊用户页面id，103开头的为特殊用户，需要转换成普通的100开头的page_id
        String page_id = "100505" + BasicTools.getPageIdByUid(uid).substring(6);

        int followcount = 0;
        int page = 1;
        int maxpage = 1;
        boolean hasNextPage = true;
        do {
            System.out.println("正在读取第" + page + "页");
            Document doc = null;
            // 根据page_id组成js请求地址
            String URL = "http://m.weibo.cn/page/json";
            Map<String, String> params = new HashMap<>();
            params.put("module", "user");
            params.put("action", "FOLLOWERS");
            params.put("itemid", "FOLLOWERS");
            params.put("title", "关注");
            params.put("containerid", page_id + "_-_FOLLOWERS");
            params.put("page", page + "");
            try {
                doc = Jsoup
                        .connect(URL)
                        .data(params)
                        .ignoreContentType(true)
                        .header("Accept-Encoding", "gzip,deflate,sdch")
                        .header("Accept-Language", "zh-CN,zh;q=0.8,en;q=0.6")
                        .header("Accept",
                                "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("User-Agent",
                                "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
                        .timeout(5000).cookie("auth", BasicTools.wap_cookie)
                        .get();
            } catch (SocketTimeoutException e) {
                System.out.println("读取超时，正在重试");
                try {
                    doc = Jsoup
                            .connect(URL)
                            .data(params)
                            .ignoreContentType(true)
                            .header("Accept-Encoding", "gzip,deflate,sdch")
                            .header("Accept-Language",
                                    "zh-CN,zh;q=0.8,en;q=0.6")
                            .header("Connection", "keep-alive")
                            .header("Accept",
                                    "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                            .header("User-Agent",
                                    "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
                            .timeout(5000)
                            .cookie("auth", BasicTools.wap_cookie).get();
                } catch (SocketTimeoutException e2) {
                    System.out.println("读取超时，正在重试");
                    try {
                        doc = Jsoup
                                .connect(URL)
                                .data(params)
                                .ignoreContentType(true)
                                .header("Accept-Encoding", "gzip,deflate,sdch")
                                .header("Accept-Language",
                                        "zh-CN,zh;q=0.8,en;q=0.6")
                                .header("Accept",
                                        "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                                .header("User-Agent",
                                        "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
                                .timeout(5000)
                                .cookie("auth", BasicTools.wap_cookie).get();
                    } catch (IOException e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            // 获取最原始的数据
            String html = doc.html();
            String databody = null;
            // 进行清理
            // unicode转中文
            html = BasicTools.unicodeToString(html);
            // System.out.println(html);
            Matcher m = Pattern.compile("\\\\").matcher(html);
            if (m.find()) {
                html = m.replaceAll("");
            }
            m = Pattern.compile("&quot;").matcher(html);
            if (m.find()) {
                html = m.replaceAll("\"");
            }
            // 清理text中的多余"
            StringBuffer textSB = new StringBuffer();
            m = Pattern.compile("\"text\":.*?\"created").matcher(html);
            while (m.find()) {
                String temp = m.group();
                String[] arry = temp.split("\"");
                if (arry.length - 1 > 5) {
                    System.out.println("会有错误！");
                    // System.out.println(temp);
                    //
                    Matcher t = Pattern.compile("\"").matcher(m.group());
                    int count = 0;
                    StringBuffer sb = new StringBuffer();
                    while (t.find()) {
                        count++;
                        if (count < 4 || count > (arry.length - 1 - 2)) {
                            continue;
                        } else {
                            if (count != arry.length - 3) {
                                Matcher ttt = t.appendReplacement(sb, "\'");
                                // System.out.println(sb);
                            } else {
                                t.appendReplacement(sb, "\'");
                                t.appendTail(sb);
                            }
                        }
                        // System.out.println(sb);
                    }
                    m.appendReplacement(textSB, sb.toString());// 替换更换后的文本
                }
            }
            m.appendTail(textSB);
            html = textSB.toString();

            // 清理desc1中的多余"
            StringBuffer descSB = new StringBuffer();
            m = Pattern.compile("\"desc1\":.*?\"desc2").matcher(html);
            while (m.find()) {
                String temp = m.group();
                String[] arry = temp.split("\"");
                if (arry.length - 1 > 5) {
                    // System.out.println("会有错误！");
                    // System.out.println(temp);
                    //
                    Matcher t = Pattern.compile("\"").matcher(m.group());
                    int count = 0;
                    StringBuffer sb = new StringBuffer();
                    while (t.find()) {
                        count++;
                        if (count < 4 || count > (arry.length - 1 - 2)) {
                            continue;
                        } else {
                            if (count != arry.length - 3) {
                                Matcher ttt = t.appendReplacement(sb, "\'");
                                // System.out.println(sb);
                            } else {
                                t.appendReplacement(sb, "\'");
                                t.appendTail(sb);
                            }
                        }
                        // System.out.println(sb);
                    }
                    m.appendReplacement(descSB, sb.toString());// 替换更换后的文本
                }
            }
            m.appendTail(descSB);
            html = descSB.toString();

            // System.out.println("转换后文本：" + html);

            // 将处理出来的数据传给Jsoup转换为document
            doc = Jsoup.parse(html, "");
            databody = doc.select("body").text();
            // 通过json格式读取
            JSONObject root = null;
            try {
                root = (JSONObject) new JSONTokener(databody).nextValue();
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            if (root.getInt("ok") == 1) {
                maxpage = root.getInt("maxPage");
                JSONArray users = root.getJSONArray("users");
                for (int i = 0; i < users.length(); i++) {
                    JSONObject user = users.getJSONObject(i);
                    // 处理单条关注用户
                    // uid
                    String user_id = user.getString("id");
                    // 用户名
                    String username = user.getString("screen_name");
                    // 获取关注、粉丝、微博数
                    String num_fans = user.getString("fansNum");
                    String num_weibo = user.getString("statuses_count");
                    // 获取最近一条的微博
                    String recentText = user.getString("text");
                    // 用户性别
                    String sex = user.getString("gender");
                    boolean isVerified = user.getBoolean("verified");

                    Followdata fd = new Followdata(user_id, username,
                            recentText, num_fans, num_weibo, sex, isVerified);
                    list_follows.add(fd);
                    followcount++;
                }
            }
            // 检测是否还有下一页
            // }
            hasNextPage = page < maxpage;
            page++;
        } while (hasNextPage);
        System.out.println("用户：" + uid + "微博关注爬取完成，共爬取" + followcount + "个关注");

        return list_follows;
    }

    /**
     * 根据用户名进行关注列表的爬取
     *
     * @param username 用户名
     * @return
     */
    public static String FollowerCrawler(String username) {
        String uid = BasicTools.username2uid(username);
        ArrayList<Followdata> list_follows = new ArrayList<Followdata>();
        JSONObject root = new JSONObject();
        try {
            list_follows = FollowDataCrawler(uid);
            root.put("username", username);
            root.put("task_time", new Date(System.currentTimeMillis()));
            JSONArray follows = new JSONArray();
            for (Followdata fd : list_follows) {
                JSONObject followUser = new JSONObject();
                followUser.put("uid", fd.getUid());
                followUser.put("username", fd.getUsername());
                followUser.put("num_fans", fd.getNum_fans());
                followUser.put("num_weibo", fd.getNum_weibo());
                followUser.put("recentText", fd.getRecentText());
                followUser.put("sex", fd.getSex());
                followUser.put("isVerified", fd.isVerified());
                follows.put(followUser);
            }
            root.put("follows", follows);
            return root.toString();

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;

    }
}
