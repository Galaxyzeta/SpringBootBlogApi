package com.galaxyzeta.blog.service;

import java.util.concurrent.TimeUnit;

import com.galaxyzeta.blog.dao.RoleDao;
import com.galaxyzeta.blog.dao.UserDao;
import com.galaxyzeta.blog.entity.Captcha;
import com.galaxyzeta.blog.entity.Role;
import com.galaxyzeta.blog.entity.User;
import com.galaxyzeta.blog.exceptions.AccessViolationException;
import com.galaxyzeta.blog.util.Constants;
import com.galaxyzeta.blog.util.SecurityUtil;
import com.galaxyzeta.blog.util.FormatUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {

	@Autowired
	private UserDao userDao;

	@Autowired
	private RoleDao roleDao;

	@Autowired
	private StringRedisTemplate redisTemplate;

	@Autowired
	private MailService mailService;

	public static final String ADMIN_NAME = "galaxyzeta";
	
	/** 用户登录，若成功则返回 token */
	public String login(String mail, String password) throws AccessViolationException {
		User user = userDao.getUserByMail(mail);
		if(user == null) {
			throw new AccessViolationException("用户邮箱不存在");
		}
		// 若已被禁止登录，直接抛出异常
		if (redisTemplate.opsForValue().get(Constants.REDIS_BAN_PREFIX + user.getMail()) != null) {
			throw new AccessViolationException("已被禁止登录，请在一定时间后重试");
		}
		// 密码验证
		if(! SecurityUtil.loginAuthorization(user.getPasswordHash(), user.getSalt(), password)) {
			// 错误惩罚计数 + 1
			final String loginFailureUser= Constants.REDIS_LOGIN_FAILURE_COUNT_PREFIX + user.getMail();
			redisTemplate.opsForValue().increment(loginFailureUser);
			redisTemplate.expire(loginFailureUser, Constants.REDIS_FAILURE_DURATION, TimeUnit.SECONDS);
			if(Integer.parseInt(redisTemplate.opsForValue().get(loginFailureUser)) >= Constants.REDIS_MAX_LOGIN_FAILURE) {
				// 错误次数太多，禁止登录
				redisTemplate.opsForValue().set(Constants.REDIS_BAN_PREFIX + user.getMail(), "1", Constants.REDIS_BAN_EXPIRE, TimeUnit.SECONDS);
				throw new AccessViolationException("密码错误次数太多，已被禁止登录");
			}
			throw new AccessViolationException("认证失败");
		}
		final String tok = SecurityUtil.createToken(mail);
		redisTemplate.opsForValue().set(Constants.REDIS_TOKEN_PREFIX + mail, tok);
		return tok;
	}

	// === 登陆状态验证 === 

	/** Token 转 User */
	public User token2User(String token) {
		final String mail = SecurityUtil.getMailFromToken(token);
		return userDao.getUserByMail(mail);
	} 

	/** token 指向的用户是不是期望的用户，用于判定用户登录态 */
	public boolean isSameUser(String token, User user) {
		if(!isLogin(token)) return false;
		if(token2User(token).getUid() != user.getUid()) return false;
		return true;
	}

	/** token 指向的用户是不是期望的用户，用于判定用户登录态 */
	public boolean isSameUser(String token, Integer uid) {
		if(!isLogin(token)) return false;
		if(token2User(token).getUid() != uid) return false;
		return true;
	}

	/** 若用户token指向的uid和目标uid不同，抛出异常 */
	public void denyServiceIfNotSameUser(String token, Integer uid) throws AccessViolationException {
		if(!isSameUser(token, uid)) throw new AccessViolationException("用户未登录");
	}

	/** 若token指向的用户无权操作，抛出异常 */
	public void denyServiceIfNoRole(String token, String roleName) throws AccessViolationException {
		if(!isLogin(token)) throw new AccessViolationException("用户未登录");
		final Role role;
		try {
			role = roleDao.getByRoleName(roleName);
		} catch (Exception e) {
			throw new AccessViolationException("权限不存在");
		}
		final User tokenUser = token2User(token);
		for(Integer r : roleDao.getRoleIdsByUserId(tokenUser.getUid())) {
			if(r == role.getRid()) return;	
		}
		throw new AccessViolationException("无权操作");
	}

	/** 检验 Token 正确性 */
	public boolean isLogin(String token) {
		// 格式和加密正确性检验
		boolean result = SecurityUtil.validateToken(token);
		if(result == false) return false;
		final String mail = SecurityUtil.getMailFromToken(token);
		// Token 是否在 redis 里
		if(redisTemplate.opsForValue().get(Constants.REDIS_TOKEN_PREFIX + mail) == null) return false;
		return true;
	}

	/** 用户登出：注销 redis 中的 token。需要事先验证 token 合法性。 */
	public void logout(String token) throws AccessViolationException {
		final String mail = SecurityUtil.getMailFromToken(token);
		redisTemplate.delete(Constants.REDIS_TOKEN_PREFIX + mail);
	}
	
	/** 根据邮件获得验证码 */
	public String getCaptcha(String mail) {
		return redisTemplate.opsForValue().get(Constants.REDIS_CAPTCHA_PREFIX + mail);
	}

	
	/** 用户请求验证码，系统生成随机验证码，设置过期时间后放入redis */
	public String requestCaptcha(String mail) {
		// 检查邮件发送情况，测试时请取消
		if(mailService.getMailStatus(mail) != null) {
			throw new AccessViolationException("邮件已经在发送了！请稍后再试！");
		}

		String captcha = SecurityUtil.getRandomCaptcha(SecurityUtil.CAPTCHA_LENGTH);
		Captcha captchaVO = new Captcha(mail, captcha);
		// 送入消息队列
		mailService.updateRedisAndSendCaptcha(captchaVO);
		
		redisTemplate.opsForValue().set(Constants.REDIS_CAPTCHA_PREFIX + mail, captcha, Constants.REDIS_CAPTCHA_EXPIRE, TimeUnit.SECONDS);
		return captcha;
	}

	/** 根据邮箱获得用户信息 */
	public User getUserByMail(String mail) {
		return userDao.getUserByMail(mail);
	}
	
	/** 用户注册 */
	public User register(User user) throws AccessViolationException{
		User dbUser = userDao.getUserByMail(user.getMail());
		userDao.getUserByMail(user.getMail());
		if(dbUser != null) {
			throw new AccessViolationException("用户邮箱已经被注册");
		}
		if(FormatUtil.isBlank(user.getUsername())) throw new AccessViolationException("用户名不能为空");
		if(! FormatUtil.isValidPassword(user.getPassword())) throw new AccessViolationException("密码不合要求");
		
		String correctCapt = getCaptcha(user.getMail());
		// 验证码校验
		if(user.captcha == null || !user.captcha.equals(correctCapt)) throw new AccessViolationException("验证码错误");
		// 密码盐
		String salt = SecurityUtil.getRandomSalt(10);
		String pwdhash = SecurityUtil.getEncryptedPassword(user.getPassword(), salt);
		// 存入数据库
		user.setPasswordHash(pwdhash);
		user.setSalt(salt);
		userDao.registerUser(user);
		
		return user;
	}

	/** 根据邮箱删除用户 */
	public void deleteByMail(String mail) {
		userDao.deleteUserByMail(mail);
	}
	
	/** 修改用户邮箱 */
	public void modify(String mail, String password) throws AccessViolationException {
		User user = userDao.getUserByMail(mail);
		if(user == null) {
			throw new AccessViolationException("用户不存在");
		} else {
			userDao.updatePasswordById(user.getUid(), SecurityUtil.getEncryptedPassword(password, user.getSalt()));
		}
	}

	public boolean isAdmin(String username) {
		return username == ADMIN_NAME;
	}

	/** 测试 */
	public boolean ping() {
		return true;
	}
}
