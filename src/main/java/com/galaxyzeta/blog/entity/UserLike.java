package com.galaxyzeta.blog.entity;

public class UserLike {
	private Integer uid;
	private Integer bid;
	private Integer status = 1;

	private UserLike() {};
	
	public UserLike(Integer uid, Integer bid) {
		this.uid = uid;
		this.bid = bid;
	}

	public UserLike(Integer uid, Integer bid, Integer status) {
		this(uid, bid);
		this.status = status;	
	}

	// Getter
	public Integer getBid() {
		return bid;
	}
	public Integer getUid() {
		return uid;
	}
	public Integer getStatus() {
		return status;
	}

	// Setter
	public void setBid(Integer bid) {
		this.bid = bid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
}
