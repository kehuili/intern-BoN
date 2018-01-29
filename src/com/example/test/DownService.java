package com.example.test;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.widget.Toast;

import com.example.download.DownloadManager;
import com.example.download.LoadInfo;
import com.example.utils.DatePath;
import com.example.utils.Login;
import com.example.utils.XmlParse;

public class DownService extends Service {
	private static String SD_PATH = "/mnt/sdcard/test/2015-8-12";
	// URL list
	private List<String> urlList = new ArrayList<String>();
	private List<String> typeList = new ArrayList<String>();
	private List<String> nameList = new ArrayList<String>();
	private List<Map<String, String>> itemlist = new ArrayList<Map<String, String>>();

	private Login li = new Login();
	// 监听网络状况
	private NetworkBroadcastReceiver nbr = new NetworkBroadcastReceiver();

	// 各下载器
	private Map<String, DownloadManager> downloaders = new HashMap<String, DownloadManager>();
	// 各下载完成度
	private Map<String, Integer> completeSize = new HashMap<String, Integer>();
	// 各文件大小
	private Map<String, Integer> fileSize = new HashMap<String, Integer>();
	private int fileNum = 0; // 已下载文件数

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
				.detectDiskReads().detectDiskWrites().detectNetwork()
				.penaltyLog().build());
		StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
				.detectLeakedSqlLiteObjects().detectLeakedClosableObjects()
				.penaltyLog().penaltyDeath().build());
		// 注册广播接收器
		IntentFilter filter = new IntentFilter();
		filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
		this.registerReceiver(nbr, filter);

		super.onCreate();
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		// 注销reveiver
		Log.i("service destry", "y");
		unregisterReceiver(nbr);
		// 暂停下载
		for (int i = 0; i < urlList.size(); i++) {
			if (downloaders.containsKey(urlList.get(i))) {
				downloaders.get(urlList.get(i)).pause();
			}
		}
		super.onDestroy();
	}

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				String url = (String) msg.obj;
				int length = msg.arg1;
				int temp = completeSize.get(url);
				temp += length;
				completeSize.put(url, temp);
				if (msg.arg2 == 1) {
					Log.i(url.substring(url.lastIndexOf("/") + 1),
							completeSize.get(url) / (fileSize.get(url) / 100)
									+ "%");
				}
				// 下载完成
				if (completeSize.get(url).equals(fileSize.get(url))) {
					File f = new File(SD_PATH
							+ url.substring(url.lastIndexOf("/")) + ".tmp");
					File nf = new File(SD_PATH
							+ url.substring(url.lastIndexOf("/")));
					f.renameTo(nf);
					// 下载完成后将map中的数据清空
					if(downloaders.containsKey("url")) {
					downloaders.get(url).closeDB();
					downloaders.get(url).delete(url);
					downloaders.get(url).reset();
					downloaders.remove(url);
					Log.i("downloading has done", url);
					Intent intent = new Intent("com.example.downloadCompleted");
					sendBroadcast(intent);
					} else {
						Intent intent = new Intent("com.example.xmlCompleted");
						sendBroadcast(intent);
					}

					if (downloaders.isEmpty()) {
						stopSelf();
					}
				}
			} else if (msg.what == -2) {// 网络问题
				Toast.makeText(getApplicationContext(),
						"Please check network!", Toast.LENGTH_SHORT).show();
				for (int i = 0; i < urlList.size(); i++) {
					if (downloaders.containsKey(urlList.get(i))) {
						downloaders.get(urlList.get(i)).pause();
					}
				}
			}
		}
	};

	public int download(int i) {
		// 已下载完成，返回1
		File file = new File(SD_PATH
				+ urlList.get(i).substring(urlList.get(i).lastIndexOf("/")));
		if (file.exists()) {
			Log.i("complete", file.getAbsolutePath());
			fileNum++;
			return 1;
		}
		int threadCount = 5;
		DownloadManager dm = downloaders.get(urlList.get(i));
		if (dm == null) {
			dm = new DownloadManager(urlList.get(i), SD_PATH
					+ urlList.get(i).substring(urlList.get(i).lastIndexOf("/"))
					+ ".tmp", threadCount, this, mHandler);
			downloaders.put(urlList.get(i), dm);
		}
		// 正在下载，返回2
		if (dm.isdownloading())
			return 2;
		// 下载信息类总和
		LoadInfo loadInfo = dm.getDownloaderInfors();
		// 出错，返回-1
		if (loadInfo == null) {
			Toast.makeText(getApplicationContext(), "Please check network!",
					Toast.LENGTH_LONG).show();
			Log.i("connected?", "X");
			File f = new File(SD_PATH
					+ urlList.get(i).substring(urlList.get(i).lastIndexOf("/")));
			f.delete();
			return -1;
		}
		fileSize.put(urlList.get(i), loadInfo.getFileSize());
		completeSize.put(urlList.get(i), loadInfo.getComplete());

		// 调用方法开始下载，返回3
		dm.download();
		return 3;
	}

	class NetworkBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub

			ConnectivityManager cm = (ConnectivityManager) context
					.getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo wifi = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			NetworkInfo gprs = cm
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

			// 未连接，暂停下载
			if (!wifi.isConnected() && !gprs.isConnected()) {
				Toast.makeText(context, "Not connected!", Toast.LENGTH_SHORT)
						.show();
				Log.i("connected?", "NO!!!");
				for (int i = 0; i < urlList.size(); i++) {
					if (downloaders.containsKey(urlList.get(i))) {
						downloaders.get(urlList.get(i)).pause();
					}
				}

			} else {// 有网络连接，继续下载
				Log.i("connected?", "Yeah");
				// 先登录，获取xml
				li.login();
//				DownloadManager dm = new DownloadManager(xmlURL, DatePath.ORI_PATH
//						+ ".tmp", 3, this, myHandler);
//				dm.getDownloaderInfors();
//				dm.download();
				
				SD_PATH = DatePath.DOWN_PATH;
				itemlist = XmlParse.getElem();
				for (int i = 0; i < itemlist.size(); i++) {
					urlList.add(itemlist.get(i).get("url"));
					typeList.add(itemlist.get(i).get("type"));
					nameList.add(itemlist.get(i).get("name"));
				}

				File f = new File(SD_PATH);
				if (!f.exists()) {
					f.mkdirs();
				}

				fileNum = 0;
				for (int i = 0; i < urlList.size(); i++) {
					if (download(i) == -1)
						break;
				}
				if (fileNum == urlList.size()) {
					stopSelf();
				}
			}
		}
	}

}
