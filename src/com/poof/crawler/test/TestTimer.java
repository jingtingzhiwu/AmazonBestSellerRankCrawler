package com.poof.crawler.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;

import com.poof.crawler.utils.ThreadPool;

public class TestTimer {
	static int count = 0;

	static class InnerTask implements Runnable {
		private String name;

		public InnerTask(String name) {
			this.name = name;
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			final ExecutorService pool = Executors.newFixedThreadPool(2);
			final ExecutorCompletionService<String> completionService = new ExecutorCompletionService<String>(pool);

			List<String> items = new ArrayList<String>();

			Integer[] counts = new Integer[] { 1, 2, 3, 4, 5 };

			long startTime = System.currentTimeMillis();
			for (final Integer i : counts) {
				completionService.submit(new Callable() {
					@Override
					public Object call() throws Exception {
						items.add("value" + i);
						System.err.println(name + "job " + i + " ok...");
						TimeUnit.SECONDS.sleep(new Random().nextInt(10));
						return null;
					}
				});
			}
			for (int i = 1; i <= 5; i++) {
				try {
					completionService.take();
					System.err.println(name + i + " taked...");
					synchronized (this) {
						System.err.println(name + i + " get value: " + StringUtils.join(items, ","));
						items.clear();
					}
					System.err.println(name + i + " cleared...");
				} catch (Exception e) {

				}
			}
			pool.shutdown();
			long endTime = System.currentTimeMillis();
			System.err.println("耗时" + (endTime - startTime) / 1000 + "秒");

		}
	}

	public static void showTime() {

		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				InnerTask t1 = new InnerTask("Thread 1: ");
				InnerTask t2 = new InnerTask("Thread 2: ");
				ThreadPool.getInstance().execute(t1);
				ThreadPool.getInstance().execute(t2);
			}
		};
		task.run();
	}

	public static void main(String[] args) {
		showTime();
	}
}