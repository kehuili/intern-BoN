package com.example.test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.alarms.AlarmStart;
import com.example.utils.DatePath;
import com.example.utils.FileOperation;
import com.example.utils.XmlParse;
import com.example.views.AutoScrollView;
import com.joanzapata.pdfview.PDFView;
import com.joanzapata.pdfview.listener.OnPageChangeListener;

public class MainActivity extends Activity {
	private int pvFlag = 0;
	private int biFlag = 0;
	private LinearLayout l2 = null;
	private LinearLayout in = null;
	private LinearLayout in2 = null;
	private ImageView iv1 = null;
	private VideoView v1 = null;
	private ImageView iv2 = null;
	private VideoView v2 = null;
	private PDFView pv1 = null;
	private PDFView pv2 = null;
	private static final int PLAYPDF = 4; // 正在播放pdf
	private static final int PLAYHTML = 3; // 正在播放html
	private static final int PLAYIMAGE = 2; // 正在播放图片
	private static final int PLAYVIDEO = 1; // 正在播放视频
	private static final int PLAYNOTHING = 0; // 没有播放
	private static final int READYPLAY = -1; // 准备播放
	private static final String SD_PATH = DatePath.PLAY_PATH;

	private AutoScrollView av = null;
	private LinearLayout l = null;
	private int playFlag = 0;
	private int playNow = -1;
	private MediaController mediaController = null;
	private ImageView iv = null;
	private VideoView v = null;
	private WebView w = null;
	private TextView t = null;
	private PDFView pv = null;
	private int page = 0;
	private LinearLayout.LayoutParams p = null;
	private LinearLayout.LayoutParams params = new LayoutParams(
			LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

	private List<String> files = new ArrayList<String>();
	private Map<String, Bitmap> bit = new HashMap<String, Bitmap>();
	private MyBroadcastReceiver mbr = new MyBroadcastReceiver();
	private HtmlBroadcastReceiver m = new HtmlBroadcastReceiver();

	private Handler textHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				t.setText((String) msg.obj);
			}
		}
	};

	private Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 2) {
				loopPlay();
			} else if (msg.what == 1) {
				PDFView temp = null;
				// pdf换页
				switch (pvFlag) {
				case 0:
					temp = pv;
					break;
				case 1:
					temp = pv1;
					break;
				case 2:
					temp = pv2;
					break;
				}

				if (biFlag == 6) {
					if (page < temp.getPageCount() - 1) {
						temp.jumpTo(++page);
					} else {
						playFlag = PLAYNOTHING;
					}
				} else if (page < temp.getPageCount() - 1) {
					temp.jumpTo(++page);
				} else {
					if (biFlag == 0) {
						playFlag = PLAYNOTHING;
					} else {
						page = 0;
						temp.jumpTo(0);
					}
				}
			}
		}
	};

	public class PlayThread implements Runnable {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			while (!Thread.currentThread().isInterrupted()) {
				if (playFlag == PLAYHTML) {
					try {
						Thread.sleep(60 * 1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (playFlag == PLAYNOTHING) {// 没有文件播放，去播放
					if (files.size() == 0) {
						continue;
					}
					playFlag = READYPLAY;
					mHandler.sendEmptyMessage(2);
				} else if (playFlag == PLAYIMAGE) { // 图片播放10秒
					try {
						Thread.sleep(10 * 1000);
						playFlag = PLAYNOTHING;
						mHandler.sendEmptyMessage(0);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				} else if (playFlag == PLAYPDF) { // pdf播放10秒
					try {
						Thread.sleep(10 * 1000);
						mHandler.sendEmptyMessage(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		l = (LinearLayout) findViewById(R.id.linear);
		l2 = (LinearLayout) findViewById(R.id.linear2);
		av = (AutoScrollView) findViewById(R.id.autoscroll);
		t = (TextView) findViewById(R.id.scrolltext);
		pv = (PDFView) findViewById(R.id.pdfview);

		v = new VideoView(this);
		iv = new ImageView(this);
		w = new WebView(this);
		mediaController = new MediaController(this);

		in = (LinearLayout) findViewById(R.id.in);
		in2 = (LinearLayout) findViewById(R.id.in2);
		pv1 = (PDFView) findViewById(R.id.pdfview1);
		pv2 = (PDFView) findViewById(R.id.pdfview2);
		v1 = new VideoView(this);
		iv1 = new ImageView(this);
		v2 = new VideoView(this);
		iv2 = new ImageView(this);
		in.addView(v1, params);
		v1.setVisibility(View.GONE);
		in.addView(iv1, params);
		iv1.setVisibility(View.GONE);
		in2.addView(v2, params);
		v2.setVisibility(View.GONE);
		in2.addView(iv2, params);
		iv2.setVisibility(View.GONE);

		l.addView(v, params);
		v.setVisibility(View.GONE);
		l.addView(iv, params);
		v.setVisibility(View.GONE);

		// 网页播放开始&结束的广播
		XmlParse.getHtml(this);
		// 注册广播
		IntentFilter htmlStart = new IntentFilter("com.example.htmlstart");
		IntentFilter htmlEnd = new IntentFilter("com.example.htmlend");
		this.registerReceiver(m, htmlStart);
		this.registerReceiver(m, htmlEnd);

		AlarmStart.startPendingIntent(this);
		// 注册广播，下载完一个文件接收service中启动的线程发送过来的信息
		IntentFilter filter = new IntentFilter("com.example.downloadCompleted");
		this.registerReceiver(mbr, filter);
		filter = new IntentFilter("com.example.xmlCompleted");
		this.registerReceiver(mbr, filter);
		// 获取文件
		if (getFiles(SD_PATH) == -1) {
			Log.i("no files", "y");
		}
		// 开始播放
		new Thread(new PlayThread()).start();
		// 下载滚动字幕
		// new Thread(new TextThread()).start();

		av.setScrolled(true);

		Log.i("play path", SD_PATH);
	}

	// 获取下载的文件
	private int getFiles(String path) {
		files = FileOperation.GetFileName(path);
		if (files == null || files.size() == 0)
			return -1;
		String type = "";
		for (int i = 0; i < files.size(); i++) {
			type = files.get(i).substring(files.get(i).lastIndexOf(".") + 1);
			if ("jpg".equals(type) || "png".equals(type)) {
				Bitmap b = BitmapFactory.decodeFile(SD_PATH + files.get(i));
				bit.put(files.get(i), b);
			}
		}
		return 1;
	}

	// 循环播放
	private void loopPlay() {
		// 放到最后一个文件，从头开始
		if (playNow == files.size() - 1) {
			Log.i("file", "last");
			playNow = -1;
		}
		if (playFlag == READYPLAY) {
			if (files.get(playNow + 1).startsWith("0911")) {
				biPlayInit();
				showMedia(files.get(playNow + 1), files.get(playNow + 2));
				playNow += 2;
			} else {
				biPlayCompleted();
				showMedia(files.get(playNow + 1));
			}
		}
	}

	// 根据类型选择播放种类
	private void showMedia(String name) {
		Log.i("type", name);
		String fileType = name.substring(name.lastIndexOf(".") + 1);
		if ("jpg".equals(fileType) || "png".equals(fileType)) {
			playNow = files.indexOf(name);
			playFlag = PLAYIMAGE;
			showImage(name, iv);
		} else if ("mp4".equals(fileType) || "flv".equals(fileType)
				|| "3gp".equals(fileType) || "avi".equals(fileType)
				|| "wmv".equals(fileType) || "mkv".equals(fileType)
				|| "mov".equals(fileType)) {
			playNow = files.indexOf(name);
			playFlag = PLAYVIDEO;
			showVideo(name, v);
		} else if ("pdf".equals(fileType)) {
			playFlag = PLAYPDF;
			playNow = files.indexOf(name);
			showPDF(name, pv);
		} else {
			playFlag = PLAYNOTHING;
			playNow = files.indexOf(name);
		}
	}

	// 双屏播放
	private void showMedia(String name, String name2) {
		String fileType = name.substring(name.lastIndexOf(".") + 1);
		biFlag = 0;
		Log.i("a", fileType);
		if ("jpg".equals(fileType) || "png".equals(fileType)) {
			showImage(name, iv1);
			biFlag += PLAYIMAGE; // 图片计数2
		} else if ("mp4".equals(fileType) || "flv".equals(fileType)
				|| "3gp".equals(fileType) || "avi".equals(fileType)
				|| "wmv".equals(fileType) || "mkv".equals(fileType)
				|| "mov".equals(fileType)) {
			showVideo(name, v1);
			biFlag += PLAYVIDEO; // 视频计数1
		} else if ("pdf".equals(fileType)) {
			pvFlag = 1;
			showPDF(name, pv1);
			biFlag += PLAYPDF; // pdf计数4
		}

		String fileType2 = name2.substring(name2.lastIndexOf(".") + 1);
		Log.i("b", fileType2);
		if ("jpg".equals(fileType2) || "png".equals(fileType2)) {
			showImage(name2, iv2);
			biFlag += PLAYIMAGE;
		} else if ("mp4".equals(fileType2) || "flv".equals(fileType2)
				|| "3gp".equals(fileType2) || "avi".equals(fileType2)
				|| "wmv".equals(fileType2) || "mkv".equals(fileType2)
				|| "mov".equals(fileType2)) {
			showVideo(name2, v2);
			biFlag += PLAYVIDEO;
		} else if ("pdf".equals(fileType2)) {
			pvFlag = 2;
			showPDF(name2, pv2);
			biFlag += PLAYPDF;
		}
		if (biFlag == 4) { // 都为图片，10s后更换
			Log.i("bi", "two images");
			playFlag = PLAYIMAGE;
		} else if (biFlag == 5) { // pdf&video，pdf翻页，video结束时更换
			playFlag = PLAYPDF;
		} else if (biFlag == 6) { // pdf&image，pdf翻页，pdf到尾页时结束
			playFlag = PLAYPDF;
		}
	}

	// 打开pdf
	private void showPDF(String filename, final PDFView pv) {
		LinearLayout l = (LinearLayout) pv.getParent();
		if (l.getChildAt(2).getVisibility() == View.VISIBLE)
			l.getChildAt(2).setVisibility(View.GONE);
		File f = new File(SD_PATH + filename);
		pv.fromFile(f).defaultPage(0).onPageChange(new OnPageChangeListener() {

			@Override
			public void onPageChanged(int page, int pageCount) {
				// TODO Auto-generated method stub
				p = new LayoutParams((int) pv.getOptimalPageWidth(),
						LayoutParams.MATCH_PARENT);
				pv.setLayoutParams(p);
				// pv.zoomTo((float) 3);
			}

		}).load();
		page = 0;
		if (pv.getVisibility() == View.GONE)
			pv.setVisibility(View.VISIBLE);
	}

	// 打开图片
	private void showImage(String filename, ImageView iv) {
		LinearLayout l = (LinearLayout) iv.getParent();
		iv.setImageBitmap(bit.get(filename));
		if (l.getChildAt(0).getVisibility() == View.VISIBLE)
			l.getChildAt(0).setVisibility(View.GONE);
		if (iv.getVisibility() == View.GONE)
			iv.setVisibility(View.VISIBLE);
	}

	// 打开网页
	private void showHtml(String filename) {
		w.getSettings().setJavaScriptEnabled(true);
		w.loadUrl(filename);
		if (l2.getVisibility() == View.VISIBLE)
			l2.setVisibility(View.GONE);
		if (l.getVisibility() == View.GONE)
			l.setVisibility(View.VISIBLE);
		if (v.getVisibility() == View.VISIBLE)
			v.setVisibility(View.GONE);
		if (iv.getVisibility() == View.VISIBLE)
			iv.setVisibility(View.GONE);
		if (pv.getVisibility() == View.VISIBLE)
			pv.setVisibility(View.GONE);
		l.addView(w, params);
	}

	// 播放视频
	private void showVideo(String filename, final VideoView v) {
		v.setVideoPath(SD_PATH + filename);
		LinearLayout li = (LinearLayout) iv.getParent();
		if (li.getChildAt(2).getVisibility() == View.VISIBLE)
			li.getChildAt(2).setVisibility(View.GONE);
		if (li.getChildAt(0).getVisibility() == View.VISIBLE)
			li.getChildAt(0).setVisibility(View.GONE);
		v.setVisibility(View.VISIBLE);
		mediaController.setAnchorView(v);
		// 设置VideView与MediaController建立关联
		v.setMediaController(mediaController);
		v.requestFocus();
		v.start();

		// 播放结束后flag置为PLAYNOTHING
		v.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mp) {
				playFlag = PLAYNOTHING;
				v.setVisibility(View.GONE);
			}
		});

	}

	public class TextThread extends Thread {
		HttpURLConnection connection = null;

		@Override
		public void run() {
			try {
				URL url = new URL("");
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(50000);
				connection.setRequestMethod("GET");
				String text = "";

				while (true) {
					// 获取text
					InputStream is = connection.getInputStream();
					BufferedReader in = new BufferedReader(
							new InputStreamReader(is));
					StringBuffer buffer = new StringBuffer();
					while ((text = in.readLine()) != null) {
						buffer.append(text);
					}
					text = is.toString();
					Message message = Message.obtain();
					message.what = 1;
					message.obj = text;
					textHandler.sendMessage(message);

					Thread.sleep(60 * 1000);
				}
			} catch (Exception e) {
				new Thread(new TextThread()).start();
			} finally {
				connection.disconnect();
			}
		}
	}

	// 有文件下载完成，重新获取文件
	class MyBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals("com.example.downloadCompleted")) {
				Log.i("myBroadcastReceiver", "getfiles");
				getFiles(SD_PATH);
			} else if (intent.getAction().equals("com.example.xmldCompleted")) {
				Log.i("myBroadcastReceiver", "getxml");
				XmlParse.getHtml(context);
			}
		}
	}

	// 定时播放网页
	class HtmlBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			if (intent.getAction().equals("com.example.htmlstart")) {
				Log.i("htmlBroadcastReceiver", "start");
				// 网页地址，更换
				showHtml(intent.getStringExtra("url"));
				playFlag = PLAYHTML;

			} else if (intent.getAction().equals("com.example.htmlend")) {
				Log.i("htmlBroadcastReceiver", "end");
				playFlag = PLAYNOTHING;
				l.removeView(w);
			}
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		unregisterReceiver(m);
		unregisterReceiver(mbr);

		for (int i = 0; i < files.size(); i++) {
			if (bit.containsKey(files.get(i))) {
				if (!bit.get(files.get(i)).isRecycled()) {
					bit.get(files.get(i)).recycle();
				}
			}
		}
		android.os.Process.killProcess(android.os.Process.myPid());
		super.onStop();
	}

	private void biPlayCompleted() {
		biFlag = 0;
		if (l2.getVisibility() == View.VISIBLE)
			l2.setVisibility(View.GONE);
		if (l.getVisibility() == View.GONE)
			l.setVisibility(View.VISIBLE);
	}

	private void biPlayInit() {
		biFlag = 1;
		if (l.getVisibility() == View.VISIBLE)
			l.setVisibility(View.GONE);
		if (l2.getVisibility() == View.GONE)
			l2.setVisibility(View.VISIBLE);
	}
}