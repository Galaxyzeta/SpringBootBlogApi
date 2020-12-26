package com.galaxyzeta.blog.dao;

import java.util.List;

import com.galaxyzeta.blog.entity.Comment;
import com.galaxyzeta.blog.util.Pager;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface CommentDao {
	
	String COLUMNS_ROOTPARENT = "uid, bid, created_at, content, parent, prefer";
	String COLUMNS = "uid, bid, created_at, content, parent, prefer";

	String VALUES_ROOTPARENT = "#{uid}, #{bid}, #{createdAt}, #{content}, #{parent}, #{prefer}";
	String VALUES = "#{uid}, #{bid}, #{createdAt}, #{content}, #{parent}, #{prefer}";

	@Insert({
		"INSERT INTO comment ("+COLUMNS_ROOTPARENT+") VALUES ("+VALUES_ROOTPARENT+")"
	})
	@Options(useGeneratedKeys = true, keyColumn = "cid", keyProperty = "cid")
	public void saveComment(Comment comment);

	@Delete({
		"DELETE FROM comment WHERE cid = #{cid}"
	})
	public void deleteByCommentId(Integer cid);
	
	@Select({
		"SELECT * FROM comment WHERE cid = #{cid}"
	})
	public Comment getByCommentId(Integer cid);
	
	@Select({
		"SELECT * FROM comment WHERE uid = #{uid} LIMIT #{pager.itemStart}, #{pager.itemPerPage}"
	})
	public List<Comment> getByUserIdPaged(Integer uid, Pager pager);

	@Select({
		"SELECT * FROM comment WHERE bid = #{bid} LIMIT #{pager.itemStart}, #{pager.itemPerPage}"
	})
	public List<Comment> getByBlogIdPaged(Integer bid, Pager pager);
	
	@Update({
		"UPDATE comment SET prefer = prefer + #{count} WHERE cid = #{cid}"
	})
	public void preferIncrement(Integer cid, Integer count);

	@Select({
		"SELECT cid FROM comment WHERE parent = #{parentCid}"
	})
	public List<Integer> getChildrenCommentIdsByParent(Integer parentCid);

	@Delete({
		"<script>",
		"DELETE FROM comment WHERE cid IN",
		"<foreach collection='list' item='item' index='index' separator=',' open='(' close=')'>",
		"#{item}",
		"</foreach>",
		"</script>",
	})
	public void deleteCommentBatchByIds(List<Integer> list);

	@Select({
		"SELECT COUNT(1) FROM comment WHERE cid = #{cid}"
	})
	public Integer existsByCommentId(Integer cid);

	@Select({
		"SELECT COUNT(1) FROM comment WHERE bid = #{bid}"
	})
	public Integer getCommentCountByBlogId(Integer bid);

	@Select({
		"SELECT find_children_comment_cascade(#{cid})"
	})
	public String getCommentIdStringByRootParentId(Integer cid);

	@Select({
		"SELECT delete_comment_cascade(#{cid})"
	})
	public String deleteCommentsByRootParentId(Integer cid);

	@Select({
		"<script>",
		"SELECT * FROM comment WHERE cid IN",
		"<foreach collection='arr' item='item' index='index' separator=',' open='(' close=')'>",
		"#{item}",
		"</foreach>",
		"</script>",
	})
	public List<Comment> getCommentListByCommentId(String[] arr);
}
