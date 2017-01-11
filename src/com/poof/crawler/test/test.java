package com.poof.crawler.test;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Random;

public class test {
	public static void main(String[] args) throws UnsupportedEncodingException, InterruptedException {
		String sb = "\"nav-cart\":{\"cartQty\":\"3\"}},\"requestid\":\"W1EH7KYNX3ZRXSQ8JSBJ\"}";

		sb = sb.substring(sb.indexOf("cartQty"));
		sb = sb.substring(0, sb.indexOf(","));
		sb = sb.replaceAll("[^\\d.]", "");
		System.err.println(sb);
		
		System.err.println(URLEncoder.encode("gDrpQB3hWhi9xhSoicblNe%2biuRNvtyOm46n5bbYAAAAJAAAAAFhzZdxyYXcAAAAA&requestID","UTF-8"));
		System.err.println(new Random().nextInt(10) % (20 - 5 + 1) + 5);
		
		String href = "Nite-Ize-SpotLit-Carabiner-Resistant/dp/B001E8BA9M/ref=zg_bs_2975317011_1?_encoding=UTF8&psc=1&refRID=6BVVQEKSWZG64TF7DGNX";
		System.err.println(href.substring(href.indexOf("/dp/")+4, href.indexOf("/dp/") + 14));
	}
}
