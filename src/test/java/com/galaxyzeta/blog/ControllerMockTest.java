package com.galaxyzeta.blog;

import static org.junit.jupiter.api.Assertions.assertEquals;

import javax.print.attribute.standard.Media;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.galaxyzeta.blog.entity.Blog;
import com.galaxyzeta.blog.entity.User;
import com.galaxyzeta.blog.util.Result;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MockMvcBuilder;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import jdk.jfr.ContentType;

@SpringBootTest
public class ControllerMockTest {
	
	@Autowired
	private WebApplicationContext context;

	private MockMvc client;

	private ObjectMapper mapper = new ObjectMapper();

	@BeforeEach
	void init() {
		client = MockMvcBuilders.webAppContextSetup(context).build();
	}

	// 简化结果获取
	final Result getResult(MvcResult res) throws Exception {
		MockHttpServletResponse resp = res.getResponse();
		resp.setCharacterEncoding("UTF-8");
		return (Result)mapper.readerFor(Result.class).readValue(resp.getContentAsString());
	}

	// 简化常规post
	final MvcResult mockPost(String path, Object body) throws Exception {
		return client.perform(MockMvcRequestBuilders.post(path)
			.contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(body))).andReturn();
	}

	// 简化常规get
	final MvcResult mockGet(String path) throws Exception {
		return client.perform(MockMvcRequestBuilders.get(path)).andReturn();
	}

	// 带 token 头访问的 get
	final MvcResult mockGetWithToken(String path, String token) throws Exception {
		return client.perform(MockMvcRequestBuilders.get(path).header("Token", token)).andReturn();
	}

	// 注册并登录一个测试用户，返回 token
	String mockUserRegisterLogin(final User user) throws Exception {
		MvcResult res = mockGet("/user/captcha?mail="+user.getMail());
		user.setCaptcha((String)(getResult(res).getData()));
		res = mockPost("/user/register", user);
		return (String)(getResult(res).getData());
	}
	
	@Test
	@Transactional
	void registerLoginTest() throws Exception {
		// Debug 性质的测试
		MvcResult res = mockGet("/user/ping");
		assertEquals("PONG", (String)(getResult(res).getData()));

		// === 常量定义 ===
		final String correctPassword = "A98zkqozm213";
		final String wrongPassword = "fuckyou";
		final String correctUsername = "asd";
		final String emptyUsername = "     ";
		final String registerMail = "123@qq.com";
		final String wrongMail = "456@qq.com";
		final String wrongCapt = "123";
		// === 场景测试 ===
		// 1. 123@qq.com 用户注册
		// 1.1 获得验证码
		res = mockGet("/user/captcha?mail="+registerMail);
		// 1.2 密码不符合要求
		User user = new User("asd", wrongPassword, registerMail);
		user.setCaptcha((String)(getResult(res).getData()));
		res = mockPost("/user/register", user);
		assertEquals(HttpStatus.NOT_FOUND, getResult(res).getStatus());
		// 1.3 用户名空
		user.setPassword(correctPassword);
		user.setUsername(emptyUsername);
		res = mockPost("/user/register", user);
		assertEquals(HttpStatus.NOT_FOUND, getResult(res).getStatus());
		// 1.4 验证码错误
		user.setCaptcha(wrongCapt);
		user.setUsername(correctUsername);
		res = mockPost("/user/register", user);
		assertEquals(HttpStatus.NOT_FOUND, getResult(res).getStatus());
		// 1.5 正确的注册
		res = mockGet("/user/captcha?mail="+registerMail);
		user.setCaptcha((String)(getResult(res).getData()));
		res = mockPost("/user/register", user);
		assertEquals(HttpStatus.OK, getResult(res).getStatus());

		// 2. 123@qq.com 用户登录
		// 2.1 用户不存在
		user = new User("asd", correctPassword, wrongMail);
		res = mockPost("/user/login", user);
		assertEquals(HttpStatus.NOT_FOUND, getResult(res).getStatus());
		// 2.2 用户密码错误
		user.setMail(registerMail);
		user.setPassword(wrongPassword);
		res = mockPost("/user/login", user);
		assertEquals(HttpStatus.NOT_FOUND, getResult(res).getStatus());
		// 2.3 正确登录
		user.setPassword(correctPassword);
		res = mockPost("/user/login", user);
		assertEquals(HttpStatus.OK, getResult(res).getStatus());

		// 3. 用户登出操作 -- 需要 token
		final String token = (String)(getResult(res).getData());
		System.out.println(token);
		res = mockGetWithToken("/user/logout", token);
		assertEquals(HttpStatus.OK, getResult(res).getStatus());
	}

	@Test
	@Transactional
	void blogCommentTagTest() throws Exception {
		final String token = mockUserRegisterLogin(new User("asd", "Asdqzw123", "123@qq.com"));
		
	}
}