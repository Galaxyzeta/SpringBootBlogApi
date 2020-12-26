package com.galaxyzeta.blog.entity;

import java.util.Date;

public class Comment {
	private User user;
	private Integer cid;
	private String content;
	private Date createdAt;
	private Integer uid;
	private Integer bid;
	private Integer parent;
	private Integer prefer = 0;

	// Constructor
	public Comment() {}
	public Comment(String content, Integer uid, Integer bid, Integer parent) {
		this.content = content;
		this.uid = uid;
		this.bid = bid;
		this.parent = parent;
	}

	// Getter
	public String getContent() {
		return content;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public User getUser() {
		return user;
	}
	public Integer getBid() {
		return bid;
	}
	public Integer getUid() {
		return uid;
	}
	public Integer getCid() {
		return cid;
	}
	public Integer getParent() {
		return parent;
	}
	public Integer getPrefer() {
		return prefer;
	}
	
	// Setter
	public void setContent(String content) {
		this.content = content;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public void setBid(Integer bid) {
		this.bid = bid;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public void setCid(Integer cid) {
		this.cid = cid;
	}
	public void setParent(Integer parent) {
		this.parent = parent;
	}
	public void setPrefer(Integer prefer) {
		this.prefer = prefer;
	}

}
