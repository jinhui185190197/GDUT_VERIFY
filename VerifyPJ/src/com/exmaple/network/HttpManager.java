package com.exmaple.network;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.nfc.Tag;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * 实现网络操作
 * 
 * @author 橘子哥
 *
 */

public class HttpManager {

	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.3; WOW64; Trident/7.0; rv:11.0) like Gecko";
	public static final String USER_AGENT__ = "Mozilla/5.0 (Windows NT 6.3; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.63 Safari/537.36";

	public static final String HOST = "http://jwgl.gdut.edu.cn";
	/**
	 * 登陆首页的URI地址
	 */
	public static final String LOGINPAGE_URI = "http://jwgl.gdut.edu.cn/";

	/**
	 * 登陆请求接口地址
	 */
	public static final String LOGIN_URI = "http://jwgl.gdut.edu.cn/default2.aspx";
	/**
	 * 请求验证码地址URI
	 */
	public static final String VERIFY_URI = "http://jwgl.gdut.edu.cn/CheckCode.aspx";

	public static String COOKIE = "";
	public static String __VIEWSTATE = "";
	public static String LOCATION = "";

	public HttpManager() {

	}

	/**
	 * 进行第一次网页请求，希望获取对应的cookies值
	 * 
	 */
	public void getWebCookies(final Handler handler) {

		Thread httpThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					HttpGet httpGet = new HttpGet(LOGINPAGE_URI);
					HttpClient httpClient = new DefaultHttpClient();
					HttpResponse response = httpClient.execute(httpGet);
					String cookie = ((AbstractHttpClient) httpClient)
							.getCookieStore().getCookies().get(0).getValue();
					Log.d("TAG", "response code: "
							+ response.getStatusLine().getStatusCode());
					Log.e("TAG", "cookie: " + cookie);
					COOKIE = cookie;
					Pattern pattern = Pattern.compile("__VIEWSTATE");
					Matcher matcher = null;
					String html = "";
					int legth = 0;
					byte[] buffer = new byte[1024];
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent(), "GB2312"));
					while ((html = reader.readLine()) != null) {
						// Log.e("TAG", "Response: " + html);
						matcher = pattern.matcher(html);
						if (matcher.find()) {
							__VIEWSTATE = html.substring(47, 95);
							Log.d("TAG", "__VIEWSTATE: " + __VIEWSTATE);
						}
					}
					handler.sendEmptyMessage(1);

				} catch (Exception e) {
					// TODO: handle exception
				}

			}
		});
		httpThread.start();
	}

	/**
	 * 根据第一次请求获得的cookie， 请求相应的验证码图片
	 */
	public void getVerifyImg(final Handler handler) {
		Thread httpThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Log.d("TAG", "cookie: " + COOKIE);
					HttpGet httpGet = new HttpGet(VERIFY_URI);

					httpGet.setHeader("Cookie", "ASP.NET_SessionId=" + COOKIE);
					HttpClient httpClient = new DefaultHttpClient();
					HttpResponse response = httpClient.execute(httpGet);
					Log.d("TAG", "response code: "
							+ response.getStatusLine().getStatusCode());
					byte[] buffer = EntityUtils.toByteArray(response
							.getEntity());
					Bitmap bitmap = BitmapFactory.decodeByteArray(buffer, 0,
							buffer.length);
					Message message = new Message();
					message.obj = bitmap;
					message.what = 2;
					handler.sendMessage(message);
				} catch (Exception e) {

					e.printStackTrace();
				}

			}
		});
		httpThread.start();
	}

	/**
	 * 进行登陆操作
	 * 
	 * @param account
	 * @param pwd
	 * @param code
	 */
	public void doLoginAction(final String account, final String pwd,
			final String code, final Handler handler) {
		Thread httpThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					HttpPost httpPost = new HttpPost(LOGIN_URI);
					// 配置post参数
					List<NameValuePair> params = new ArrayList<NameValuePair>();

					String result = null;
					params.add(new BasicNameValuePair("__VIEWSTATE",
							__VIEWSTATE));
					params.add(new BasicNameValuePair("txtUserName", account));
					params.add(new BasicNameValuePair("TextBox2", pwd));
					params.add(new BasicNameValuePair("txtSecretCode", code));
					params.add(new BasicNameValuePair("RadioButtonList1", "学生"));
					params.add(new BasicNameValuePair("Button1", ""));
					params.add(new BasicNameValuePair("lbLanguage", ""));

					httpPost.setHeader("Cookie", "ASP.NET_SessionId=" + COOKIE);
					httpPost.getParams().setParameter(
							ClientPNames.HANDLE_REDIRECTS, false);
					httpPost.setHeader("Content-Type",
							"application/x-www-form-urlencoded;charset=gbk");
					httpPost.addHeader("User-Agent", USER_AGENT);
					httpPost.setEntity(new UrlEncodedFormEntity(params, "GBK"));
					HttpClient httpClient = new DefaultHttpClient();
					httpClient.getParams().setParameter(
							ClientPNames.ALLOW_CIRCULAR_REDIRECTS, false);
					HttpResponse response = httpClient.execute(httpPost);
					// String cookie =
					// response.getFirstHeader("Cookie").getValue();
					Log.e("TAG", "cookie: " + response.getHeaders("Set-Cookie"));
					Log.d("TAG", "response code: "
							+ response.getStatusLine().getStatusCode());
					Log.d("TAG",
							"getFirstHeader:"
									+ response.getFirstHeader("Location")
											.getValue());
					LOCATION = response.getFirstHeader("Location").getValue();
					String html = "";
					int legth = 0;
					byte[] buffer = new byte[1024];
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(response.getEntity()
									.getContent(), "GB2312"));
					while ((html = reader.readLine()) != null) {
						Log.e("TAG", "Response: " + html);
					}
					getWebMain(LOCATION, "http://jwgl.gdut.edu.cn/", handler,
							3, USER_AGENT);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		httpThread.start();
	}

	/**
	 * 获取首页html
	 */
	public void getWebMain(final String loc, final String referer,
			final Handler handler, final int message, final String uag) {
		Thread httpThread = new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Log.d("TAG", "cookie: " + COOKIE);

					Log.d("TAG", "HOST+LOCATION: " + HOST + loc);

					HttpGet httpGet = new HttpGet(HOST + loc);
					HttpClient httpClient = new DefaultHttpClient();
					
					HttpHead head = new HttpHead();
					
					
					httpGet.addHeader("Cookie", "ASP.NET_SessionId=" + COOKIE);
					httpGet.addHeader("Referer", referer);
					httpGet.addHeader("Host", "jwgl.gdut.edu.cn");
					httpGet.addHeader("Accept-Encoding", "gzip,deflate,sdch");
					httpGet.addHeader("User-Agent", uag);
					httpGet.addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
					HttpResponse response = httpClient.execute(httpGet);
					Log.d("TAG", "response code: "
							+ response.getStatusLine().getStatusCode());

					StringBuilder html = new StringBuilder("");
					html.append(EntityUtils.toString(response.getEntity()));
					Log.d("Tag", html.toString());
					Message msg = new Message();
					msg.what = message;
					msg.obj = html.toString();
					handler.sendMessage(msg);

				} catch (Exception e) {
					e.printStackTrace();
				}

			}
		});
		httpThread.start();

	}

}
