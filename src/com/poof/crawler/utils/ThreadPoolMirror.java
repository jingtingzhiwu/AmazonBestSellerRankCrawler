package com.poof.crawler.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

/**
 * 线程工具类
 * @author wilkey 
 * @mail admin@wilkey.vip
 * @Date 2017年1月10日 下午4:25:46
 */
public abstract class ThreadPoolMirror {

	private static Logger log = Logger.getLogger(ThreadPoolMirror.class);

	/**
	 * 关闭线程池
	 * 
	 * @param threadPool
	 *            需要关闭的线程池
	 * @param shutdownNow
	 *            true-立即关闭放弃当前执行的任务 false-等待所提交的任务都完成后再最初
	 */
	public static void shundownThreadPool(ExecutorService threadPool, boolean shutdownNow) {
		if (shutdownNow) {
			try {
				threadPool.shutdownNow();
			} catch (Exception e) {
				if (!(e instanceof InterruptedException)) {
					log.error("关闭线程池时出错!", e);
				}
			}
		} else {
			threadPool.shutdown();
			boolean taskComplete = false;
			for (int i = 0; i < 30; i++) {// 最多等待30秒

				log.error("正在第 [" + i + 1 + "] 次尝试关闭线程池!");

				try {
					taskComplete = threadPool.awaitTermination(1, TimeUnit.SECONDS);
				} catch (InterruptedException e) {
					if (!taskComplete) {
						continue;
					}
				}

				if (taskComplete) {
					break;
				} else {
					if (threadPool instanceof ThreadPoolExecutor) {
						Queue<?> taskQueue = getTaskQueue((ThreadPoolExecutor) threadPool);
						if (taskQueue != null) {
							log.error("当前正在关闭的线程池尚有 [" + taskQueue.size() + "] 个任务排队等待处理!");
						}
					}

				}
			}

			if (!taskComplete) {
				log.error("线程池非正常退出!");
			} else {
				log.error("线程池正常退出!");
			}
		}
	}

	/**
	 * 获取线程池的任务队列
	 * 
	 * @param threadPoolExecutor
	 * @return
	 */
	private static BlockingQueue<?> getTaskQueue(ThreadPoolExecutor threadPoolExecutor) {
		BlockingQueue<?> queue = null;
		try {
			queue = threadPoolExecutor.getQueue();
		} catch (Exception e1) {
			try {
				Field field = ThreadPoolExecutor.class.getDeclaredField("workQueue");
				field.setAccessible(true);
				queue = (BlockingQueue<?>) field.get(threadPoolExecutor);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return queue;
	}

	/**
	 * 获取线程池的任务队列
	 * 
	 * @param threadPoolExecutor
	 * @return
	 */
	private static BlockingQueue<?> getTaskQueue() {
		BlockingQueue<?> queue = null;
		try {
			queue = ThreadPool.getInstance().getQueue();
		} catch (Exception e1) {
			try {
				Field field = ThreadPoolExecutor.class.getDeclaredField("workQueue");
				field.setAccessible(true);
				queue = (BlockingQueue<?>) field.get(ThreadPool.getInstance());
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
		return queue;
	}

	/**
	 * dump出线程池情况
	 * 
	 * @param poolname
	 * @param threadPool
	 * @return
	 */
	public static String dumpThreadPool(String poolname, ExecutorService threadPool) {

		if (threadPool instanceof ThreadPoolExecutor) {
			ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) threadPool;
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("线程池名称", poolname);

			map.put("当前队列上排队的任务数量", "(无法获取)");
			BlockingQueue<?> queue = getTaskQueue(threadPoolExecutor);
			if (queue != null) {
				map.put("当前队列上排队的任务数量", queue.size());
			}

			map.put("当前池内总的线程数量", threadPoolExecutor.getPoolSize());
			map.put("当前正在执行任务的线程数", threadPoolExecutor.getActiveCount());
			map.put("历史执行过的任务数量", threadPoolExecutor.getCompletedTaskCount());
			map.put("配置的核心大小", threadPoolExecutor.getCorePoolSize());
			map.put("配置的最大线程数量", threadPoolExecutor.getMaximumPoolSize());
			map.put("历史最大峰值线程数量", threadPoolExecutor.getLargestPoolSize());
			return transMapToString(map);
		} else if (threadPool instanceof ThreadPoolExecutor) {
			ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) threadPool;
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("线程池名称", poolname);

			map.put("当前队列上排队的任务数量", "(无法获取)");
			BlockingQueue<?> queue = getTaskQueue(threadPoolExecutor);
			if (queue != null) {
				map.put("当前队列上排队的任务数量", queue.size());
			}

			map.put("当前池内总的线程数量", threadPoolExecutor.getPoolSize());
			map.put("当前正在执行任务的线程数", threadPoolExecutor.getActiveCount());
			map.put("历史执行过的任务数量", threadPoolExecutor.getCompletedTaskCount());
			map.put("配置的核心大小", threadPoolExecutor.getCorePoolSize());
			map.put("配置的最大线程数量", threadPoolExecutor.getMaximumPoolSize());
			map.put("历史最大峰值线程数量", threadPoolExecutor.getLargestPoolSize());
			return transMapToString(map);
		}

		return "无法内省的线程池 [" + poolname + "]";

	}

	public static String transMapToString(Map map) {
		java.util.Map.Entry entry;
		StringBuffer sb = new StringBuffer();
		for (Iterator iterator = map.entrySet().iterator(); iterator.hasNext();) {
			entry = (java.util.Map.Entry) iterator.next();
			sb.append(entry.getKey().toString()).append("'").append(null == entry.getValue() ? "" : entry.getValue().toString()).append(iterator.hasNext() ? "^" : "");
		}
		return sb.toString();
	}
}