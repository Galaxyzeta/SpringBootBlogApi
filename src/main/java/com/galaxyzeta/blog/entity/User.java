package com.galaxyzeta.blog.entity;

import javax.validation.constraints.NotBlank;

import com.fasterxml.jackson.annotation.JsonIgnore;

import org.springframework.data.annotation.Transient;

public class User {
	public String username;
	public String password;
	
	@JsonIgnore
	public String passwordHash;

	public String mail = "";
	public Integer uid;
	public String salt;
	
	@Transient
	public String captcha;

	public Role role;

	public User() {}

	public User(String username, String password, String mail) {
		this.username = username;
		this.password = password;
		this.mail = mail;
	}
	
	// Getter
	public String getCaptcha() {
		return captcha;
	}
	public String getPassword() {
		return password;
	}
	public String getPasswordHash() {
		return passwordHash;
	}
	public String getUsername() {
		return username;
	}
	public String getSalt() {
		return salt;
	}
	public Integer getUid() {
		return uid;
	}
	public String getMail() {
		return mail;
	}
	public Role getRole() {
		return role;
	}

	// Setter
	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public void setSalt(String salt) {
		this.salt = salt;
	}
	public void setUid(Integer userid) {
		this.uid = userid;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
	public void setRole(Role role) {
		this.role = role;
	}

	// To-String
	@Override
	public String toString() {
		return String.format("%s|%s|%s", username, password, mail);
	}
}
