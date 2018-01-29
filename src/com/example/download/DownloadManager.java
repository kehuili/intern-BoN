package com.example.download;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.conn.ConnectTimeoutException;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class DownloadManager {
	private String urlstr = null;// ���صĵ�ַ
	private String localfile = null;// ����·��
	private int threadcount = 0;// �߳���
	private Handler mHandler = null;// ��Ϣ������
	private Dao dao = null;// ������
	private int fileSize = 0;// ��Ҫ���ص��ļ��Ĵ�С
	private List<DownloadInfo> infos = null;// ���������Ϣ��ļ���
	private static final int INIT = 1;// �����������ص�״̬����ʼ��״̬����������״̬����ͣ״̬
	private static final int DOWNLOADING = 2;
	private static final int PAUSE = 3;
	private int state = INIT;

	public DownloadManager(String urlstr, String localfile, int threadcount,
			Context context, Handler mHandler) {
		this.urlstr = urlstr;
		this.localfile = localfile;
		this.threadcount = threadcount;
		this.mHandler = mHandler;
		dao = new Dao(context);
	}

	/**
	 * �ж��Ƿ���������
	 */
	public boolean isdownloading() {
		return state == DOWNLOADING;
	}

	/**
	 * �ж��Ƿ��һ������
	 */
	public LoadInfo getDownloaderInfors() {
		if (isFirst(urlstr)) {
			Log.i("TAG", "isFirst");
			if (init() == -1) {
				return null;
			}

			int range = fileSize / threadcount;
			infos = new ArrayList<DownloadInfo>();
			for (int i = 0; i < threadcount - 1; i++) {
				DownloadInfo info = new DownloadInfo(i, i * range, (i + 1)
						* range - 1, 0, urlstr);
				infos.add(info);
			}
			DownloadInfo info = new DownloadInfo(threadcount - 1,
					(threadcount - 1) * range, fileSize - 1, 0, urlstr);
			infos.add(info);
			// ����infos�е����ݵ����ݿ�
			dao.saveInfos(infos);
			// ����һ��LoadInfo��������������ľ�����Ϣ
			LoadInfo loadInfo = new LoadInfo(fileSize, 0, urlstr);
			return loadInfo;
		} else {
			// �õ����ݿ������е�urlstr���������ľ�����Ϣ
			infos = dao.getInfos(urlstr);
			Log.i("TAG", "not isFirst size=" + infos.size());
			int size = 0;
			int completeSize = 0;
			for (DownloadInfo info : infos) {
				completeSize += info.getCompleteSize();
				size += info.getEndPos() - info.getStartPos() + 1;
			}
			return new LoadInfo(size, completeSize, urlstr);
		}
	}

	/**
	 * ��ʼ��
	 */
	private int init() {
		try {
			URL url = new URL(urlstr);
			HttpURLConnection connection = (HttpURLConnection) url
					.openConnection();
			connection.setConnectTimeout(3000);
			connection.setReadTimeout(3000);
			connection.setRequestMethod("GET");
			fileSize = connection.getContentLength();

			Log.i("code", connection.getResponseCode() + "");
			// ���ش���
			if (fileSize <= 0 || connection.getResponseCode() != 200)
				return -1;

			File file = new File(localfile);
			if (!file.exists()) {
				file.createNewFile();
			}
			// ���ط����ļ�
			RandomAccessFile accessFile = new RandomAccessFile(file, "rw");
			accessFile.setLength(fileSize);
			accessFile.close();
			connection.disconnect();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return -1;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 1;
	}

	/**
	 * �ж��Ƿ��ǵ�һ�� ����
	 */
	private boolean isFirst(String urlstr) {
		return dao.isHasInfors(urlstr);
	}

	/**
	 * �����߳̿�ʼ��������
	 */
	public void download() {
		if (infos != null) {
			if (state == DOWNLOADING)
				return;
			state = DOWNLOADING;
			for (DownloadInfo info : infos) {
				new MyThread(info.getThreadId(), info.getStartPos(),
						info.getEndPos(), info.getCompleteSize(), info.getUrl())
						.start();
			}
		}
	}

	public class MyThread extends Thread {
		private int threadId;
		private int startPos;
		private int endPos;
		private int completeSize;
		private String urlstr;

		public MyThread(int threadId, int startPos, int endPos,
				int completeSize, String urlstr) {
			this.threadId = threadId;
			this.startPos = startPos;
			this.endPos = endPos;
			this.completeSize = completeSize;
			this.urlstr = urlstr;
		}

		@Override
		public void run() {
			HttpURLConnection connection = null;
			RandomAccessFile randomAccessFile = null;
			InputStream is = null;
			int times = 0;
			try {
				URL url = new URL(urlstr);
				connection = (HttpURLConnection) url.openConnection();
				connection.setConnectTimeout(5000);
				connection.setReadTimeout(50000);
				connection.setRequestMethod("GET");
				// �߳�������ɣ�����
				if (startPos + completeSize == endPos + 1)
					return;
				// ���÷�Χ����ʽΪRange��bytes x-y;
				connection.setRequestProperty("Range", "bytes="
						+ (startPos + completeSize) + "-" + endPos);

				randomAccessFile = new RandomAccessFile(localfile, "rw");
				randomAccessFile.seek(startPos + completeSize);

				// ��Ҫ���ص��ļ�д�������ڱ���·���µ��ļ���
				is = connection.getInputStream();
				byte[] buffer = new byte[4096];
				int length = -1;
				long firstTime = System.currentTimeMillis();
				long nowTime = firstTime;
				while ((length = is.read(buffer)) != -1) {
					randomAccessFile.write(buffer, 0, length);
					completeSize += length;
					nowTime = System.currentTimeMillis();

					Message message = Message.obtain();
					message.what = 1;
					message.obj = urlstr;
					message.arg1 = length;
					message.arg2 = 0;
					if (nowTime - firstTime >= 2000) {
						// �������ݿ��е�������Ϣ
						dao.updataInfos(threadId, completeSize, urlstr);
						firstTime = nowTime;
						message.arg2 = 1;
					}

					mHandler.sendMessage(message);
					if (state == PAUSE) {
						Log.i(urlstr, "pause");
						break;
					}
				}
				Log.i(urlstr, "terminate");
			} catch (SocketTimeoutException e) {
				Log.i(urlstr, "SocketTimeoutException");
				new MyThread(threadId, startPos, endPos, completeSize, urlstr)
						.start();
				e.printStackTrace();
			} catch (SocketException e) {
				Log.i(urlstr, "SocketException");
				new MyThread(threadId, startPos, endPos, completeSize, urlstr)
						.start();
				e.printStackTrace();
			} catch (ConnectTimeoutException e) {
				Log.i(urlstr, "ConnectTimeoutException");
				new MyThread(threadId, startPos, endPos, completeSize, urlstr)
						.start();
				e.printStackTrace();
			} catch (FileNotFoundException e) {
				Log.i(urlstr, "FileNotFoundException");
				new MyThread(threadId, startPos, endPos, completeSize, urlstr)
						.start();
				e.printStackTrace();
			} catch (ProtocolException e) {
				Log.i(urlstr, "ProtocolException");
				new MyThread(threadId, startPos, endPos, completeSize, urlstr)
						.start();
				e.printStackTrace();
			} catch (UnknownHostException e) {
				Log.i(urlstr, "UnknownHostException" + times);
				if (++times < 3) {
					new MyThread(threadId, startPos, endPos, completeSize,
							urlstr).start();
				} else {
					Log.d(urlstr, "download failed");
					mHandler.sendEmptyMessage(-2);
				}
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (is != null)
						is.close();
					if (randomAccessFile != null)
						randomAccessFile.close();
					connection.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}

		}
	}

	// �ر����ݿ�
	public void closeDB() {
		dao.closeDb();
	}

	// ɾ�����ݿ���urlstr��Ӧ����������Ϣ
	public void delete(String urlstr) {
		dao.delete(urlstr);
	}

	// ������ͣ
	public void pause() {
		state = PAUSE;
	}

	// ��������״̬
	public void reset() {
		state = INIT;
	}
}