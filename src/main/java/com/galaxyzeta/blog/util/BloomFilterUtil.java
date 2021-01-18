package com.galaxyzeta.blog.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
/**
 * 布隆过滤器工具类
 */
public class BloomFilterUtil {

	@Autowired
	StringRedisTemplate redisTemplate;

	private static final long FACTOR = 133;

	/**
	 * 计算给定的 hash
	 */
	private Long hash(String toHash, long factor) {
		long hash = 0;
		char[] array = toHash.toCharArray();
		for(char c: array) {
			hash = hash * factor + c;
		}
		return hash;
	}

	/**
	 * 存入 bloomfilter
	 */
	public void save(String key, String toHash) {
		storeHashToBloomFilter(key, this.hash(toHash, FACTOR));
	}

	private void init() {
		redisTemplate.executePipelined(new RedisCallback<Object>(){
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				connection.openPipeline();
				for(int i=0; i<64; i++) {
					connection.setBit(Constants.REDIS_BLOOM_FILTER_BLOG.getBytes(), i, false);
				}
				return null;
			}
		});
	}

	/**
	 * 将 String 进行 hash 后，匹配其中每一个 1 所在的位，如果与 Bloomfilter 不一致，说明不存在，直接返回
	 */
	public boolean exists(String key, String toHash) {
		Long hashed = this.hash(toHash, FACTOR);
		boolean[] res = new boolean[1];
		
		// 管线操作
		redisTemplate.executePipelined(new RedisCallback<Object>(){
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				connection.openPipeline();
				long innerHashed = hashed;

				for(int i=0; i<32; i++, innerHashed >>= 1) {
					if((innerHashed & 1) == 1 && redisTemplate.opsForValue().getBit(key, i) == false) {
						res[0] = false;
						return null;
					}
				}

				res[0] = true;
				return null;
			}
		});

		return res[0];
	}
	
	private void storeHashToBloomFilter(String key, long hash) {
		// 布隆过滤器初始化 32位 0
		
		if(! redisTemplate.hasKey(key)) {
			init();
		}

		// 管线操作

		redisTemplate.executePipelined(new RedisCallback<Object>(){
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				connection.openPipeline();
				long innerhash = hash;
				for(int i=0; i<32; i++) {
					if((innerhash & 1) == 1) {
						connection.setBit(key.getBytes(), i, true);
					}
					innerhash >>= 1;
				}
				return null;
			}
		});
		
	}

	public static void main(String[] args) {
		int mask = 1;
		for(int i=0; i<5; i++) {
			System.out.println(mask);
			mask <<= 1;
		}
	}

}
