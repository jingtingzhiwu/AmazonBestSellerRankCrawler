package com.poof.crawler.bestsellerank;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.net.URLEncoder;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.LogFactory;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.log4j.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import com.gargoylesoftware.htmlunit.JavaScriptPage;
import com.gargoylesoftware.htmlunit.ProxyConfig;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.DomElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.poof.crawler.db.DBUtil;
import com.poof.crawler.utils.FileUtils;
import com.poof.crawler.utils.ThreadPool;

/**
 * @author wilkey
 * @mail admin@wilkey.vip
 * @Date 2017年1月10日 下午4:25:00
 */
public class BestSellerRankFetcher {

	private static Logger log = Logger.getLogger(BestSellerRankFetcher.class);
	// https://www.amazon.com/gp/cart/ajax-update.html/ref=ox_sc_update_quantity_1%7C5%7C9?hasMoreItems=0&timeStamp=1483943437&token=gLlivHf2vegw6KN/2Zyn4y/Vu43CbjJqruDM4dwAAAAJAAAAAFhzLg1yYXcAAAAA&requestID=EWCFGCFB10F33Q96XWP8&quantity.C1ZX7K3VT5AIR1=9&pageAction=update-quantity&submit.update-quantity.C1ZX7K3VT5AIR1=1&actionItemID=C1ZX7K3VT5AIR1&asin=B00ITFZTHC
	// https://www.amazon.com/gp/navigation/ajax/dynamic-menu.html?cartItems=cart&rid=A5SA17R53F0SB9JEE2GD&isFullWidthPrime=0&isPrime=0&weblabs=&customerId=0&sessionId=154-3551805-0723447&marketplaceId=ATVPDKIKX0DER&dynamicRequest=1&primeMenuWidth=310&isFreshRegionAndCustomer=&_=1483956146849

	/**
	 * @param args
	 * @throws IOException
	 * @throws ParseException
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws InterruptedException
	 */
	public static void main(String[] args) throws Exception {
		System.err.println("starting......");
		BestSellerRankFetcher.timer();
		System.in.read();
	}

	public static void timer() {
		TimerTask task = new TimerTask() {
			@Override
			public void run() {
				final LinkedList<ProxyConfig> proxies = new LinkedList<ProxyConfig>();
				final LinkedList<String> urls = new LinkedList<String>();
				//静态块初始化非动态调用
				String[] ut = FileUtils.geturl().split("\n\r");
				for (String url : ut) {
					urls.add(url);
				}
				
				String[] pt = FileUtils.getproxy().split("\n\r");
				for (String proxy : pt) {
					ProxyConfig pc = new ProxyConfig();
					pc.setProxyHost(proxy.split(",")[0]);
					pc.setProxyPort(Integer.valueOf(proxy.split(",")[1]));
					proxies.add(pc);
				}
				
				Collections.shuffle(proxies);
				for (int i = 0; i < urls.size(); i++) {
					CategoryThread thread = new CategoryThread(urls.get(i), proxies.get(i));
					ThreadPool.getInstance().execute(thread);
				}
			}
		};

		Calendar calendar = Calendar.getInstance();
		int year = calendar.get(Calendar.YEAR);
		int month = calendar.get(Calendar.MONTH);
		int day = calendar.get(Calendar.DAY_OF_MONTH);
		calendar.set(year, month, day, 9, 10, 00);
		Date date = calendar.getTime();
		Timer timer = new Timer();
		timer.schedule(task, date);
	}

	static class CategoryThread implements Runnable {
		String url;
		String categoryId;
		ProxyConfig proxy;

		public CategoryThread(String url, ProxyConfig proxyConfig) {
			this.url = url;
			this.proxy = proxyConfig;
			this.categoryId = url.substring(url.lastIndexOf("/") + 1);
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			LogFactory.getFactory().setAttribute("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
			java.util.logging.Logger.getLogger("com.gargoylesoftware.htmlunit").setLevel(java.util.logging.Level.OFF);
			java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(java.util.logging.Level.OFF);
			java.util.logging.Logger.getLogger("org.apache.http").setLevel(java.util.logging.Level.OFF);
			
			List<Item> items = new ArrayList<Item>();
			final ExecutorService pool = Executors.newFixedThreadPool(3);
			final ExecutorCompletionService<String> completionService = new ExecutorCompletionService<String>(pool);
			Integer[] counts = new Integer[] { 1, 2, 3, 4, 5 };

			long startTime = System.currentTimeMillis();
			for (final Integer count : counts) {
				completionService.submit(new Callable() {
					WebClient webClient = null;
					HtmlPage bsrpage = null;
					Document doc = null;

					@Override
					public Object call() throws Exception {
						try {
							boolean needretry = true;
							while (needretry) {
								try {
									webClient = new WebClient(BrowserVersion.getDefault(), proxy.getProxyHost(), proxy.getProxyPort());
									webClient.getOptions().setCssEnabled(false);
									webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
									webClient.getOptions().setThrowExceptionOnScriptError(false);
									webClient.getOptions().setTimeout(60 * 1000);
									bsrpage = webClient.getPage(url + "?ref=nav_logo&pg=" + count);
								} catch (HttpHostConnectException | SocketTimeoutException e1) {
									webClient = new WebClient();
									webClient.getOptions().setCssEnabled(false);
									webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
									webClient.getOptions().setThrowExceptionOnScriptError(false);
									webClient.getOptions().setTimeout(60 * 1000);
									bsrpage = webClient.getPage(url + "?ref=nav_logo&pg=" + count);
								}

								String htmlContent = bsrpage.getWebResponse().getContentAsString();
								if (htmlContent == null || htmlContent.contains("robot")) {
									log.error(log.getName() + " : Robot Check !!!  Repeat, Robot Check !!!");
									TimeUnit.SECONDS.sleep(120);
									continue;
								}

								doc = Jsoup.parse(htmlContent);
								if (doc == null) {
									log.error(log.getName() + " : program error: Unproperly  Dom Exception !!! ");
									TimeUnit.SECONDS.sleep(120);
									continue;
								}
								needretry = false;
							}

							Elements elements = doc.select("div#zg_centerListWrapper > div");
							Iterable<DomElement> iterators = bsrpage.getElementById("zg_centerListWrapper").getChildElements();
							List<DomElement> asinDoms = new ArrayList<DomElement>();
							for (Iterator<DomElement> tmp = iterators.iterator(); tmp.hasNext();)
								asinDoms.add(tmp.next());

							for (int j = 0; j < elements.size(); j++) {
								try {
									if (!"zg_itemImmersion".equals(elements.get(j).attr("class")))
										continue;
									webClient.getCookieManager().clearCookies();
									Item item = Parse.cleanProductBlock(elements.get(j));
									item.setRank(((count - 1) * 20) + j);
									item.setCategoryId(categoryId);

									DomElement dom = asinDoms.get(j);
									HtmlAnchor firsthref = dom.querySelector("a");
									if (firsthref == null) {
										items.add(item);
										continue;
									}

									HtmlPage firstpage = firsthref.click();
									HtmlInput firstsubmit = firstpage.querySelector("#add-to-cart-button");
									if (firstsubmit == null) {
										items.add(item);
										continue;
									}

									TimeUnit.SECONDS.sleep(new Random().nextInt(10) + 5); // checkrobot
									HtmlPage secondpage = firstsubmit.click();
									webClient.waitForBackgroundJavaScript(5 * 1000);
									if (secondpage == null) {
										items.add(item);
										continue;
									}

									DomElement cartbutton = secondpage.getElementById("hlb-view-cart");
									if (cartbutton == null) {
										items.add(item);
										continue;
									}

									TimeUnit.SECONDS.sleep(new Random().nextInt(10) + 5); // checkrobot
									HtmlAnchor secondhref = cartbutton.querySelector("a");
									HtmlPage thridpage = secondhref.click();
									webClient.waitForBackgroundJavaScript(5 * 1000);

									HtmlElement div = thridpage.querySelector(".sc-list-item");

									HtmlElement time = thridpage.getElementByName("timeStamp");
									HtmlElement token = thridpage.getElementByName("token");
									HtmlElement requestID = thridpage.getElementByName("requestID");

									updateStock(div, time, token, requestID, webClient, item);

									items.add(item);
									System.err.println(url + "?pg=" + count + ": No." + (((count - 1) * 20) + j) + " item, qty: [" + item.getStock() + "]");
									log.info(log.getName() + " : " + url + "?pg=" + count + ": No." + (((count - 1) * 20) + j) + " item: [" + item.getAsin() + "], qty: [" + item.getStock() + "]");
									TimeUnit.SECONDS.sleep(new Random().nextInt(10) + 5); // checkrobot
								} catch (java.net.SocketException | java.lang.RuntimeException e) {
									TimeUnit.SECONDS.sleep(120); // 被关闭后休眠，重新启动
									continue;
								}
							}
							webClient.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}
				});
			}

			for (int i = 1; i <= 5; i++) {
				try {
					completionService.take();
					synchronized (this) {
						BatchInsert(items);
						items.clear();
					}
				} catch (Exception e) {

				}
			}
			pool.shutdown();
			long endTime = System.currentTimeMillis();
			log.info(log.getName() + " : " + String.format("categoryId: " + categoryId + " best seller rank done，耗时%s秒", (endTime - startTime) / 1000));
		}

	}

	// save to db
	private static void BatchInsert(List<Item> items) {
		String sql = "insert into bz_bestsellerank (rank, asin, title, pic, href, price, listprice, rating, reviews, stock, category_id) values(?,?,?,?,?,?,?,?,?,?,?);";
		Connection conn = null;
		try {
			conn = DBUtil.openConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
			PreparedStatement pstmt = conn.prepareStatement(sql);
			int size = items.size() / 20;
			size = items.size() % 20 >= 0 ? size + 1 : size; // 5521,5
			for (int i = 0; i < size; i++) { // 6
				for (int j = 0; j < (i == size - 1 ? items.size() % 20 : 20); j++) {
					Item bean = items.get(i * 20 + j);
					pstmt.setInt(1, bean.getRank());
					pstmt.setString(2, bean.getAsin());
					pstmt.setString(3, bean.getTitle());
					pstmt.setString(4, bean.getPic());
					pstmt.setString(5, bean.getHref());
					if (bean.getPrice() == null)
						pstmt.setNull(6, Types.DOUBLE);
					else
						pstmt.setDouble(6, bean.getPrice());
					if (bean.getListprice() == null)
						pstmt.setNull(7, Types.DOUBLE);
					else
						pstmt.setDouble(7, bean.getListprice());
					pstmt.setString(8, bean.getRating());
					if (bean.getReviews() == null)
						pstmt.setNull(9, Types.INTEGER);
					else
						pstmt.setInt(9, bean.getReviews());
					if (bean.getStock() == null)
						pstmt.setNull(10, Types.INTEGER);
					else
						pstmt.setInt(10, bean.getStock());
					pstmt.setString(11, bean.getCategoryId());
					pstmt.addBatch();
				}
				pstmt.executeBatch();
				pstmt.clearBatch();
			}
			if (size > 0) {
				// DBUtil.execute(conn, "delete from bz_reviews where asin = '"
				// + anitem.itemID + "'");
			}
			conn.commit();
			pstmt.close();
		} catch (Exception e) {
			e.printStackTrace();
			log.error(log.getName() + " : program error: " + e);
		} finally {
			try {
				DBUtil.closeConnection();
			} catch (SQLException e) {
				log.error(log.getName() + " : program error: " + e);
				e.printStackTrace();
			}
		}
	}

	public static void updateStock(HtmlElement div, HtmlElement time, HtmlElement token, HtmlElement requestID, WebClient webClient, Item item) {
		try {
			String tmpUrl = "https://www.amazon.com/gp/cart/ajax-update.html/ref=ox_sc_update_quantity_1%7C999%7C999?hasMoreItems=0&timeStamp=" + time.getAttribute("value") + "&token="
					+ (URLEncoder.encode(token.getAttribute("value"), "UTF-8")) + "&requestID=" + requestID.getAttribute("value") + "&quantity." + div.getAttribute("data-itemid")
					+ "=999&addressId=new&addressZip=&closeAddonUpsell=1&flcExpanded=0&" + "pageAction=update-quantity&submit.update-quantity." + div.getAttribute("data-itemid") + "=1&actionItemID="
					+ div.getAttribute("data-itemid") + "&asin=" + div.getAttribute("data-asin");

			JavaScriptPage jsonpage = webClient.getPage(tmpUrl);
			webClient.waitForBackgroundJavaScript(5 * 1000);
			// System.err.println(jsonpage.getWebResponse().getContentAsString());
			String updatemsg = jsonpage.getWebResponse().getContentAsString();

			if (updatemsg.contains("cartQty")) {
				updatemsg = updatemsg.substring(updatemsg.indexOf("cartQty"));
				updatemsg = updatemsg.substring(0, updatemsg.indexOf(","));
				updatemsg = updatemsg.replaceAll("[^\\d.]", "");
				item.setStock(StringUtils.isNotBlank(updatemsg) ? Integer.valueOf(updatemsg) : null);
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error(log.getName() + " : program error: " + e);
		}
	}

}