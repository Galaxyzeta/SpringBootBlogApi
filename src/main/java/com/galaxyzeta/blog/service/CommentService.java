package com.galaxyzeta.blog.service;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.galaxyzeta.blog.dao.BlogDao;
import com.galaxyzeta.blog.dao.CommentDao;
import com.galaxyzeta.blog.entity.Comment;
import com.galaxyzeta.blog.exceptions.AccessViolationException;
import com.galaxyzeta.blog.util.Pager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

	@Autowired
	private CommentDao commentDao;

	@Autowired
	private BlogDao blogDao;

	/** 保存评论，事先检查是否存在博客文章，用户登录态的检验留在 controller */
	public void saveComment(Comment comment) throws AccessViolationException {
		if(blogDao.existsByBlogId(comment.getBid()) != 1)
			throw new AccessViolationException("博客id非法");
		// 检验 parent 和 rootparent 设置是否合法
		final Comment dbParentComment;

		comment.setCreatedAt(new Date());

		if(comment.getParent() != null) {
			dbParentComment = commentDao.getByCommentId(comment.getParent());
			if(dbParentComment == null) throw new AccessViolationException("父级评论非法");
			commentDao.saveComment(comment);
		} else {
			commentDao.saveComment(comment);
		}
	}

	/** 根据评论 id 删评论 */
	@Transactional
	public void userDeleteByCommentId(Integer cid, Integer uid) {
		// 验证确实是本人操作
		Comment dbComment = commentDao.getByCommentId(cid);
		if(dbComment == null || ! dbComment.getUid().equals(uid)) throw new AccessViolationException("无权操作");
		
		adminDeleteByCommentId(cid);
		
	}

	/** 管理员删评论，不需要验证评论是不是本人 */
	public void adminDeleteByCommentId(Integer cid) {
		List<Integer> ids = getCommentIdsByRootParentId(cid);
		commentDao.deleteCommentBatchByIds(ids);
	}

	
	/** 获得某个根 cid 的全部子评论（包括了自身） */
	public List<Comment> getCommentsByRootParentId(Integer rootParentId) {
		String[] arr = commentDao.getCommentIdStringByRootParentId(rootParentId).split(",");
		return commentDao.getCommentListByCommentId(arr);
	}

	public List<Integer> getCommentIdsByRootParentId(Integer rootParentId) {
		String[] arr = commentDao.getCommentIdStringByRootParentId(rootParentId).split(",");
		return Arrays.stream(arr).map(elem->Integer.valueOf(elem)).collect(Collectors.toList());
	}

	/** 根据评论 id 得到评论详情 */
	public Comment getByCommentId(Integer cid) {
		return commentDao.getByCommentId(cid);
	}

	/** 根据用户 id 获得评论，分页 */
	public List<Comment> getByUserIdPaged(Integer uid, Pager pager) {
		return commentDao.getByUserIdPaged(uid, pager);
	}

	/** 根据博客 id 获得评论，分页 */
	public List<Comment> getByBlogIdPaged(Integer bid, Pager pager) {
		return commentDao.getByBlogIdPaged(bid, pager);
	}

	/** 增长点赞数 */
	public void preferIncrement(Integer cid, Integer count) {
		commentDao.preferIncrement(cid, count);
	}

	/** 获取博客的评论数 */
	public Integer getCommentCountByBlogId(Integer bid) {
		return commentDao.getCommentCountByBlogId(bid);
	}
}
