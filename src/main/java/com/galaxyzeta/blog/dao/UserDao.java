package com.galaxyzeta.blog.dao;

import com.galaxyzeta.blog.entity.User;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserDao {

	@Select("select * from user where mail = #{mail}")
	public User getUserByMail(String mail);

	@Select("SELECT * FROM user WHERE uid = #{uid}")
	public User getUserByUid(Integer integer);

	@Select("delete from user where mail = #{mail}")
	public void deleteUserByMail(String mail);

	@Insert("insert into user(username, passwordhash, salt, mail) values (#{username}, #{passwordHash}, #{salt}, #{mail})")
	@Options(useGeneratedKeys=true, keyColumn="uid", keyProperty = "uid")	// 自动返回自增主键
	public void registerUser(User user);

	@Update("update user set passwordhash = #{pwdhash} where uid = #{id}")
	public void updatePasswordById(Integer uid, String pwdhash);

}