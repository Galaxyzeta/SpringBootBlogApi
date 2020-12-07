package com.galaxyzeta.blog.entity;

public class Tag {
	private Integer tid;
	private Integer uid;
	private String name;

	public Tag(Integer uid, String name) {
		this.name = name;
		this.uid = uid;
	}

	// Getter
	public Integer getTid() {
		return tid;
	}
	public Integer getUid() {
		return uid;
	}
	public String getName() {
		return name;
	}

	// Setter
	public void setTid(Integer tagId) {
		this.tid = tagId;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public void setName(String name) {
		this.name = name;
	}
	
}
