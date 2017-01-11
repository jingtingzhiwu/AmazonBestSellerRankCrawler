package com.poof.crawler.bestsellerank;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.ParseException;
import org.apache.log4j.Logger;
import org.jsoup.nodes.Element;

/**
 * @author wilkey 
 * @mail admin@wilkey.vip
 * @Date 2017年1月10日 下午4:25:15
 */
public class Parse {

	private static Logger log = Logger.getLogger(Parse.class);

	public static Item cleanProductBlock(Element productBlock) throws ParseException {
		try {
			Element picEle = productBlock.select("img").first();
			String pic = picEle.attr("src");

			Element titleEle = productBlock.select("a").first();
			String title = titleEle.text();
			String href = titleEle.attr("href");

			// Nite-Ize-SpotLit-Carabiner-Resistant/dp/B001E8BA9M/ref=zg_bs_2975317011_1?_encoding=UTF8&psc=1&refRID=6BVVQEKSWZG64TF7DGNX
			String asin = href.substring(href.indexOf("/dp/") + 4, href.indexOf("/dp/") + 14);

			// price
			String listpriceTxt = null;
			String priceTxt = null;
			String reviewsCount = null;
			String rating = null;
			Element listpriceEle = productBlock.select(".listprice").first();
			if(listpriceEle != null)
				listpriceTxt = listpriceEle.text().replaceAll("[^\\d.]", "");
			
			Element priceEle = productBlock.select(".price").first() == null ? productBlock.select(".a-color-price").first() : productBlock.select(".price").first();
			if(priceEle != null)
				priceTxt = priceEle.text().replaceAll("[^\\d.]", "");
			
			Double listprice = StringUtils.isNotBlank(listpriceTxt) ? Double.parseDouble(listpriceTxt) : null;
			Double price = StringUtils.isNotBlank(priceTxt) ? Double.parseDouble(priceTxt) : null;

			// rating
			Element star = productBlock.select("i.a-icon-star").first();
			if(star != null)
				rating = StringUtils.isNotBlank(star.text()) ? star.text().substring(0, star.text().indexOf(" ")) : null;
			else
					rating = "";

			// reviews
			Element reviewsEle = productBlock.select("a.a-size-small").last() == null ? productBlock.select("a.a-link-normal").last() : productBlock.select("a.a-size-small").last();
			if(reviewsEle != null)
				reviewsCount = reviewsEle.text().replaceAll("[^\\d.]", "");
			Integer reviews = StringUtils.isNotBlank(reviewsCount) ? Integer.valueOf(reviewsCount) : null;

			return new Item(asin, title, pic, href, price, listprice, rating, reviews);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(log.getName() + " : program error: " + e);
			Element titleEle = productBlock.select("a").first();
			String title = titleEle.text();
			String href = titleEle.attr("href");
			String asin = href.substring(href.indexOf("/dp/") + 4, href.indexOf("/dp/") + 14);
			return new Item(asin, title);
		}
	}

}