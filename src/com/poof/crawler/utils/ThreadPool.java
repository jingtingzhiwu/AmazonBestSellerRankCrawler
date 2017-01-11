package com.poof.crawler.utils;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wilkey 
 * @mail admin@wilkey.vip
 * @Date 2017年1月10日 下午4:25:58
 */
public class ThreadPool {
	private static volatile ThreadPoolExecutor executor = null;

	private ThreadPool() {
	}

	public static ThreadPoolExecutor getInstance(int corePoolSize, int maximumPoolSize, long aliveTime, TimeUnit unit) {
		if (executor == null) {
			PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<Runnable>();
			executor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, aliveTime, unit, queue);
		}

		return executor;
	}

	public static ThreadPoolExecutor getInstance() {
		if (executor == null) {
			PriorityBlockingQueue<Runnable> queue = new PriorityBlockingQueue<Runnable>();
			executor = new ThreadPoolExecutor(5, 5, 10, TimeUnit.SECONDS, queue);
		}

		return executor;
	}
}