package com.galaxyzeta.blog.controller;

import java.io.IOException;

import javax.validation.constraints.Min;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.galaxyzeta.blog.entity.Blog;
import com.galaxyzeta.blog.entity.UserLike;
import com.galaxyzeta.blog.exceptions.AccessViolationException;
import com.galaxyzeta.blog.service.BlogService;
import com.galaxyzeta.blog.service.UserService;
import com.galaxyzeta.blog.util.Constants;
import com.galaxyzeta.blog.util.Pager;
import com.galaxyzeta.blog.util.Result;
import com.galaxyzeta.blog.util.ResultFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@RestController
@RequestMapping(path = "/blog")
@Validated
public class BlogController {

	@Autowired
	BlogService blogService;

	@Autowired
	UserService userService;

	public static Logger LOG = LoggerFactory.getLogger(BlogController.class);

	/** 获得某篇博客的具体信息，不截断文字 */
	@GetMapping(value = "/{id}")
	public Result getBlogByBlogId(@PathVariable Integer id) throws IOException {
		return ResultFactory.getSuccess("查找成功", blogService.getBlogById(id));
	}

	/** 获得分页的主页博客文章，文字被截断成摘要 */
	@GetMapping(value = "/home/{currentPage}/{itemPerPage}")
	public Result getHomeBlogsPaged(@PathVariable @Min(0) Integer currentPage,
			@PathVariable @Min(0) Integer itemPerPage) throws IOException {
		return ResultFactory.getSuccess("查找成功", blogService.getHomeBlogs(new Pager(currentPage, itemPerPage)));
	}

	/** 获得分页的热门博客文章，文字被截断成摘要 */
	@GetMapping(value = "/hot/{currentPage}/{itemPerPage}")
	public Result getMethodName(@PathVariable @Min(0) Integer currentPage, @PathVariable @Min(0) Integer itemPerPage)
			throws IOException {
		return ResultFactory.getSuccess("查找成功", blogService.getHotBlogs(new Pager(currentPage, itemPerPage)));
	}

	/** 获得某用户的文章摘要，分页 */
	@GetMapping(value = "/user/{uid}")
	public Result getUserBlogPaged(@PathVariable Integer uid, @PathVariable @Min(0) Integer currentPage,
			@PathVariable @Min(0) Integer itemPerPage) {
		return ResultFactory.getSuccess("查找成功", blogService.getByUserIdPaged(uid, new Pager(currentPage, itemPerPage)));
	}

	/** 获得某用户的文章数量 */
	@GetMapping(value = "/user/count/{uid}")
	public Result getBlogCountByUserId(@PathVariable Integer uid) {
		return ResultFactory.getSuccess("查找成功", blogService.getBlogCountByUserId(uid));
	}

	/** 获得所有博客的数量 */
	@GetMapping(value = "/count")
	public Result getMethodName(@RequestParam String param) {
		return ResultFactory.getSuccess("查找成功", blogService.getAllBlogCount());
	}

	/** 修改博客，需要登录状态 */
	@PutMapping(value = "/")
	public Result updateBlog(@RequestHeader String token, @RequestBody Blog blog) throws JsonProcessingException {
		userService.denyServiceIfNotSameUser(token, blog.getUid());
		blogService.update(blog);
		return ResultFactory.getSuccess("更新成功");
	}
	
	/** 用户删除博客，需要登录者 token 和 blog uid 匹配 */
	@DeleteMapping(value = "/")
	public Result deleteBlogByBlogId(@RequestHeader String token, @RequestBody Blog blog) {
		userService.denyServiceIfNotSameUser(token, blog.getUid());
		blogService.deleteByBlogId(blog.getBid(), blog.getUid());
		return ResultFactory.getSuccess("删除成功");
	}

	/** 管理员删除博客，需要登录token权限是管理员 */
	@DeleteMapping(value = "/admin")
	public Result adminDeleteBlogByBlogId(@RequestHeader String token, @RequestBody Blog blog) {
		userService.denyServiceIfNoRole(token, Constants.ROLE_ADMIN);
		blogService.deleteByBlogId(blog.getBid(), blog.getUid());
		return ResultFactory.getSuccess("删除成功");
	}

	/** 操作文章点赞状态，需要token检验 */
	@PostMapping(value = "/like")
	public Result postLike(@RequestHeader String token, @RequestBody UserLike userLike)
			throws AccessViolationException, JsonProcessingException, IOException {
		userService.denyServiceIfNotSameUser(token, userLike.getUid());
		blogService.preferBlog(userLike);
		return ResultFactory.getSuccess("操作成功");
	}

	/** 发布博客文章，需要token检验 */
	@PostMapping(value = "/")
	public Result saveBlog(@RequestHeader String token, @RequestBody Blog blog) throws AccessViolationException, JsonProcessingException {
		userService.denyServiceIfNotSameUser(token, blog.getUid());
		blogService.saveBlog(blog);
		return ResultFactory.getSuccess("操作成功");
	}

}
