package com.poof.crawler.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * @author wilkey 
 * @mail admin@wilkey.vip
 * @Date 2017年1月10日 下午4:25:55
 */
public class FileUtils extends org.apache.commons.io.FileUtils {
	public static String geturl() {
		InputStream is = FileUtils.class.getResourceAsStream("/url");
		BufferedReader reader = null;
		String result = "";
		try {
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				result += tempString + "\n\r";
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return result;
	}

	public static String getproxy() {
		InputStream is = FileUtils.class.getResourceAsStream("/proxy");
		BufferedReader reader = null;
		String result = "";
		try {
			reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			String tempString = null;
			while ((tempString = reader.readLine()) != null) {
				result += tempString + "\n\r";
			}
			reader.close();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e1) {
				}
			}
		}
		return result;
	}
	public static void writeToFile(String fileName, String content) {
		try {
			FileUtils.writeStringToFile(new File(fileName), content, "utf-8");
		} catch (IOException e) {
		}
	}

	public static void main(String[] args) {
		String[] urls = FileUtils.geturl().split("\n\r");
		for (String url : urls) {
			System.err.println(url);
		}
	}
}
