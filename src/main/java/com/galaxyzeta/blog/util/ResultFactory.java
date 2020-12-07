package com.galaxyzeta.blog.util;

import org.springframework.http.HttpStatus;

public class ResultFactory {
	
	public static Result getSuccess(String message, Object data) {
		return new Result(message, HttpStatus.OK, data);
	}

	public static Result getSuccess(String message) {
		return new Result(message, HttpStatus.OK, null);
	}

	public static Result getNotFound(String message, Object data) {
		return new Result(message, HttpStatus.NOT_FOUND, data);
	}
	
	public static Result getNotFound(String message) {
		return new Result(message, HttpStatus.NOT_FOUND, null);
	}
	
	public static Result getInternalServerError(String message, Object data) {
		return new Result(message, HttpStatus.INTERNAL_SERVER_ERROR, data);
	}
}
