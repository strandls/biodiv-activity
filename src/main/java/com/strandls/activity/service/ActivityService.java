/**
 * 
 */
package com.strandls.activity.service;

import java.util.List;

import com.strandls.activity.pojo.ShowActivity;
import com.strandls.activity.pojo.ShowActivityIbp;

/**
 * @author Abhishek Rudra
 *
 */
public interface ActivityService {

	public List<ShowActivity> fetchActivity(String objectType,Long objectId);
	
	public List<ShowActivityIbp> fetchActivityIbp(String objectType,Long objectId);
}
