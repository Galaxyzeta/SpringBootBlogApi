package com.galaxyzeta.blog.controller;

import javax.validation.constraints.Min;

import com.galaxyzeta.blog.entity.Comment;
import com.galaxyzeta.blog.service.CommentService;
import com.galaxyzeta.blog.service.UserService;
import com.galaxyzeta.blog.util.Constants;
import com.galaxyzeta.blog.util.Pager;
import com.galaxyzeta.blog.util.Result;
import com.galaxyzeta.blog.util.ResultFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/comment")
public class CommentController {

	@Autowired
	private CommentService commentService;

	@Autowired
	private UserService userService;

	@GetMapping(value="/blog/{bid}/{currentPage}/{itemPerPage}")
	public Result getByBlogIdPaged(@Min(0) @PathVariable Integer currentPage, @Min(0) @PathVariable int itemPerPage, @Min(0) @PathVariable Integer bid) {
		return ResultFactory.getSuccess("查询成功", commentService.getByBlogIdPaged(bid, new Pager(currentPage, itemPerPage)));
	}

	@GetMapping(value="/user/{uid}/{currentPage}/{itemPerPage}")
	public Result getByUsername(@Min(0) @PathVariable Integer currentPage, @Min(0) @PathVariable int itemPerPage, @Min(0) @PathVariable Integer uid)  {
		return ResultFactory.getSuccess("查询成功", commentService.getByUserIdPaged(uid, new Pager(currentPage, itemPerPage)));
	}

	/** 发布评论，需要token认证 */
	@PostMapping(value="/")
	public Result saveComment(@RequestHeader String token, @RequestBody Comment comment) {
		userService.denyServiceIfNotSameUser(token, comment.getUid());
		commentService.saveComment(comment);
		return ResultFactory.getSuccess("增加成功", null);
	}
	
	/** 普通用户删除评论，需要token认证 */
	@DeleteMapping(value = "/user/{uid}/{cid}")
	public Result deleteByUser(@RequestHeader String token, @PathVariable Integer uid, @PathVariable Integer cid) {
		userService.denyServiceIfNotSameUser(token, uid);
		commentService.userDeleteByCommentId(cid, uid);
		return ResultFactory.getSuccess("删除成功", null);
	}

	/** 管理员删除评论，需要管理员权限 */
	@DeleteMapping(value = "/admin/{cid}")
	public Result deleteByAdmin(@RequestHeader String token, @PathVariable Integer cid) {
		userService.denyServiceIfNoRole(token, Constants.ROLE_ADMIN);
		commentService.userDeleteByCommentId(cid, userService.token2User(token).getUid());
		return ResultFactory.getSuccess("删除成功", null);
	}
}
