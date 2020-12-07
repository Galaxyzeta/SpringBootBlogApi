package com.galaxyzeta.blog.dao;

import java.util.List;
import com.galaxyzeta.blog.entity.Blog;
import com.galaxyzeta.blog.entity.BlogLikeParam;
import com.galaxyzeta.blog.util.Pager;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface BlogDao {
	
	String COLUMNS = "title, content, created_at, updated_at, uid, prefer, status, viewed, comment";
	String CROPPED_COLUMNS = "title, LEFT(content,150), created_at, updated_at, uid, prefer, status, viewed, comment";
	String VALUES = "#{title}, #{content}, #{createdAt}, #{updatedAt}, #{uid}, #{prefer}, #{status}, #{viewed}, #{comment}";
	String TABLENAME = "blog";

	@Insert("insert into blog ("+COLUMNS+") VALUES(" + VALUES +")")
	@Options(useGeneratedKeys = true, keyProperty = "bid", keyColumn = "bid")
	public void saveBlog(Blog blog);

	@Delete("delete from blog where bid = #{bid}")
	public void deleteByBlogId(Integer bid);

	@Update({
		"<script>",
		"UPDATE blog SET title = #{title}, updated_at = #{updatedAt}, content = #{content}",
		"WHERE bid = #{bid}",
		"</script>"
	})
	public void update(Blog blog);

	@Select("SELECT * from blog where bid = #{bid}")
	// 字段和属性名称不同，进行辅助映射
	@Results(id = "resultMap", value = {
		@Result(column = "created_at", property = "createdAt"),
		@Result(column = "updated_at", property = "updatedAt")
	})
	public Blog getByBlogId(Integer bid);

	@Select("SELECT bid, "+ CROPPED_COLUMNS +" FROM blog LIMIT #{itemStart}, #{itemPerPage}")
	@ResultMap("resultMap")
	public List<Blog> getByUserIdPaged(Integer cid, Pager pager);

	@Select({
		"SELECT * FROM blog WHERE tid = #{tid}",
		"LIMIT #{itemStart}, #{itemPerPage}"
	})
	@ResultMap("resultMap")
	public List<Blog> getByTagIdPaged(Integer tid, Pager pager);

	@Select({
		"SELECT bid, "+ CROPPED_COLUMNS +" FROM blog",
		"ORDER BY viewed DESC, prefer DESC, comment DESC",
		"LIMIT #{start}, #{count}"
	})
	@ResultMap("resultMap")
	public List<Blog> getHotBlogs(Integer start, Integer count);

	@Select({
		"SELECT bid, " + CROPPED_COLUMNS +" FROM blog",
		"ORDER BY updated_at DESC",
		"LIMIT #{itemStart}, #{itemPerPage}"
	})
	@ResultMap("resultMap")
	public List<Blog> getHomeBlogs(Pager pager);

	@Select({
		"SELECT bid, " + CROPPED_COLUMNS +" FROM blog",
		"ORDER BY updated_at DESC",
		"LIMIT #{itemStart}, #{count}"
	})
	@ResultMap("resultMap")
	public List<Blog> getHomeBlogsByParams(Integer itemStart, Integer count);

	@Select({
		"<script>",
		"SELECT * FROM blog WHERE name = #{tagname} ORDER BY created_at ",
		"<if test='asc == false'>DESC</if> ",
		"LIMIT #{itemStart}, #{itemPerPage}",
		"</script>"
	})
	@ResultMap("resultMap")
	public List<Blog> getByCreatedTimeSortedPaged(Pager pager, boolean asc);

	@Update({
		"UPDATE blog SET viewed = viewed + #{count} ",
		"WHERE bid = #{bid}"
	})
	public void viewedIncrement(Integer bid, Integer count);

	@Update({
		"UPDATE blog SET comment = comment + #{count} ",
		"WHERE bid = #{bid}"
	})
	public void commentIncrement(Integer bid, Integer count);

	@Update({
		"UPDATE blog SET prefer = prefer + #{count} ",
		"WHERE bid = #{bid}"
	})
	public void preferIncrement(Integer bid, Integer count);
	
	@Update({
		"<script>",
		"UPDATE blog SET prefer = (CASE bid ",
		"<foreach collection='list' item='item' index='index' separator=' '>",
		"WHEN #{item.bid} THEN #{item.count}",
		"</foreach>",
		"END)",
		"WHERE bid IN (",
		"<foreach collection = 'list' item='item' index='index' seperator=','>",
		"#{item.bid}",
		"</foreach>",
		")",
		"</script>"
	})
	public void updateBlogPrefersByBlogParams(List<BlogLikeParam> list);

	@Select({
		"SELECT prefer FROM blog WHERE bid = #{bid}"
	})
	public Integer getPreferByBlogId(Integer bid);

	@Select({
		"SELECT COUNT(1) FROM blog WHERE uid = #{uid}"
	})
	public Integer getBlogCountByUserId(Integer uid);

	@Select({
		"SELECT COUNT(1) FROM blog"
	})
	public Integer getAllBlogCount();

	@Select({
		"SELECT bid," + CROPPED_COLUMNS + ",",
		"MATCH(content, title) AGAINST (#{keyword} IN NATURAL LANGUAGE MODE) AS score",
		"FROM blog",
		"WHERE MATCH(content, title) AGAINST (#{keyword} IN NATURAL LANGUAGE MODE)",
		"ORDER BY score DESC",
		"LIMIT #{pager.itemStart}, #{pager.itemPerPage}"
	})
	@ResultMap("resultMap")
	public List<Blog> searchBlogPagedBySQL(String keyword, Pager pager);
	
	@Select({
		"SELECT COUNT(1) FROM blog WHERE bid = #{bid}"
	})
	public Integer existsByBlogId(Integer bid);
}