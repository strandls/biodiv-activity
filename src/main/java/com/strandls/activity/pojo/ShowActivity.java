/**
 * 
 */
package com.strandls.activity.pojo;

/**
 * @author Abhishek Rudra
 *
 */
public class ShowActivity {

	private Activity activity;
	private Comments comments;
	private Comments reply;

	/**
	 * @param activity
	 * @param comments
	 * @param reply
	 */
	public ShowActivity(Activity activity, Comments comments, Comments reply) {
		super();
		this.activity = activity;
		this.comments = comments;
		this.reply = reply;
	}

	public Activity getActivity() {
		return activity;
	}

	public void setActivity(Activity activity) {
		this.activity = activity;
	}

	public Comments getComments() {
		return comments;
	}

	public void setComments(Comments comments) {
		this.comments = comments;
	}

	public Comments getReply() {
		return reply;
	}

	public void setReply(Comments reply) {
		this.reply = reply;
	}

}
