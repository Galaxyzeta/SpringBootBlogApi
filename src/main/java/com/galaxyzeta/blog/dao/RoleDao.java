package com.galaxyzeta.blog.dao;

import java.util.List;

import com.galaxyzeta.blog.entity.Role;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface RoleDao {

	public static final String COLUMNS = "rid, role";

	public static final String VALUES = "#{rid}, #{role}";

	public static final String USER_ROLE_COLUMNS = "rid, uid";

	public static final String USER_ROLE_VALUES = "#{rid}, #{uid}";

	@Insert({
		"INSERT INTO role ("+ COLUMNS +") VALUES ("+ VALUES +")"
	})
	@Options(keyColumn = "rid", keyProperty = "rid")
	public void saveRole(Role role);

	@Delete({
		"DELETE FROM role WHERE rid = #{rid}"
	})
	public void deleteByRoleId(Integer rid);

	@Update({
		"UPDATE role SET role = #{role} WHERE rid = #{rid}"
	})
	public void update(Role role);

	@Select({
		"SELECT * FROM role WHERE rid = #{rid}"
	})
	public Role getByRoleId(Integer rid);

	@Select({
		"SELECT * FROM role WHERE role = #{roleName}"
	})
	public Role getByRoleName(String roleName);

	@Select({
		"SELECT rid, role FROM user_role",
		"INNER JOIN role USING(rid)",
		"INNER JOIN user USING(uid)",
		"WHERE uid = #{uid}"
	})
	public List<Role> getRolesByUserId(Integer uid);

	@Select({
		"SELECT rid FROM user_role WHERE uid = #{uid}"
	})
	public List<Integer> getRoleIdsByUserId(Integer uid);

	@Insert({
		"INSERT IGNORE INTO user_role (" + USER_ROLE_COLUMNS + ")",
		"VALUES (" + USER_ROLE_VALUES + ")"
	})
	public void grantUserRole(Integer uid, Integer rid);

	@Delete({
		"DELETE FROM user_role WHERE uid = #{uid} AND rid = #{rid}"
	})
	public void revokeUserRole(Integer uid, Integer rid);

	@Delete({
		"DELETE FROM user_role WHERE uid = #{uid}"
	})
	public void revokeAllUserRoles(Integer uid);
}
