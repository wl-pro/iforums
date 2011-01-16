package net.iforums.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import net.iforums.JForumExecutionContext;
import net.iforums.beans.Banlist;
import net.iforums.dao.BanlistDao;
import net.iforums.dao.BaseORMDao;
import net.iforums.utils.DbUtils;
import net.iforums.utils.preferences.SystemGlobals;

import org.springframework.stereotype.Repository;

@Repository
public class BanlistDaoImpl extends BaseORMDao<Banlist> implements BanlistDao{
	/**
	 * @see net.iforums.dao.BanlistDao#delete(int)
	 */
	public void delete(int banlistId)
	{
		PreparedStatement p = null;
		
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(
				SystemGlobals.getSql("BanlistModel.delete"));
			p.setInt(1, banlistId);
			p.executeUpdate();
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(p);
		}
	}

//	/**
//	 * @see net.iforums.dao.BanlistDao#insert(net.jforum.entities.Banlist)
//	 */
//	public void insert(Banlist b)
//	{
//		PreparedStatement p = null;
//		
//		try {
//			p = JForumExecutionContext.getConnection().prepareStatement(
//				SystemGlobals.getSql("BanlistModel.insert"));
//			
//			p.setInt(1, b.getUserId());
//			p.setString(2, b.getIp());
//			p.setString(3, b.getEmail());
//			
//			this.setAutoGeneratedKeysQuery(SystemGlobals.getSql("BanlistModel.lastGeneratedBanlistId"));
//			
//			int id = this.executeAutoKeysQuery(p);
//			
//			b.setId(id);
//		}
//		catch (Exception e) {
//			throw new RuntimeException(e);
//		}
//		finally {
//			DbUtils.close(p);
//		}
//	}
	
	/**
	 * @see net.iforums.dao.BanlistDao#selectAll()
	 */
	public List selectAll()
	{
		ResultSet rs = null;
		PreparedStatement p = null;
		
		List l = new ArrayList();
		
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(
				SystemGlobals.getSql("BanlistModel.selectAll"));
			rs = p.executeQuery();
			
			while (rs.next()) {
				Banlist b = new Banlist();
				
				b.setId(rs.getInt("banlist_id"));
				b.setUserId(rs.getInt("user_id"));
				b.setEmail(rs.getString("banlist_email"));
				b.setIp(rs.getString("banlist_ip"));
				
				l.add(b);
			}
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(rs, p);
		}
		
		return l;
	}
}