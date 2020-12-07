package com.galaxyzeta.blog.mq;

import com.galaxyzeta.blog.entity.Captcha;
import com.galaxyzeta.blog.service.MailService;
import com.galaxyzeta.blog.util.Constants;

import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = Constants.MQ_MAIL_QUEUE_NAME)
public class CaptchaReceiver {
	
	@Autowired
	AmqpTemplate rabbitmq;

	@Autowired
	JavaMailSender mailSender;

	@Autowired
	MailService mailService;

	@RabbitHandler
	public void receive(Captcha capt) {
		SimpleMailMessage msg = mailService.create(capt.getMail(), Constants.MAIL_SUBJECT, capt.getCaptcha());
		// mailSender.send(msg);
		// mailService.setMailStatusExpire(capt.getMail(), Constants.MAIL_STATUS_SENT, Constants.REDIS_CAPTCHA_EXPIRE);
	}
}
