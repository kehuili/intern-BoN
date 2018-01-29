package com.example.utils;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class PlayListByDate {
	private AlarmManager am;
	private PendingIntent pendingIntent;
	private Calendar calendar = Calendar.getInstance();
	private int year = calendar.get(Calendar.YEAR);
	private int month = calendar.get(Calendar.MONTH);
	private int day = calendar.get(Calendar.DATE);
	
	public void playHtmlStartTime(Context context){
		am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Log.i("alarm", "html start");
		Intent intent = new Intent("com.example.htmlstart");
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		calendar.clear();
		calendar.set(year, month, day, 15, 9);
		
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
	}
	
	public void playHtmlEndTime(Context context){
		am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		Log.i("alarm", "html end");
		Intent intent = new Intent("com.example.htmlend");
		pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		
		calendar.clear();
		calendar.set(year, month, day, 15, 10);
		
		am.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
	}

}
