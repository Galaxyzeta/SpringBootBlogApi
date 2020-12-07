package com.galaxyzeta.blog.util;

import java.util.regex.Pattern;

public class FormatUtil {
	// 至少 8 位，含大小写数字的密码
	public static final String PASSWORD_REGEX = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9]).{8,}$";
	// 邮箱
	public static final String MAIL_REGEX = "[^@\t\r\n]+@[^@\t\r\n]+\\.[^@\t\r\n]+";

	public final static boolean isValidPassword(String password) {
		return matches(PASSWORD_REGEX, password);
	}

	public final static boolean isValidMail(String mail) {
		return matches(MAIL_REGEX, mail);
	}

	public final static boolean isBlank(String str) {
		return str == null || str.trim().equals("");
	}

	public final static boolean matches(String regex, String target) {
		return Pattern.matches(regex, target);
	}

	public final static String trimContent(String content, int len) {
		if(content.length() < len) return content;
		else return content.substring(0, len);
	}

	public final static String stringConcat(Object... stuff) {
		StringBuilder sb = new StringBuilder();
		for(Object i : stuff) {
			sb.append(i.toString());
		}
		return sb.toString();
	}

}
