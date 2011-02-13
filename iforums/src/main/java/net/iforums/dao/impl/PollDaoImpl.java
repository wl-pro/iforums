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
 * Created on 21/05/2004 - 14:19:11
 * The JForum Project
 * http://www.jforum.net
 */
package net.iforums.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import net.iforums.JForumExecutionContext;
import net.iforums.beans.Poll;
import net.iforums.beans.PollOption;
import net.iforums.dao.BaseORMDao;
import net.iforums.dao.PollDao;
import net.iforums.utils.DbUtils;
import net.iforums.utils.preferences.SystemGlobals;

import org.springframework.stereotype.Repository;

/**
 * @author David Almilli
 * @version $Id: GenericPollDao.java,v 1.9 2007/08/01 22:30:03 rafaelsteil Exp $
 */
@Repository
public class PollDaoImpl extends BaseORMDao<Poll> implements PollDao
{
	/**
	 * @see net.iforums.dao.PollDao#addNew(net.jforum.entities.Poll)
	 */
	public int addNew(Poll poll)
	{
		this.insert(poll);
		this.addNewPollOptions(poll.getId(), poll.getOptions());

		return poll.getId();
	}

	protected void addNewPollOptions(int pollId, List options)
	{
		Connection connection = JForumExecutionContext.getConnection();

		PreparedStatement p = null;
		ResultSet rs = null;
		try {
			p = connection.prepareStatement(SystemGlobals.getSql("PollModel.selectMaxVoteId"));

			p.setInt(1, pollId);
			rs = p.executeQuery();
			rs.next();

			int optionId = rs.getInt(1);

			rs.close();
			rs = null;
			p.close();
			p = null;

			p = connection.prepareStatement(SystemGlobals.getSql("PollModel.addNewPollOption"));
			for (Iterator iter = options.iterator(); iter.hasNext();) {
				PollOption option = (PollOption) iter.next();

				p.setInt(1, pollId);
				p.setInt(2, ++optionId);
				p.setString(3, option.getText());

				p.executeUpdate();
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
	 * @see net.iforums.dao.PollDao#selectById(int)
	 */
	public Poll selectById(int pollId)
	{
		PreparedStatement p = null;
		PreparedStatement o = null;
		ResultSet ors = null;
		ResultSet prs = null;
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("PollModel.selectById"));
			p.setInt(1, pollId);
			prs = p.executeQuery();

			Poll poll = null;
			if (prs.next()) {
				poll = this.makePoll(prs);

				o = JForumExecutionContext.getConnection().prepareStatement(
						SystemGlobals.getSql("PollModel.selectOptionsByPollId"));
				o.setInt(1, pollId);
				ors = o.executeQuery();

				while (ors.next()) {
					poll.addOption(this.makePollOption(ors));
				}
			}

			return poll;
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(prs, p);
			DbUtils.close(ors, o);
		}
	}

	protected Poll makePoll(ResultSet rs) throws SQLException
	{
		Poll poll = new Poll();
		poll.setId(rs.getInt("vote_id"));
		poll.setTopicId(rs.getInt("topic_id"));
		poll.setLabel(rs.getString("vote_text"));
		poll.setStartTime(new Date(rs.getTimestamp("vote_start").getTime()));
		poll.setLength(rs.getInt("vote_length"));

		return poll;
	}

	protected PollOption makePollOption(ResultSet rs) throws SQLException
	{
		PollOption option = new PollOption();
		option.setPollId(rs.getInt("vote_id"));
		option.setId(rs.getInt("vote_option_id"));
		option.setText(rs.getString("vote_option_text"));
		option.setVoteCount(rs.getInt("vote_result"));

		return option;
	}

	/**
	 * @see net.iforums.dao.PollDao#voteOnPoll(int, int, int, java.lang.String)
	 */
	public void voteOnPoll(int pollId, int optionId, int userId, String ipAddress)
	{
		Connection connection = JForumExecutionContext.getConnection();

		PreparedStatement v = null;
		PreparedStatement p = null;
		try {
			p = connection.prepareStatement(SystemGlobals.getSql("PollModel.incrementVoteCount"));
			v = connection.prepareStatement(SystemGlobals.getSql("PollModel.addNewVoter"));

			p.setInt(1, pollId);
			p.setInt(2, optionId);

			v.setInt(1, pollId);
			v.setInt(2, userId);
			v.setString(3, ipAddress);

			p.executeUpdate();
			v.executeUpdate();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(p);
			DbUtils.close(v);
		}
	}

	/**
	 * @see net.iforums.dao.PollDao#hasVotedOnPoll(int, int)
	 */
	public boolean hasUserVotedOnPoll(int pollId, int userId)
	{
		PreparedStatement p = null;
		ResultSet rs = null;
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("PollModel.selectVoter"));
			p.setInt(1, pollId);
			p.setInt(2, userId);

			rs = p.executeQuery();

			return rs.next();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(rs, p);
		}
	}

	/**
	 * Tells if the anonymous user has already voted on the given poll from the given IP
	 * 
	 * @param pollId
	 *            the poll id that is being checked
	 * @param ipAddress
	 *            the IP address of the anonymoususer to check the vote for
	 * @return true if the user has already voted on the given poll
	 */
	public boolean hasUserVotedOnPoll(int pollId, String ipAddress)
	{
		PreparedStatement p = null;
		ResultSet rs = null;
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(
					SystemGlobals.getSql("PollModel.selectVoterByIP"));
			p.setInt(1, pollId);
			p.setString(2, ipAddress);

			rs = p.executeQuery();

			return rs.next();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(rs, p);
		}
	}

	/**
	 * @see net.iforums.dao.PollDao#delete(int)
	 */
	public void deleteByTopicId(int topicId)
	{
		// first, lookup the poll id, then delete it
		PreparedStatement p = null;
		ResultSet rs = null;
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(
					SystemGlobals.getSql("PollModel.selectPollByTopicId"));

			p.setInt(1, topicId);

			rs = p.executeQuery();

			int pollId = 0;
			if (rs.next()) {
				pollId = rs.getInt("vote_id");
			}

			if (pollId != 0) {
				delete(pollId);
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
	 * @see net.iforums.dao.PollDao#delete(int)
	 */
	public void delete(int pollId)
	{
		this.deletePollVotes(pollId);
		this.deleteAllPollOptions(pollId);
		this.deletePoll(pollId);
	}

	protected void deletePoll(int pollId)
	{
		PreparedStatement poll = null;
		try {
			poll = JForumExecutionContext.getConnection()
					.prepareStatement(SystemGlobals.getSql("PollModel.deletePoll"));
			poll.setInt(1, pollId);
			poll.executeUpdate();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(poll);
		}
	}

	protected void deletePollVotes(int pollId)
	{
		PreparedStatement poll = null;
		try {
			poll = JForumExecutionContext.getConnection().prepareStatement(
					SystemGlobals.getSql("PollModel.deletePollVoters"));
			poll.setInt(1, pollId);
			poll.executeUpdate();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(poll);
		}
	}

	protected void deleteAllPollOptions(int pollId)
	{
		PreparedStatement poll = null;
		try {
			poll = JForumExecutionContext.getConnection().prepareStatement(
					SystemGlobals.getSql("PollModel.deleteAllPollOptions"));

			poll.setInt(1, pollId);
			poll.executeUpdate();
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
		finally {
			DbUtils.close(poll);
		}
	}

	protected void deletePollOptions(int pollId, List deleted) throws SQLException
	{
		Connection connection = JForumExecutionContext.getConnection();

		PreparedStatement options = null;
		try {
			options = connection.prepareStatement(SystemGlobals.getSql("PollModel.deletePollOption"));

			for (Iterator iter = deleted.iterator(); iter.hasNext();) {
				PollOption o = (PollOption) iter.next();

				// Option
				options.setInt(1, pollId);
				options.setInt(2, o.getId());
				options.executeUpdate();
			}
		}
		finally {
			DbUtils.close(options);
		}
	}

	protected void updatePollOptions(int pollId, List options) throws SQLException
	{
		PreparedStatement p = null;
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(
					SystemGlobals.getSql("PollModel.updatePollOption"));

			for (Iterator iter = options.iterator(); iter.hasNext();) {
				PollOption o = (PollOption) iter.next();

				p.setString(1, o.getText());
				p.setInt(2, o.getId());
				p.setInt(3, pollId);

				p.executeUpdate();
			}
		}
		finally {
			DbUtils.close(p);
		}
	}

	/**
	 * @see net.iforums.dao.PollDao#update(net.jforum.entities.Poll)
	 */
	public void update(Poll poll)
	{
		try {
			this.updatePoll(poll);

			if (poll.getChanges() != null) {
				this.deletePollOptions(poll.getId(), poll.getChanges().getDeletedOptions());
				this.updatePollOptions(poll.getId(), poll.getChanges().getChangedOptions());
				this.addNewPollOptions(poll.getId(), poll.getChanges().getNewOptions());
			}
		}
		catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	protected void updatePoll(Poll poll) throws SQLException
	{
		PreparedStatement p = null;
		try {
			p = JForumExecutionContext.getConnection().prepareStatement(SystemGlobals.getSql("PollModel.updatePoll"));

			p.setString(1, poll.getLabel());
			p.setInt(2, poll.getLength());
			p.setInt(3, poll.getId());

			p.executeUpdate();
		}
		finally {
			DbUtils.close(p);
		}
	}
}
