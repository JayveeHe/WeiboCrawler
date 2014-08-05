package data;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * 存储关注好友的数据类
 * 
 * @author Jayvee
 * 
 */
public class Followdata {

	String uid;
	String username;
	String recentText;
	String num_fans;
	String num_weibo;
	String sex;
	boolean isVerified;

	public Followdata(String uid, String username, String recentText,
			String num_fans, String num_weibo, String sex, boolean isVerified) {
		super();
		this.uid = uid;
		this.username = username;
		this.recentText = recentText;
		this.num_fans = num_fans;
		this.num_weibo = num_weibo;
		this.sex = sex;
		this.isVerified = isVerified;
	}

	public String getUid() {
		return uid;
	}

	public String getUsername() {
		return username;
	}

	public String getRecentText() {
		return recentText;
	}

	public String getNum_fans() {
		return num_fans;
	}

	public String getNum_weibo() {
		return num_weibo;
	}

	public String getSex() {
		return sex;
	}

	public boolean isVerified() {
		return isVerified;
	}

	public JSONObject toJson() throws JSONException {
		JSONObject root = new JSONObject();
		root.put("uid", uid);
		root.put("screen_name", username);
		root.put("num_fans", num_fans);
		root.put("num_weibo", num_weibo);
		root.put("sex", sex);
		root.put("isVerified", isVerified);
		return root;
	}

	@Override
	public String toString() {
		return "Followdata [uid=" + uid + ", username=" + username
				+ ", recentText=" + recentText + ", num_fans=" + num_fans
				+ ", num_weibo=" + num_weibo + ", sex=" + sex + ", isVerified="
				+ isVerified + "]";
	}

}
