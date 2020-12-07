package com.galaxyzeta.blog.entity;

import java.util.Date;
import java.util.List;

import javax.validation.constraints.NotBlank;

import com.galaxyzeta.blog.util.FormatUtil;

public class Blog {
	private Integer bid;
	private Integer uid;
	
	@NotBlank(message = "标题不能为空")
	private String title;
	private String content;
	private Date createdAt;
	private Date updatedAt;
	private Integer prefer = 0;
	private Integer status;
	private List<Tag> tags;
	private Integer viewed = 0;
	private Integer comment = 0;

	private User user;

	public Blog() {}

	public Blog(Integer uid, String title, String content) {
		this.title = title;
		this.uid = uid;
		this.content = content;
	}

	public Blog(Integer uid, String title, String content, List<Tag> tags) {
		this(uid, title, content);
		this.tags = tags;
	}

	// Getter
	public Integer getUid() {
		return uid;
	}
	public User getUser() {
		return user;
	}
	public Integer getBid() {
		return bid;
	}
	public List<Tag> getTags() {
		return tags;
	}
	public String getTitle() {
		return title;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public Integer getPrefer() {
		return prefer;
	}
	public Integer getStatus() {
		return status;
	}
	public Date getUpdatedAt() {
		return updatedAt;
	}
	public Integer getComment() {
		return comment;
	}
	public Integer getViewed() {
		return viewed;
	}
	public String getContent() {
		return content;
	}
	

	// Setter
	public void setContent(String content) {
		this.content = content;
	}
	public void setUid(Integer uid) {
		this.uid = uid;
	}
	public void setUser(User user) {
		this.user = user;
	}
	public void setBid(Integer blogId) {
		this.bid = blogId;
	}
	public void setTags(List<Tag> tags) {
		this.tags = tags;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public void setPrefer(Integer prefer) {
		this.prefer = prefer;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public void setUpdatedAt(Date updateAt) {
		this.updatedAt = updateAt;
	}
	public void setComment(Integer comment) {
		this.comment = comment;
	}
	public void setViewed(Integer viewed) {
		this.viewed = viewed;
	}

	// toString
	@Override
	public String toString() {
		return FormatUtil.stringConcat("title=", title, "|", "created=", createdAt.getTime(), "|", 
			"prefer=", prefer);
	}

}
