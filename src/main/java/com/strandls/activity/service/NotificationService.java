package com.strandls.activity.service;

public interface NotificationService {
	
	public void sendNotification(String objectType, Long objectId, String title, String content);

}
