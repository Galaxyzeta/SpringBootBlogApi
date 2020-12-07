package com.galaxyzeta.blog.entity;

public class BlogTagRef {
	
	private Integer bid;
	private Integer tid;

	public BlogTagRef(Integer blogId, Integer tagId) {
		this.bid = blogId;
		this.tid = tagId;
	}
	// getter
	public Integer getBlogId() {
		return bid;
	}
	public Integer getTagId() {
		return tid;
	}

	// setter
	public void setBlogId(Integer blogId) {
		this.bid = blogId;
	}
	public void setTagId(Integer tagId) {
		this.tid = tagId;
	}
}
