package com.galaxyzeta.blog.dao;

import java.util.List;

import com.galaxyzeta.blog.entity.Blog;
import com.galaxyzeta.blog.entity.BlogTagRef;
import com.galaxyzeta.blog.entity.Tag;
import com.galaxyzeta.blog.util.Pager;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface BlogTagRefDao {

	String COLUMNS = "bid, tid";
	String VALUES = "#{bid}, #{tid}";
	String FOREACH_VALUES = "#{item.bid}, #{item.tid}";

	@Select({
		"SELECT tid, name FROM blog_tag JOIN tag USING(tid)",
		"WHERE bid = #{bid}"
	})
	public List<Tag> getTagsByBlogId(Integer bid);

	@Select({
		"SELECT * FROM blog_tag JOIN tag USING(tid)",
		"JOIN blog USING(bid)",
		"WHERE tid = #{tid}"
	})
	public List<Blog> getBlogsByTagNamePaged(String name, Pager pager);

	@Select({
		"DELETE FROM blog_tag WHERE bid = #{blogid}"
	})
	public void deleteTagByBlogId(Integer blogid);

	@Insert({
		"insert into blog_tag ("+COLUMNS+") values ("+VALUES+")"
	})
	public void saveRef(BlogTagRefDao ref);

	@Insert({
		"<script>",
		"INSERT IGNORE INTO blog_tag ("+COLUMNS+") VALUES ",
		"<foreach collection='refs' item='item' index='index' separator=','>",
        "("+FOREACH_VALUES+")",
		"</foreach>",
		"</script>"
	})
	public void saveManyRefs(List<BlogTagRef> refs);
}
