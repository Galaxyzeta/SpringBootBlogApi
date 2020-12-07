package com.galaxyzeta.blog.dao;

import java.util.List;

import com.galaxyzeta.blog.entity.Tag;
import com.galaxyzeta.blog.util.Pager;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TagDao {

	String COLUMNS = "uid, name";
	String VALUES = "#{uid}, #{name}";
	String FOREACH_VALUES = "#{item.uid}, #{item.name}";


	@Insert({
		"INSERT IGNORE INTO tag ("+COLUMNS+") VALUES ("+VALUES+")"
	})
	@Options(useGeneratedKeys = true, keyColumn = "tid", keyProperty = "tid")
	public void saveTag(Tag tag);

	@Insert({
		"<script>",
		"INSERT IGNORE INTO tag ("+COLUMNS+") VALUES ",
		"<foreach collection='taglist' item='item' index='index' separator=','>",
        "("+FOREACH_VALUES+")",
		"</foreach>",
		"</script>"
	})
	@Options(useGeneratedKeys = true)
	public void saveManyTags(List<Tag> taglist);

	@Delete({
		"DELETE FROM tag WHERE tid = #{tid}"
	})
	public void deleteByTagId(Integer tid);
	
	@Select({
		"SELECT * FROM tag WHERE tid = #{tid}"
	})
	public Tag getByTagId(Integer tid);
	
	@Update({
		"<script>",
		"UPDATE tag ",
		"SET name = #{name}, uid = #{uid} WHERE tid = #{tid} ",
		"</script>"
	})
	public void update(Tag tag);
	
	@Select({
		"SELECT * FROM tag WHERE name = #{name}"
	})
	public Tag getByName(String name);
	
	@Select({
		"SELECT * FROM tag LIMIT #{itemStart}, #{itemPerPage}"
	})
	public List<Tag> getAllTagsPaged(Pager pager);

	@Select({
		"SELECT * FROM tag INNER JOIN (SELECT * FROM blog_tag WHERE bid = #{bid}) AS temp ON tag.tid = temp.tid"
	})
	public List<Tag> getTagsByBlogId(Integer bid);

	
	
}
