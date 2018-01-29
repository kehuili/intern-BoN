package com.example.utils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FileOperation {
	// ��ȡ��ǰĿ¼�������ļ�
	public static List<String> GetFileName(String fileAbsolutePath) {
		List<String> listFile = new ArrayList<String>();
		File file = new File(fileAbsolutePath);
		File[] subFile = file.listFiles();
		if (subFile == null)
			return listFile;
		for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
			// �ж��Ƿ�Ϊ�ļ���
			if (!subFile[iFileLength].isDirectory()) {
				String filename = subFile[iFileLength].getName();
				listFile.add(filename);
			}
		}
		Collections.sort(listFile);
		return listFile;
	}

	// ��ȡ���첥�ŵ��ļ���
	public static String GetFileDir(String fileAbsolutePath) {
		String dir = null;
		File file = new File(fileAbsolutePath);
		File[] subFile = file.listFiles();
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		Date todayDate = null;
		int after = -1; // ���������ļ�������֮��
		int equal = -1; // �������ڵ����ļ�����
		try {
			todayDate = sdf.parse(DatePath.DATE);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (subFile.length == 1) {
			File f = new File(DatePath.SD_PATH);
			dir = f.getName();
			return dir;
		}
		if (subFile.length == 2)
			if ("test.xml".equals(subFile[0].getName())){
				return subFile[1].getName();
			} else {
				return subFile[0].getName();
			}
		for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
			// �ж��Ƿ�Ϊ�ļ���
			if (subFile[iFileLength].isDirectory()) {
				Date fileDate = null;
				try {
					fileDate = sdf.parse(subFile[iFileLength].getName());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				if (todayDate.after(fileDate)) {
					after = iFileLength;
				} else if (todayDate.equals(fileDate)) {
					equal = iFileLength;
				} else if (fileDate.before(todayDate)) {
				}
			}
		}
		if (after == -1)
			dir = subFile[equal].getName();
		else if (equal == -1)
			dir = subFile[after].getName();
		else {
			dir = subFile[equal].getName();
			deleteDir(new File(fileAbsolutePath + subFile[after].getName()));
		}
		return dir;
	}

//	// ������Ŀ¼ʱ��ɾ����ǰ���ļ�
//	public static void deleteFormer(String fileAbsolutePath) {
//		File file = new File(fileAbsolutePath);
//		File[] subFile = file.listFiles();
////		if (subFile == null)
////			return;
//		for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
//			// �ж��Ƿ�Ϊ�ļ���
//			if (subFile[iFileLength].isDirectory()) {
//				deleteDir(subFile[iFileLength]);
//			}
//		}
//	}

	private static boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}
		return dir.delete();
	}
}
