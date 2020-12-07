package com.galaxyzeta.blog.util;

public class Constants {
	// === REDIS ===

	/** 验证码前缀 */
	public static final String REDIS_CAPTCHA_PREFIX = "capt_";
	/** 验证码时效 */
	public static final int REDIS_CAPTCHA_EXPIRE = 60 * 1;
	/** 被ban前的最大登录失败次数 */
	public static final int REDIS_MAX_LOGIN_FAILURE = 5;
	/** 用户登录失败次数前缀 */
	public static final String REDIS_LOGIN_FAILURE_COUNT_PREFIX = "fail_";
	/** 因登录错误太多被ban的用户key所加的前缀 */
	public static final String REDIS_BAN_PREFIX = "login_ban_";
	/** 被禁止登陆后的等待时间 */
	public static final int REDIS_BAN_EXPIRE = 60 * 5;
	/** 错误的登录计数器在一段时间后过期，每此错误将重置计数器 */
	public static final int REDIS_FAILURE_DURATION = 60;
	/** redis 用户登陆凭证前缀 */
	public static final String REDIS_TOKEN_PREFIX = "login_token_";
	/** redis 博客id */
	public static final String REDIS_BLOG_PREFIX = "blog_";
	/** redis 最新博客 */
	public static final String REDIS_NEW_BLOG = "blog_new";
	/** redis 最新博客队列的最大数量 */
	public static final Integer REDIS_NEW_BLOG_MAX_COUNT = 30;
	/** redis 热门博客 */
	public static final String REDIS_HOT_BLOG = "blog_hot";
	/** redis 热门博客队列的最大数量 */
	public static final Integer REDIS_HOT_BLOG_MAX_COUNT = 30;
	/** redis 点赞存储 */
	public static final String REDIS_USER_BLOG_LIKE_HASH = "blog_like";
	/** redis 点赞key的中间符号 */
	public static final String REDIS_LIKE_KEY_MIDDLE = "::";
	/** redis 点赞数量hash */
	public static final String REDIS_BLOG_LIKE_COUNT_HASH = "blog_like_count";
	/** redis 邮件验证码前缀 */
	public static final String MAIL_PREFIX = "MAIL_";
	/** redis IP访问限制前缀 */
	public static final String IP_PREFIX = "IP_";
	
	// === RABBIT MQ ===
	
	/** 邮件队列名 */
	public static final String MQ_MAIL_QUEUE_NAME= "mail";

	// === Other ===

	/** 博客文章摘要的最大长度 */
	public static final int BLOG_BODY_OVERVIEW_LIMIT = 150;
	/** 管理员权限 */
	public static final String ROLE_ADMIN = "ADMIN";
	/** 用户权限 */
	public static final String ROLE_USER = "USER";
	/** Token Header */
	public static final String TOKEN_HEADER = "Token";
	/** 流量控制，最大访问次数限制 */
	public static final int MAX_REQUEST_LIMIT = 5;
	/** 流量控制，访问次数清空刷新时间 */
	public static final int REQUEST_COUNT_EXPIRE = 2;

	// === Mail ===
	/** 邮件主题 */
	public static final String MAIL_SUBJECT = "这是您的验证码";
	/** 邮件已请求 */
	public static final int MAIL_STATUS_REQUESTED = 1;
	/** 邮件已发送 */
	public static final int MAIL_STATUS_SENT = 2;

	/** 错误页面 */
	public static final String ERROR_PAGE = "/error";
}
