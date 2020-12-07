package com.galaxyzeta.blog.entity;

public class Role {
	private Integer rid;
	private String role;

	public Role(String role) {
		this.role = role;
	}

	// getter
	public Integer getRid() {
		return rid;
	}
	public String getRole() {
		return role;
	}

	// setter
	public void setRid(Integer rid) {
		this.rid = rid;
	}
	public void setRole(String role) {
		this.role = role;
	}
}
