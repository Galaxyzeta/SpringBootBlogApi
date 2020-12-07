package com.galaxyzeta.blog.util;

import java.io.Serializable;

import org.springframework.http.HttpStatus;

public class Result implements Serializable {

	private static final long serialVersionUID = 1L;

	public String message;
	public HttpStatus status;
	public Object data;

	// for deserializer to call
	Result() {}

	// constructor
	public Result(String message, HttpStatus status, Object data) {
		this.message = message;
		this.status = status;
		this.data = data;
	}

	// getter
	public Object getData() {
		return data;
	}
	public String getMessage() {
		return message;
	}
	public HttpStatus getStatus() {
		return status;
	}

	// setter
	public void setData(Object data) {
		this.data = data;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public void setStatus(HttpStatus status) {
		this.status = status;
	}
}
