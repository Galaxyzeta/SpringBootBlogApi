package com.galaxyzeta.blog.entity;

import java.io.Serializable;

/**
 * 验证码视图物体
 */
public class Captcha implements Serializable {

	private static final long serialVersionUID = 1L;

	private String mail;
	private String captcha;

	public Captcha(String mail, String captcha) {
		this.mail = mail;
		this.captcha = captcha;
	}

	public String getMail() {
		return mail;
	}
	public String getCaptcha() {
		return captcha;
	}
	public void setCaptcha(String captcha) {
		this.captcha = captcha;
	}
	public void setMail(String mail) {
		this.mail = mail;
	}
}
