package com.example.utils;

import java.util.Calendar;

public class DatePath {
	public static Calendar c = Calendar.getInstance();
	private static int year = c.get(Calendar.YEAR);
	private static int month = c.get(Calendar.MONTH) + 1;
	private static int day = c.get(Calendar.DATE);
	public static final String DATE = year + "-" + month + "-" + day;
	public static final String ORI_PATH = "/mnt/sdcard/test/";
	public static final String SD_PATH = "/mnt/sdcard/test/" + year + "-"
			+ month + "-" + day + "/";
	public static final String PLAY_PATH = "/mnt/sdcard/test/"
			+ FileOperation.GetFileDir("/mnt/sdcard/test/") + "/";
	public static final String DOWN_PATH = "/mnt/sdcard/test/"
			+ XmlParse.getDate() + "/";
}
