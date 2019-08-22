/**
 * 
 */
package com.strandls.activity.pojo;

import com.strandls.observation.pojo.RecoIbp;
import com.strandls.traits.pojo.FactValuePair;
import com.strandls.user.pojo.UserIbp;
import com.strandls.userGroup.pojo.UserGroupIbp;
import com.strandls.utility.pojo.FlagIbp;

/**
 * @author Abhishek Rudra
 *
 */
public class ShowActivityIbp {

	private ActivityIbp activityIbp;
	private RecoIbp recoVote;
	private CommentsIbp commentsIbp;
	private CommentsIbp reply;
	private UserIbp userIbp;
	private FactValuePair factValuePair;
	private UserGroupIbp userGroupIbp;
	private FlagIbp flagIbp;

	/**
	 * @param activityIbp
	 * @param recoVote
	 * @param commentsIbp
	 * @param reply
	 * @param userIbp
	 * @param factValuePair
	 * @param userGroupIbp
	 * @param flagIbp
	 */
	public ShowActivityIbp(ActivityIbp activityIbp, RecoIbp recoVote, CommentsIbp commentsIbp, CommentsIbp reply,
			UserIbp userIbp, FactValuePair factValuePair, UserGroupIbp userGroupIbp, FlagIbp flagIbp) {
		super();
		this.activityIbp = activityIbp;
		this.recoVote = recoVote;
		this.commentsIbp = commentsIbp;
		this.reply = reply;
		this.userIbp = userIbp;
		this.factValuePair = factValuePair;
		this.userGroupIbp = userGroupIbp;
		this.flagIbp = flagIbp;
	}

	public ActivityIbp getActivityIbp() {
		return activityIbp;
	}

	public void setActivityIbp(ActivityIbp activityIbp) {
		this.activityIbp = activityIbp;
	}

	public RecoIbp getRecoVote() {
		return recoVote;
	}

	public void setRecoVote(RecoIbp recoVote) {
		this.recoVote = recoVote;
	}

	public CommentsIbp getCommentsIbp() {
		return commentsIbp;
	}

	public void setCommentsIbp(CommentsIbp commentsIbp) {
		this.commentsIbp = commentsIbp;
	}

	public CommentsIbp getReply() {
		return reply;
	}

	public void setReply(CommentsIbp reply) {
		this.reply = reply;
	}

	public UserIbp getUserIbp() {
		return userIbp;
	}

	public void setUserIbp(UserIbp userIbp) {
		this.userIbp = userIbp;
	}

	public FactValuePair getFactValuePair() {
		return factValuePair;
	}

	public void setFactValuePair(FactValuePair factValuePair) {
		this.factValuePair = factValuePair;
	}

	public UserGroupIbp getUserGroupIbp() {
		return userGroupIbp;
	}

	public void setUserGroupIbp(UserGroupIbp userGroupIbp) {
		this.userGroupIbp = userGroupIbp;
	}

	public FlagIbp getFlagIbp() {
		return flagIbp;
	}

	public void setFlagIbp(FlagIbp flagIbp) {
		this.flagIbp = flagIbp;
	}

}
