package com.example.alarms;

import java.util.Calendar;

import com.example.test.DownService;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class AlarmStart {
	private static AlarmManager am;
	private static PendingIntent pendingIntent;

	/**
	 * 使用 AlarmManager 来 定时启动服务
	 */
	public static void startPendingIntent(Context context) {
		am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Log.i("alarm", "start");
		Intent intent = new Intent(context, DownService.class);
		pendingIntent = PendingIntent.getService(context, 0, intent, 0);
		Calendar c = Calendar.getInstance();
		int year = c.get(Calendar.YEAR);
		int month = c.get(Calendar.MONTH);
		int day = c.get(Calendar.DATE);
		
		c.clear();
		c.set(year, month, day, 19, 00);
		Log.i("time", c.getTime().toString());
		
		long firstWake = System.currentTimeMillis();
		am.set(AlarmManager.RTC_WAKEUP, firstWake, pendingIntent);
	}

//	public static void stopPendingIntent() {
//
//		if (pendingIntent != null) {
//			pendingIntent.cancel();
//
//		}
//	};
}
