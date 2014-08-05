package spider;

import java.io.*;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import data.Followdata;

public class BasicTools {


    protected static String www_cookie = null;
    protected static String wap_cookie = null;

    protected static String newline = System.getProperty("line.separator");// 系统的换行符

    /**
     * 根据config文件夹下的cookies.conf内容，填写cookie值
     */
    public static int loadCookies() {
        try {
            String cookies_text = new String(File2byte("./config/cookies.conf"), "utf-8");
//            JSONTokener jsonTokener = new JSONTokener(cookies_text);
//            JSONObject cookies_root = (JSONObject) jsonTokener.nextValue();
//            www_cookie = cookies_root.getString("www_cookie");
//            wap_cookie = cookies_root.getString("wap_cookie");
            //通过正则表达式进行cookie字段的获取
            String[] temp = cookies_text.split(newline);
            Matcher m = Pattern.compile("www_cookie=.+").matcher(temp[0]);
            if (m.find()) {
                Matcher text = Pattern.compile("www_cookie=").matcher(m.group());
                if (text.find()) {
                    www_cookie = text.replaceAll("");
                }
            }
            m = Pattern.compile("wap_cookie=.+").matcher(temp[1]);
            if (m.find()) {
                Matcher text = Pattern.compile("wap_cookie=").matcher(m.group());
                if (text.find()) {
                    wap_cookie = text.replaceAll("");
                }
            }

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        if (null == www_cookie || null == wap_cookie) {
            System.out.println("cookies读取失败，请检查cookie.conf文件内容！");
            return -1;
        } else {
            if (Pattern.compile("[\\u4e00-\\u9fa5]").matcher(www_cookie).find() || Pattern.compile("[\\u4e00-\\u9fa5]").matcher(wap_cookie).find()) {
                System.out.println("cookie格式错误，请检查cookie.conf文件内容！");
                return -1;
            }
            System.out.println("cookies读取完毕！");
            return 1;

        }

    }

    protected static String username2uid(String username) {
        Map<String, String> data = new HashMap<>();
        data.put("screen_name", username);
        data.put("access_token", "2.00zooRcF0m6btu8b0dfb8e19N11dDB");
        Document doc = null;
        try {
            doc = Jsoup.connect("https://api.weibo.com/2/users/show.json")
                    .data(data).ignoreContentType(true).get();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        JSONTokener jsonTokener = new JSONTokener(doc.text());
        String uid = null;
        try {
            JSONObject root = (JSONObject) jsonTokener.nextValue();
            uid = root.getString("idstr");
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return uid;

    }

    /**
     * 根据uid获取电脑端页面的page_id
     *
     * @param uid
     * @return
     */
    protected static String getPageIdByUid(String uid) {


        Document doc = null;
        // 由用户页面的相关信息获取包含page_id的信息
        String homeURL = "http://weibo.com/u/" + uid;
        try {
            doc = Jsoup
                    .connect(homeURL)
                    .header("User-Agent",
                            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
                    .timeout(0).cookie("auth", www_cookie).get();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        Matcher ret = Pattern.compile("retcode=\\d+").matcher(doc.html());
        if (ret.find()) {
            Matcher retid = Pattern.compile("\\d+").matcher(ret.group());
            if (retid.find()) {
                String retcode = retid.group();
                try {
                    doc = Jsoup
                            .connect(homeURL + "?retcode=" + retcode)
                            .header("User-Agent",
                                    "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
                            .timeout(0).cookie("auth", www_cookie).get();
                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }
        String homepage = doc.html();
        Pattern p = Pattern.compile("\\$CONFIG\\[\\'page_id\\'\\].*");
        Matcher m = p.matcher(homepage);
        String page_id = null;
        if (m.find()) {
            String temp = m.group();
            m = Pattern.compile("\\d{1,}").matcher(temp);
            if (m.find()) {
                page_id = m.group();
            }
        }
        return page_id;
    }

    /**
     * 获取HTML的函数
     *
     * @param URL      请求的地址
     * @param postdata 请求的表单数据
     * @param isPost   是否为post请求方式
     * @return 返回jsoup的document对象
     * @throws IOException
     */
    protected static Document getHTML(String URL, Map<String, String> postdata,
                                      boolean isPost) throws IOException {
        Document doc = null;

        int timeout = 5000;// 超时限制
        if (isPost) {
            doc = Jsoup
                    .connect(URL)
                    .header("User-Agent",
                            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
                    .data(postdata).timeout(timeout).cookie("auth", wap_cookie)
                    .post();
        } else {
            doc = Jsoup
                    .connect(URL)
                    .header("User-Agent",
                            "Mozilla/5.0 (Macintosh; U; Intel Mac OS X 10.4; en-US; rv:1.9.2.2) Gecko/20100316 Firefox/3.6.2")
                    .data(postdata).timeout(timeout).cookie("auth", wap_cookie)
                    .get();
        }
        System.out.println("读取成功！");
        return doc;
    }

    protected static String unicodeToString(String str) {

        Pattern pattern = Pattern.compile("(\\\\u(\\p{XDigit}{4}))");
        Matcher matcher = pattern.matcher(str);
        char ch;
        while (matcher.find()) {
            ch = (char) Integer.parseInt(matcher.group(2), 16);
            str = str.replace(matcher.group(1), ch + "");
        }
        return str;
    }

    /**
     * 实现File转Bytes，用于流传输。
     *
     * @param filePath
     * @return 字节形式的文件内容
     */
    protected static byte[] File2byte(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

}
