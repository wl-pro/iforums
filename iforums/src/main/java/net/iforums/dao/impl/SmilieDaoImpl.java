/*
 * Copyright (c) JForum Team
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, 
 * with or without modification, are permitted provided 
 * that the following conditions are met:
 * 
 * 1) Redistributions of source code must retain the above 
 * copyright notice, this list of conditions and the 
 * following  disclaimer.
 * 2)  Redistributions in binary form must reproduce the 
 * above copyright notice, this list of conditions and 
 * the following disclaimer in the documentation and/or 
 * other materials provided with the distribution.
 * 3) Neither the name of "Rafael Steil" nor 
 * the names of its contributors may be used to endorse 
 * or promote products derived from this software without 
 * specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT 
 * HOLDERS AND CONTRIBUTORS "AS IS" AND ANY 
 * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, 
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF 
 * MERCHANTABILITY AND FITNESS FOR A PARTICULAR 
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL 
 * THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE 
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, 
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, 
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER 
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER 
 * IN CONTRACT, STRICT LIABILITY, OR TORT 
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN 
 * ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF 
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE
 * 
 * This file creation date: 13/01/2004 / 12:02:54
 * The JForum Project
 * http://www.jforum.net
 */
package net.iforums.dao.impl;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import net.iforums.JForumExecutionContext;
import net.iforums.beans.Smilie;
import net.iforums.dao.BaseORMDao;
import net.iforums.dao.SmilieDao;
import net.iforums.utils.DbUtils;
import net.iforums.utils.preferences.SystemGlobals;

import org.springframework.stereotype.Repository;

/**
 * @author Rafael Steil
 * @version $Id: GenericSmilieDao.java,v 1.9 2007/02/25 13:48:33 rafaelsteil Exp $
 */
@Repository
public class SmilieDaoImpl extends BaseORMDao<Smilie> implements SmilieDao
{

	/**
	 * @see net.jforum.repository.SmilieDao#delete(int, int)
	 */
	public void delete(int id)
	{
		PreparedStatement p = null;
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("SmiliesModel.delete"));
			p.setInt(1, id);
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
	 * @see net.jforum.repository.SmilieDao#update(net.jforum.entities.Smilie)
	 */
	public void update(Smilie smilie)
	{
		PreparedStatement p = null;
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("SmiliesModel.update"));
			p.setString(1, smilie.getCode());
			p.setString(2, smilie.getUrl());
			p.setString(3, smilie.getDiskName());
			p.setInt(4, smilie.getId());

			p.executeUpdate();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(p);
		}
	}

	private Smilie getSmilie(ResultSet rs) throws SQLException
	{
		Smilie s = new Smilie();

		s.setId(rs.getInt("smilie_id"));
		s.setCode(rs.getString("code"));
		s.setUrl(rs.getString("url"));
		s.setDiskName(rs.getString("disk_name"));

		return s;
	}

	/**
	 * @see net.jforum.repository.SmilieDao#selectAll()
	 */
	public List selectAll()
	{
		List l = new ArrayList();

		PreparedStatement p = null;
		ResultSet rs = null;
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("SmiliesModel.selectAll"));
			rs = p.executeQuery();
			while (rs.next()) {
				l.add(this.getSmilie(rs));
			}

			return l;
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(rs, p);
		}
	}

	/**
	 * @see net.iforums.dao.SmilieDao#selectById(int)
	 */
	public Smilie selectById(int id)
	{
		PreparedStatement p = null;
		ResultSet rs = null;
		try {
			p = JForumExecutionContext.getConnection()
					.prepareStatement(SystemGlobals.getSql("SmiliesModel.selectById"));
			p.setInt(1, id);

			Smilie s = new Smilie();

			rs = p.executeQuery();
			if (rs.next()) {
				s = this.getSmilie(rs);
			}

			return s;
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(rs, p);
		}
	}
}
