package com.example.download;

import java.util.ArrayList;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Dao {

	private static DBOpenHelper dbOpenHelper;

	public Dao(Context context) {
		getInstance(context);
	}

	public synchronized static DBOpenHelper getInstance (Context context) {
		if(dbOpenHelper == null) {
			dbOpenHelper = new DBOpenHelper(context);
		}
		return dbOpenHelper;
	}
	//判断数据库中是否有数据
	public boolean isHasInfors(String url) {
		SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
		String sql = "select count(*) from download_info where url=?";
		Cursor cursor = database.rawQuery(sql, new String[] {url});
		cursor.moveToFirst();
		int count = cursor.getInt(0);
		cursor.close();
		return count == 0;
	}

	//保存各线程下载信息
	public void saveInfos(List<DownloadInfo> infos) {
		SQLiteDatabase database = dbOpenHelper.getWritableDatabase();
		for (DownloadInfo info : infos) {
			String sql = "insert into download_info(thread_id,start_pos, end_pos,compelete_size,url) values (?,?,?,?,?)";
			Object[] args = { info.getThreadId(), info.getStartPos(),
					info.getEndPos(), info.getCompleteSize(), info.getUrl() };
			database.execSQL(sql, args);
		}
	}

	//获取各线程下载信息
	public List<DownloadInfo> getInfos(String urlstr) {
		List<DownloadInfo> list = new ArrayList<DownloadInfo>();
		SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
		String sql = "select thread_id, start_pos, end_pos,compelete_size,url from download_info where url=?";
		Cursor cursor = database.rawQuery(sql, new String[] { urlstr });
		while (cursor.moveToNext()) {
			DownloadInfo info = new DownloadInfo(cursor.getInt(0),
					cursor.getInt(1), cursor.getInt(2), cursor.getInt(3),
					cursor.getString(4));
			list.add(info);
		}
		cursor.close();
		return list;
	}

	//更新各线程下载信息
	public synchronized void updataInfos(int threadId, int compeleteSize, String url) {
		SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
		String sql = "update download_info set compelete_size=? where thread_id=? and url=?";
		Object[] args = { compeleteSize, threadId, url };
		database.execSQL(sql, args);
	}

	//下载完成后，删除各线程下载信息
	public void delete(String url) {
		SQLiteDatabase database = dbOpenHelper.getReadableDatabase();
		database.delete("download_info", "url=?", new String[] { url });
		database.close();
	}
	
	public void closeDb() {
		dbOpenHelper.close();
	}

}
