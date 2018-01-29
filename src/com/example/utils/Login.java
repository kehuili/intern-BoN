package com.example.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;
import android.widget.EditText;

public class Login {
	private String sessionid = null;
	private EditText txtUser = null;
	private EditText txtPwd = null;

	/*
	 * 向服务端发送获取验证码请求的线程 这里使用了SESSIONID优化处理机制
	 */
	public int login() {
		InputStream is = null;
		OutputStream os = null;
		// 创建一个Post请求参数对象，所有需要通过Post方法发送到服务端的请求头和请求体的参数都设置该对象中
		HttpPost httpPost = new HttpPost("url");

		/* 设置请求参数：超时时间 */
		BasicHttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 5 * 1000);
		// 设置sessionid，保存服务端session空间有效,初次发送请求无需设置
		if (sessionid != null) {
			httpPost.setHeader("cookie", "JSESSIONID=" + sessionid);
		}
		httpPost.setParams(httpParameters);
		// 将验证码，用户名和密码以JSON格式发送给服务端
		try {
			JSONObject params = new JSONObject();
			params.put("uid", txtUser.getText().toString());
			params.put("pwd", txtPwd.getText().toString());
			List<NameValuePair> nvps = new ArrayList<NameValuePair>();
			BasicNameValuePair se = new BasicNameValuePair("params",
					params.toString());
			nvps.add(se);
			httpPost.setEntity(new UrlEncodedFormEntity(nvps));
		} catch (UnsupportedEncodingException e1) {
			return -3;
		} catch (JSONException e1) {
			return -4;
		}
		// 创建一个客户端请求发送器
		HttpClient client = new DefaultHttpClient();
		// 发送Get请求，并等待服务端的响应
		try {
			HttpResponse response = client.execute(httpPost);

			// 获得SessionID，并保存，为提交数据做准备
			Header[] header = response.getHeaders("Set-Cookie");
			if (header.length > 0) {
				String temp = header[0].getValue().toString();
				sessionid = temp.substring(temp.indexOf("JSESSIONID=") + 11,
						temp.indexOf(";"));
				Log.i("sessionid", sessionid);
			}

			File file = new File("/mnt/sdcard/test/test.xml");
			if (!file.exists()) {
				file.createNewFile();
			}
			is = response.getEntity().getContent();
			os = new FileOutputStream(file);
			int bytesRead = 0;
			byte[] buffer = new byte[8192];
			while ((bytesRead = is.read(buffer, 0, 8192)) != -1) {
				os.write(buffer, 0, bytesRead);
			}

		} catch (ClientProtocolException e) {
			// 服务端如果不支持标准Http协议，则出现这个异常
			return -2;
		} catch (IOException e) {
			// 网络通讯发生故障，则出现这个异常
			return -1;
		} finally {
			try {
				if (is != null) {
					is.close();
				}
				if (os != null) {
					os.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return 0;

	}

}
