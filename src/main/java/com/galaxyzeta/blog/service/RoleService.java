package com.galaxyzeta.blog.service;

import java.util.List;

import com.galaxyzeta.blog.dao.RoleDao;
import com.galaxyzeta.blog.entity.Role;
import com.galaxyzeta.blog.exceptions.AccessViolationException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleService {
	
	@Autowired
	public RoleDao roleDao;

	/** 添加角色 */
	public void saveRole(Role role) {
		roleDao.saveRole(role);
	}

	/** 删除角色 */
	public void deleteRole(Integer roleId) {
		roleDao.deleteByRoleId(roleId);
	}

	/** 更改角色 */
	public void updateRole(Role role) {
		roleDao.update(role);
	}

	/** 查询角色 */
	public Role getRoleByRoleId(Integer roleId) {
		return roleDao.getByRoleId(roleId);
	}

	/** 根据角色名称查询角色 */
	public Role getRoleByRoleName(String roleName) {
		return roleDao.getByRoleName(roleName);
	}

	/** 获得特定用户的权限 */
	public List<Role> getRolesByUserId(Integer userId) {
		return roleDao.getRolesByUserId(userId);
	}

	/** 某用户是否具有某权限 */
	public boolean hasRole(Integer userId, String roleName) {
		List<Role> roles = roleDao.getRolesByUserId(userId);
		for (Role r : roles) {
			if(r.getRole().equals(roleName)) return true;
		}
		return false;
	}

	/** 给用户授权 */
	public void grantUserRole(Integer userId, String roleName) {
		Role role = roleDao.getByRoleName(roleName);
		if(role == null) throw new AccessViolationException("权限不存在");
		roleDao.grantUserRole(userId, role.getRid());
	}

	/** 移除用户的某个权限 */
	public void revokeUserRole(Integer userId, String roleName) {
		Role role = roleDao.getByRoleName(roleName);
		if(role == null) throw new AccessViolationException("权限不存在");
		roleDao.revokeUserRole(userId, role.getRid());
	}

	/** 移除某用户的所有权限 */
	public void revokeAllUserRoles(Integer uid) {
		roleDao.revokeAllUserRoles(uid);
	}
}
