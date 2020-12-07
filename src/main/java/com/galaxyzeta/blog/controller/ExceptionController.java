package com.galaxyzeta.blog.controller;

import javax.validation.ConstraintViolationException;

import com.galaxyzeta.blog.exceptions.AccessViolationException;
import com.galaxyzeta.blog.util.Result;
import com.galaxyzeta.blog.util.ResultFactory;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionController {
	
	@ExceptionHandler(value = ConstraintViolationException.class)
	public Result handler(ConstraintViolationException e) {
		return ResultFactory.getNotFound("参数检验失败", null);
	}

	@ExceptionHandler(value = AccessViolationException.class)
	public Result handler(AccessViolationException e) {
		return ResultFactory.getNotFound(e.getMessage(), null);
	}

	@ExceptionHandler(value = NullPointerException.class)
	public Result handler(NullPointerException e) {
		return ResultFactory.getNotFound("数据非法", null);
	}

	@ExceptionHandler(value = Exception.class)
	public Result handler(Exception e) {
		e.printStackTrace();
		return ResultFactory.getInternalServerError("服务端发生错误，请联系管理员", null);
	}
}
