package com.galaxyzeta.blog.controller;

import com.galaxyzeta.blog.entity.User;
import com.galaxyzeta.blog.service.UserService;
import com.galaxyzeta.blog.util.Result;
import com.galaxyzeta.blog.util.ResultFactory;
import com.galaxyzeta.blog.util.FormatUtil;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/user")
public class UserController {

	@Autowired
	UserService userService;

	// Debug 测试
	@GetMapping("/ping")
	public Result ping() {
		return ResultFactory.getSuccess("请求成功", "PONG");
	}

	// 请求验证码
	@GetMapping("/captcha")
	public Result requestCaptcha(@RequestParam String mail) {
		if(!FormatUtil.isValidMail(mail)) return ResultFactory.getNotFound("邮箱错误", null);
		return ResultFactory.getSuccess("验证码请求成功", userService.requestCaptcha(mail));
	}
	
	// 注册
	@PostMapping(path = "/register")
	public Result register(@RequestBody User user) {
		userService.register(user);
		return ResultFactory.getSuccess("注册成功", null);
	}

	// 登录 得到 token
	@PostMapping(value="/login")
	public Result login(@RequestBody User user) {
		if(!FormatUtil.isValidMail(user.getMail())) return ResultFactory.getNotFound("邮箱错误", null);
		String token = userService.login(user.getMail(), user.getPassword());
		return ResultFactory.getSuccess("登录成功", token);
	}
	
	// 登出
	@GetMapping(value="/logout")
	public Result logout(@RequestHeader String token) {
		if(!userService.isLogin(token)) return ResultFactory.getNotFound("token无效", null);
		userService.logout(token);
		return ResultFactory.getSuccess("登出成功", null);
	}
}
