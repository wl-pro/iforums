/*
 * Copyright (c) JForum Team
 * All rights reserved.

 * Redistribution and use in source and binary forms, 
 * with or without modification, are permitted provided 
 * that the following conditions are met:

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
 * Created on 13/11/2004 01:53:12
 * The JForum Project
 * http://www.jforum.net
 */
package net.iforums;

import java.sql.Connection;

import net.iforums.dao.CategoryDao;
import net.iforums.dao.ConfigDao;
import net.iforums.dao.ForumDao;
import net.iforums.repository.ForumRepository;

import org.apache.log4j.Logger;

/**
 * @author Rafael Steil
 * @version $Id: ForumStartup.java,v 1.16 2006/08/23 02:24:06 rafaelsteil Exp $
 */
public class ForumStartup 
{
	
	private static final Logger log = Logger.getLogger(ForumStartup.class);
	
	/**
	 * Starts the database implementation
	 * @return <code>true</code> if everthing were ok
	 * @throws RuntimeException if something were wrong
	 */
	public static boolean startDatabase()
	{
		try {
			if (DBConnection.createInstance()) {
				DBConnection.getImplementation().init();
				
				// Check if we're in fact up and running
				Connection conn = DBConnection.getImplementation().getConnection();
				DBConnection.getImplementation().releaseConnection(conn);
			}
		}
		catch (Exception e) {
			throw new RuntimeException("Error while trying to start the database: " + e, e);
		}
		
		return true;
	}
	
	/**
	 * Starts the cache control for forums and categories.
	 * @throws RepositoryStartupException is something were wrong.
	 */
	public static void startForumRepository()
	{
		try {
			ForumDao fm = DataAccessDriver.getInstance().newForumDao();
			CategoryDao cm = DataAccessDriver.getInstance().newCategoryDao();
			ConfigDao configModel = DataAccessDriver.getInstance().newConfigDao();

			ForumRepository.start(fm, cm, configModel);
		}
		catch (Exception e) {
			log.error("Unable to bootstrap JForum repository.", e);
			throw new RuntimeException("Error while trying to start ForumRepository: " + e, e);
		}
	}
}
