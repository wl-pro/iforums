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
 * Created on 21.09.2004 
 * The JForum Project
 * http://www.jforum.net
 */
package net.iforums.dao.hsqldb;

import net.iforums.dao.PostDao;
import net.iforums.dao.TopicDao;
import net.iforums.dao.UserDao;
import net.iforums.dao.generic.GenericDataAccessDriver;

/**
 * @author Marc Wick
 * @version $Id: HsqldbDataAccessDriver.java,v 1.9 2006/11/21 22:07:57 rafaelsteil Exp $
 */
public class HsqldbDataAccessDriver extends GenericDataAccessDriver 
{
	private static PostDao postDao ;
	private static UserDao userDao ;
	private static TopicDao topicDao;
	
	/**
	 * @see net.iforums.dao.DataAccessDriver#newPostDao()
	 */
	public PostDao newPostDao() {
		return postDao;
	}

	/**
	 * @see net.iforums.dao.DataAccessDriver#newTopicDao()
	 */
	public TopicDao newTopicDao() {
		return topicDao;
	}
	
	/**
	 * @see net.iforums.dao.DataAccessDriver#newUserDao()
	 */
	public UserDao newUserDao() {
		return userDao;
	}
}