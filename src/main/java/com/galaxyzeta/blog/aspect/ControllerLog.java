package com.galaxyzeta.blog.aspect;

import javax.servlet.http.HttpServletRequest;

import com.galaxyzeta.blog.util.FormatUtil;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 全局切面日志
 */
@Component
@Aspect
public class ControllerLog {
	
	@Autowired
	private HttpServletRequest request;

	@Pointcut("execution(public * com.galaxyzeta.blog.controller.*.*(..))")
	public void log() {}

	@Around("log()")
	public Object around(ProceedingJoinPoint pjp) throws Throwable {
		// Before
		long time = System.currentTimeMillis();
		// Execution
		Object obj = pjp.proceed();
		// After
		final Logger logger = LoggerFactory.getLogger(pjp.getClass());
		logger.info(FormatUtil.stringConcat(
			"ExecTime=[", System.currentTimeMillis() - time, "ms ] ",
			"Method=[", request.getMethod() ,"] ",
			"Uri=[", request.getRequestURI() ,"] ",
			"Args=[", pjp.getArgs(), "] "
		));

		return obj;
	}
}
