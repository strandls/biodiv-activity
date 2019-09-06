/**
 * 
 */
package com.strandls.activity.service;

import java.util.List;

import com.strandls.activity.pojo.ActivityResult;
import com.strandls.activity.pojo.ShowActivity;

/**
 * @author Abhishek Rudra
 *
 */
public interface ActivityService {

	public List<ShowActivity> fetchActivity(String objectType,Long objectId);
	
	public ActivityResult fetchActivityIbp(String objectType,Long objectId);
}
