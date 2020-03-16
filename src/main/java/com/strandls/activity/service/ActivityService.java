/**
 * 
 */
package com.strandls.activity.service;

import com.strandls.activity.pojo.Activity;
import com.strandls.activity.pojo.ActivityLoggingData;
import com.strandls.activity.pojo.ActivityResult;
import com.strandls.activity.pojo.CommentLoggingData;

/**
 * @author Abhishek Rudra
 *
 */
public interface ActivityService {

	public ActivityResult fetchActivityIbp(String objectType, Long objectId, String offset, String limit);

	public Activity logActivities(Long userId, ActivityLoggingData loggingData);

	public Activity addComment(Long userId, CommentLoggingData commentData);

	public void migrateData();

}
