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
	 * �����˷��ͻ�ȡ��֤��������߳� ����ʹ����SESSIONID�Ż��������
	 */
	public int login() {
		InputStream is = null;
		OutputStream os = null;
		// ����һ��Post�����������������Ҫͨ��Post�������͵�����˵�����ͷ��������Ĳ��������øö�����
		HttpPost httpPost = new HttpPost("url");

		/* ���������������ʱʱ�� */
		BasicHttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 5 * 1000);
		// ����sessionid����������session�ռ���Ч,���η���������������
		if (sessionid != null) {
			httpPost.setHeader("cookie", "JSESSIONID=" + sessionid);
		}
		httpPost.setParams(httpParameters);
		// ����֤�룬�û�����������JSON��ʽ���͸������
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
		// ����һ���ͻ�����������
		HttpClient client = new DefaultHttpClient();
		// ����Get���󣬲��ȴ�����˵���Ӧ
		try {
			HttpResponse response = client.execute(httpPost);

			// ���SessionID�������棬Ϊ�ύ������׼��
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
			// ����������֧�ֱ�׼HttpЭ�飬���������쳣
			return -2;
		} catch (IOException e) {
			// ����ͨѶ�������ϣ����������쳣
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
