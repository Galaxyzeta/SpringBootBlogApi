package com.galaxyzeta.blog.entity;

public class BlogLikeParam {
	Integer bid;
	Integer count;

	public BlogLikeParam(Integer bid, Integer count) {
		this.bid = bid;
		this.count = count;
	}

	// getter
	public Integer getBid() {
		return bid;
	}
	public Integer getCount() {
		return count;
	}

	// setter
	public void setBid(Integer bid) {
		this.bid = bid;
	}
	public void setCount(Integer count) {
		this.count = count;
	}
}
