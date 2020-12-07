package com.galaxyzeta.blog.configuration;

import com.galaxyzeta.blog.util.Constants;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BeanConfig {
	@Bean
	public Queue mailQueue() {
		return new Queue(Constants.MQ_MAIL_QUEUE_NAME, true);
	}
}
