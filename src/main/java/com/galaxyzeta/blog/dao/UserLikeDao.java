package com.galaxyzeta.blog.dao;

import java.util.List;

import com.galaxyzeta.blog.entity.UserLike;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserLikeDao {

	public static final String COLUMNS = "uid, bid, status";

	public static final String VALUES = "#{uid}, #{bid}, #{status}";
	public static final String FOREACH_VALUES = "#{item.uid}, #{item.bid}, #{item.status}";

	@Insert({
		"INSERT INTO user_like"+ COLUMNS +"("+ VALUES +")"
	})
	public void saveUserLike(UserLike like);

	@Insert({
		"<script>",
		"INSERT IGNORE INTO user_like ("+COLUMNS+") VALUES ",
		"<foreach collection='list' item='item' index='index' separator=','>",
        "("+FOREACH_VALUES+")",
		"</foreach>",
		"</script>"
	})
	public void saveManyUserLikes(List<UserLike> list);

	@Select({
		"SELECT * FROM user_like WHERE uid = #{uid} AND bid = #{bid}"
	})
	public UserLike getUserLike(UserLike userLike);

}
