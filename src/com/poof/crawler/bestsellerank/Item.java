package com.poof.crawler.bestsellerank;

import java.io.Serializable;

/**
 * @author wilkey 
 * @mail admin@wilkey.vip
 * @Date 2017年1月10日 下午4:25:08
 */
public class Item implements Serializable {

	private static final long serialVersionUID = -5497488667677979850L;

	private int rank;
	private String asin;
	private String title;
	private String pic;
	private String href;
	private Double price;
	private Double listprice;
	private String currency;
	private String rating; // 分数
	private Integer reviews; // 个数
	private Integer stock;
	private String categoryId;

	public String getAsin() {
		return asin;
	}

	public void setAsin(String asin) {
		this.asin = asin;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getPic() {
		return pic;
	}

	public void setPic(String pic) {
		this.pic = pic;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public Double getPrice() {
		return price;
	}

	public void setPrice(Double price) {
		this.price = price;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getRating() {
		return rating;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public Integer getReviews() {
		return reviews;
	}

	public void setReviews(Integer reviews) {
		this.reviews = reviews;
	}

	public Integer getStock() {
		return stock;
	}

	public void setStock(Integer stock) {
		this.stock = stock;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public Item(String asin, String title, String pic, String href, Double price, Double listprice, String currency, String rating, Integer reviews, Integer stock) {
		super();
		this.asin = asin;
		this.title = title;
		this.pic = pic;
		this.href = href;
		this.price = price;
		this.currency = currency;
		this.rating = rating;
		this.reviews = reviews;
		this.stock = stock;
	}

	public Item(String asin, String title, String pic, String href, Double price, Double listprice, String rating, Integer reviews) {
		super();
		this.asin = asin;
		this.title = title;
		this.pic = pic;
		this.href = href;
		this.price = price;
		this.rating = rating;
		this.reviews = reviews;
	}

	public Item(String asin, String title) {
		this.asin = asin;
		this.title = title;
	}

	public Double getListprice() {
		return listprice;
	}

	public void setListprice(Double listprice) {
		this.listprice = listprice;
	}

	public int getRank() {
		return rank;
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

}
