package net.iforums.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import net.iforums.JForumExecutionContext;
import net.iforums.beans.Group;
import net.iforums.beans.User;
import net.iforums.dao.BaseORMDao;
import net.iforums.dao.GroupSecurityDao;
import net.jforum.dao.generic.security.SecurityCommon;
import net.jforum.repository.RolesRepository;
import net.jforum.security.Role;
import net.jforum.security.RoleCollection;
import net.jforum.security.RoleValue;
import net.jforum.security.RoleValueCollection;
import net.jforum.util.DbUtils;
import net.jforum.util.preferences.SystemGlobals;

import org.apache.commons.lang.StringUtils;

public class GroupSecurityDAOImpl extends BaseORMDao<Group> implements GroupSecurityDao {
	private List selectForumRoles(int forumId) 
	{
		List l = new ArrayList();
		
		PreparedStatement p = null;
		ResultSet rs = null;
		
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(
				SystemGlobals.getSql("PermissionControl.selectForumRoles"));
			p.setString(1, String.valueOf(forumId));
			
			rs = p.executeQuery();
			
			while (rs.next()) {
				l.add(new Integer(rs.getInt("role_id")));
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(rs, p);
		}
		
		return l;
	}
	
	public void deleteForumRoles(int forumId) 
	{
		PreparedStatement p = null;
		
		List roleIds = this.selectForumRoles(forumId);
		
		try {
			StringBuffer ids = new StringBuffer();
			
			for (Iterator iterator = roleIds.iterator(); iterator.hasNext();) {
				Integer id = (Integer)iterator.next();
				ids.append(id).append(',');
			}
			
			ids.append("-1");
			
			// Role values
			String sql = SystemGlobals.getSql("PermissionControl.deleteRoleValues");
			sql = StringUtils.replace(sql, "#IDS#", ids.toString());
			
			p = JForumExecutionContext.getConnection().prepareStatement(sql);
			p.setString(1, String.valueOf(forumId));
			p.executeUpdate();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(p);
		}
	}
	
	/**
	 * @see net.jforum.dao.security.SecurityDAO#deleteAllRoles(int)
	 */
	public void deleteAllRoles(int groupId)
	{
		PreparedStatement p = null;
		
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(
				SystemGlobals.getSql("PermissionControl.deleteAllRoleValues"));
			p.setInt(1, groupId);
			p.executeUpdate();
			p.close();

			p = JForumExecutionContext.getConnection().prepareStatement(
				SystemGlobals.getSql("PermissionControl.deleteAllGroupRoles"));
			p.setInt(1, groupId);
			p.executeUpdate();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(p);
		}
	}

	/**
	 * @see net.jforum.dao.security.SecurityDAO#addRole(int, net.jforum.security.Role)
	 */
	public void addRole(int id, Role role)
	{
		this.addRole(id, role, null);
	}

	/**
	 * @see net.jforum.dao.security.SecurityDAO#addRole(int, net.jforum.security.Role,
	 *      net.jforum.security.RoleValueCollection)
	 */
	public void addRole(int id, Role role, RoleValueCollection roleValues)
	{
		this.setAutoGeneratedKeysQuery(SystemGlobals.getSql("PermissionControl.lastGeneratedRoleId"));
		SecurityCommon.executeAddRole(SystemGlobals.getSql("PermissionControl.addGroupRole"), id, role, roleValues,
			this.supportAutoGeneratedKeys(), this.getAutoGeneratedKeysQuery());
	}

	/**
	 * @see net.jforum.dao.security.SecurityDAO#loadRoles(int)
	 */
	public RoleCollection loadRoles(int groupId)
	{
		return this.loadRoles(new int[] { groupId });
	}
	
	protected RoleCollection loadRoles(int[] groupIds)
	{
		String sql = SystemGlobals.getSql("PermissionControl.loadGroupRoles");
		String groupIdAsString = SecurityCommon.groupIdAsString(groupIds);
		
		if ("".equals(groupIdAsString)) {
			// We suppose there is no "negative" group ids
			sql = sql.replaceAll("#IN#", "-1");
		}
		else {
			sql = sql.replaceAll("#IN#", groupIdAsString);
		}
		
		RoleCollection roles = new RoleCollection();
		
		PreparedStatement p = null;
		ResultSet rs = null;
		
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(sql);
			rs = p.executeQuery();
			
			roles = SecurityCommon.loadRoles(rs);
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(rs, p);
		}
		
		return roles;
	}

	/**
	 * @see net.jforum.dao.GroupSecurityDAO#addRoleValue(int, net.jforum.security.Role, net.jforum.security.RoleValueCollection)
	 */
	public void addRoleValue(int groupId, Role role, RoleValueCollection rvc)
	{
		PreparedStatement p = null;
		ResultSet rs = null;
		
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(
					SystemGlobals.getSql("PermissionControl.getRoleIdByName"));
			p.setString(1, role.getName());
			p.setInt(2, groupId);

			int roleId = -1;

			rs = p.executeQuery();
			if (rs.next()) {
				roleId = rs.getInt("role_id");
			}

			rs.close();
			rs = null;
			p.close();
			p = null;

			if (roleId == -1) {
				this.addRole(groupId, role, rvc);
			}
			else {
				p = JForumExecutionContext.getConnection().prepareStatement(
						SystemGlobals.getSql("PermissionControl.addRoleValues"));
				p.setInt(1, roleId);

				for (Iterator iter = rvc.iterator(); iter.hasNext();) {
					RoleValue rv = (RoleValue) iter.next();
					p.setString(2, rv.getValue());
					p.executeUpdate();
				}
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(rs, p);
		}
	}

	/**
	 * @see net.jforum.dao.GroupSecurityDAO#loadRolesByUserGroups(net.jforum.entities.User)
	 */
	public RoleCollection loadRolesByUserGroups(User user)
	{
		List groups = user.getGroupsList();

		// When the user is associated to more than one group, we
		// should check the merged roles
		int[] groupIds = this.getSortedGroupIds(groups);

		RoleCollection groupRoles = RolesRepository.getGroupRoles(groupIds);

		// Not cached yet? then do it now
		if (groupRoles == null) {
			groupRoles = this.loadRoles(groupIds);
			RolesRepository.addGroupRoles(groupIds, groupRoles);
		}

		return groupRoles;
	}

	private int[] getSortedGroupIds(List groups)
	{
		int[] groupsIds = new int[groups.size()];
		int i = 0;

		for (Iterator iter = groups.iterator(); iter.hasNext();) {
			groupsIds[i++] = ((Group)iter.next()).getId();
		}

		Arrays.sort(groupsIds);

		return groupsIds;
	}
}
