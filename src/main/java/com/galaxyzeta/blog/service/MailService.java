package com.galaxyzeta.blog.service;

import java.util.concurrent.TimeUnit;

import com.galaxyzeta.blog.entity.Captcha;
import com.galaxyzeta.blog.util.Constants;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;

@Service
public class MailService {

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Value("${spring.mail.username}")
	private String mailFrom;
	
	@Autowired
	private AmqpTemplate rabbitTemplate;

	/** 验证码邮件开始排队 */
	public void updateRedisAndSendCaptcha(Captcha capt) {
		// setMailStatusExpire(capt.getMail(), Constants.MAIL_STATUS_REQUESTED, Constants.REDIS_CAPTCHA_EXPIRE);
		rabbitTemplate.convertAndSend(Constants.MQ_MAIL_QUEUE_NAME, capt);
	}

	/** 查询邮件状态 */
	public String getMailStatus(String mail) {
		return redisTemplate.opsForValue().get(Constants.MAIL_PREFIX + mail);
	}

	/** 设置邮件状态 */
	public void setMailStatus(String mail, int status) {
		redisTemplate.opsForValue().set(Constants.MAIL_PREFIX + mail, 
			String.valueOf(Constants.MAIL_STATUS_REQUESTED));
	}

	/** 设置邮件状态和过期 */
	public void setMailStatusExpire(String mail, int status, int expire) {
		redisTemplate.opsForValue().set(Constants.MAIL_PREFIX + mail, 
			String.valueOf(Constants.MAIL_STATUS_REQUESTED), expire, TimeUnit.SECONDS);
	}

	/** 创建邮件 */
	public SimpleMailMessage create(String to, String subject, String content) {
		SimpleMailMessage msg = new SimpleMailMessage();
		msg.setFrom(mailFrom);
		msg.setTo(to);
		msg.setSubject(subject);
		msg.setText(content);
		return msg;
	}

}
