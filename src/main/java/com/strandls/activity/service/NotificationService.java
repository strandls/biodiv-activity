package com.strandls.activity.service;

import com.strandls.activity.pojo.ActivityLoggingData;

public interface NotificationService {
	
	public void sendNotification(ActivityLoggingData activity, String objectType, Long objectId, String title, String content);

}
