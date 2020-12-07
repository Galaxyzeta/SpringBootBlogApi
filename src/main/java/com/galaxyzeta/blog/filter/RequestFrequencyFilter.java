package com.galaxyzeta.blog.filter;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.galaxyzeta.blog.util.Constants;

import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
/** 请求频繁过滤器 */
public class RequestFrequencyFilter extends OncePerRequestFilter {

	@Autowired
	private StringRedisTemplate redisTemplate;

	private static final Logger LOG = org.slf4j.LoggerFactory.getLogger(RequestFrequencyFilter.class);

	@Override
	/** 拒绝频繁请求 */
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		String ip = request.getRemoteAddr();
		final String key = Constants.IP_PREFIX + ip;
		final String redisValue = redisTemplate.opsForValue().get(key);
		LOG.info("Test Filter OK");
		if (redisValue == null) {
			redisTemplate.opsForValue().set(key, "1", Constants.REQUEST_COUNT_EXPIRE, TimeUnit.SECONDS);
		} else {
			int count = Integer.parseInt(redisValue) + 1;
			if(count > Constants.MAX_REQUEST_LIMIT) {
				// 请求频繁
				request.getRequestDispatcher(Constants.ERROR_PAGE).forward(request, response);
			} else {
				redisTemplate.opsForValue().set(key, String.valueOf(count));
			}
		}

		chain.doFilter(request, response);
	}
}