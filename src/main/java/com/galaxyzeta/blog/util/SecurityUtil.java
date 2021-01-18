package com.galaxyzeta.blog.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import org.springframework.stereotype.Component;

public class SecurityUtil {

	public static String charset = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz123456789";
	public static char[] hex = new char[] { '0', '1', '2', '3', '4', '5',
	'6', '7' , '8', '9', 'A', 'B', 'C', 'D', 'E','F' };
	public static final int CAPTCHA_LENGTH = 6;
	public static final String SECRET = "Galaxyzeta";
	public static final Long EXPIRE = 1000L * 60 * 10;

	public static boolean loginAuthorization(String passwordHash, String salt, String input) {
		String combined = salt + input;
		return MD5(combined).equals(passwordHash);
	}

	// 获得随机密码盐
	public static String getRandomSalt(int len) {
		StringBuilder sb = new StringBuilder();
		int slen = charset.length();
		while(len-- > 0) {
			sb.append(charset.charAt((int)(Math.random()*slen)));
		}
		return sb.toString();
	}

	// 获得加盐密码
	public static String getEncryptedPassword(String input, String salt) {

		String combined  = salt + input;
		return MD5(combined);
	}

	// 获得随机验证码
	public static String getRandomCaptcha(int length) {
		int charSetLength = charset.length();
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<CAPTCHA_LENGTH; i++) {
			sb.append(charset.charAt((int)(Math.random()*charSetLength)));
		}
		return sb.toString();
	}

	// 将字符串用MD5加密
	public static String MD5(String message) {
		MessageDigest md5;
		try {
			md5 = MessageDigest.getInstance("MD5");
		} catch(NoSuchAlgorithmException e) {
			return "";
		}
		byte[] bytes = md5.digest(message.getBytes());
		StringBuffer sb = new StringBuffer();
		for (byte bb : bytes) {
			sb.append(hex[(bb >> 4) & 15]);
			sb.append(hex[bb & 15]);
		}
		return sb.toString();
	}

	// 签发登陆凭证，Token = Md5(Secret + mail + expire_at) + Base64(mail) + Base64(expire_at)
	public static String createToken(String mail) {
		final String expire = Long.toString(System.currentTimeMillis() + EXPIRE);
		return MD5(SECRET + mail + expire)+","
		+ new String(Base64.getEncoder().encode(mail.getBytes()))+","
		+ new String(Base64.getEncoder().encode((expire).getBytes()));
	}

	// 验证登录凭证的有效性 不要直接使用
	public static boolean validateToken(String token) {
		String[] tokenArr = token.split(",");
		// Wrong format
		if(tokenArr.length != 3) return false;
		// Expired
		if(Long.parseLong(new String(Base64.getDecoder().decode(tokenArr[2].getBytes()))) <
			System.currentTimeMillis()) return false;
		// Authorization Failed
		final String mail = new String(Base64.getDecoder().decode(tokenArr[1]));
		final String expire = new String(Base64.getDecoder().decode(tokenArr[2]));
		if(! tokenArr[0].equals(MD5(SECRET + mail + expire))) return false;
		return true;
	}

	// 从 Token 获得 mail
	public static String getMailFromToken(String token) {
		String[] tokenArr = token.split(",");
		// Wrong format
		if(tokenArr.length != 3) return null;
		return new String(Base64.getDecoder().decode(tokenArr[1]));

	}
}
