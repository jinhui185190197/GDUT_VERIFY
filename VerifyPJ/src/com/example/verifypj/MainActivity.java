package com.example.verifypj;

import java.net.URLEncoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.exmaple.network.HttpManager;

import android.app.Activity;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class MainActivity extends Activity {

	ImageView verifyIv;
	MyHandle msgHandler;
	HttpManager manager;
	EditText accountEt;
	EditText pwdEt;
	EditText codeEt;
	Button submitBtn;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		msgHandler = new MyHandle();

		verifyIv = (ImageView) findViewById(R.id.verify_iv);
		accountEt = (EditText) findViewById(R.id.account);
		pwdEt = (EditText) findViewById(R.id.pwd);
		codeEt = (EditText) findViewById(R.id.code);
		submitBtn = (Button) findViewById(R.id.submit);

		accountEt.setText("3112006276");
		pwdEt.setText("jinhui3112006276");

		submitBtn.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				manager.doLoginAction(accountEt.getText().toString(), pwdEt
						.getText().toString(), codeEt.getText().toString(),
						msgHandler);
			}
		});

		manager = new HttpManager();
		manager.getWebCookies(msgHandler);

	}

	public class MyHandle extends Handler {
		@Override
		public void handleMessage(Message msg) {
			// super.handleMessage(msg);
			switch (msg.what) {
			case 1:
				manager.getVerifyImg(msgHandler);
				break;
			case 2:
				verifyIv.setImageBitmap((Bitmap) msg.obj);
				break;
			case 3:
				String html = (String) msg.obj;
				// Log.w("TAG", html);
				// 获取个人信息的跳转地址
				// 创建正则表达式
				Pattern pattern = Pattern
						.compile("xsgrxx.aspx?.*gnmkdm=N121501");
				Matcher matcher = pattern.matcher(html);
				if (matcher.find()) {
					//%E9%99%88%E9%94%A6%E8%BE%89
					manager.getWebMain(
							"/xsgrxx.aspx?xh=3112006276&xm=%B3%C2%BD%F5%BB%D4&gnmkdm=N121501",
							"http://jwgl.gdut.edu.cn/xs_main.aspx?xh=3112006276",
							msgHandler, 4, HttpManager.USER_AGENT__);
				}

				break;
			case 4:
				String personInfo = (String) msg.obj;
				// Log.w("TAG", personInfo);
				break;
			default:
				break;
			}

		}
	}

}
