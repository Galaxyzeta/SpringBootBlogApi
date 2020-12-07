package com.galaxyzeta.blog.service;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.galaxyzeta.blog.entity.Blog;
import com.galaxyzeta.blog.entity.BlogLikeParam;
import com.galaxyzeta.blog.entity.UserLike;
import com.galaxyzeta.blog.util.Constants;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisService {
	
	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private ObjectMapper objectMapper;

	/** 从 Redis 得到用户点赞 */
	public List<UserLike> rmUserLikeFromRedis() throws IOException {
		final Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(Constants.REDIS_USER_BLOG_LIKE_HASH, ScanOptions.NONE);
		final List<UserLike> li = new LinkedList<>();
		while (cursor.hasNext()) {
			Map.Entry<Object, Object> entry = cursor.next();
			// 重新组合 uid :: bid
			final String key = (String)entry.getKey();
			final UserLike like = objectMapper.readValue((String)entry.getValue(), UserLike.class);
			li.add(like);
			// 从 redis 移除
			redisTemplate.opsForHash().delete(Constants.REDIS_USER_BLOG_LIKE_HASH, key);
		}
		return li;
	}

	/** 从 Redis 得到博客文章的点赞数 */
	public List<BlogLikeParam> rmBlogLikeCountFromRedis() {
		final Cursor<Map.Entry<Object, Object>> cursor = redisTemplate.opsForHash().scan(Constants.REDIS_BLOG_LIKE_COUNT_HASH, ScanOptions.NONE);
		final List<BlogLikeParam> res = new LinkedList<>();
		while (cursor.hasNext()) {
			Map.Entry<Object, Object> entry = cursor.next();
			final String key = (String)entry.getKey();
			final String value = (String)entry.getValue();
			res.add(new BlogLikeParam(Integer.parseInt(key), Integer.parseInt(value)));
			// 从 redis 移除
			redisTemplate.opsForHash().delete(Constants.REDIS_BLOG_LIKE_COUNT_HASH, key);
		}
		return res;
	}

	/** 完整博客信息存入 redis */
	public final void saveBlogToRedis(Blog blog) throws JsonProcessingException {
		redisTemplate.opsForValue().set(Constants.REDIS_BLOG_PREFIX + blog.getBid(), objectMapper.writeValueAsString(blog));
	}
}
