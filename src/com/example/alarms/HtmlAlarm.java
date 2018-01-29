package com.example.alarms;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class HtmlAlarm {
	private AlarmManager am;
	private PendingIntent pendingIntent;
	private Calendar calendar = Calendar.getInstance();
	private int year = calendar.get(Calendar.YEAR);
	private int month = calendar.get(Calendar.MONTH);
	private int day = calendar.get(Calendar.DATE);
	 
	public void playHtmlStartTime(Context context, int h, int m, String url){
		am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Log.i("alarm", "html start from " + h + ":" + m);
		Intent intent = new Intent("com.example.htmlstart");
		intent.putExtra("url", url);
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		calendar.clear();
		calendar.set(year, month, day, h, m);
		
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
	}
	
	public void playHtmlEndTime(Context context, int h, int m){
		am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Log.i("alarm", "html end from " + h + ":" + m);
		Intent intent = new Intent("com.example.htmlend");
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		
		calendar.clear();
		calendar.set(year, month, day, h, m);
		
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
	}

}
