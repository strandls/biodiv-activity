/**
 * 
 */
package com.strandls.activity.service;

import javax.servlet.http.HttpServletRequest;

import com.strandls.activity.pojo.Activity;
import com.strandls.activity.pojo.ActivityLoggingData;
import com.strandls.activity.pojo.ActivityResult;
import com.strandls.activity.pojo.CommentLoggingData;
import com.strandls.activity.pojo.UserGroupActivityLogging;

/**
 * @author Abhishek Rudra
 *
 */
public interface ActivityService {

	public ActivityResult fetchActivityIbp(String objectType, Long objectId, String offset, String limit);

	public Activity logActivities(HttpServletRequest request, Long userId, ActivityLoggingData loggingData);

	public Activity addComment(HttpServletRequest request, Long userId, CommentLoggingData commentData);

	public String sendObvCreateMail(Long userid, ActivityLoggingData loggingData);

	public Activity logUGActivities(Long userId, UserGroupActivityLogging loggingData);

	public Integer activityCount(String objectType, Long objectId);

}
