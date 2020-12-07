package com.galaxyzeta.blog.exceptions;

public class AccessViolationException extends CustomException {

	private static final long serialVersionUID = 1L;
	
	public AccessViolationException(String message) {
		super(message);
	}
}
