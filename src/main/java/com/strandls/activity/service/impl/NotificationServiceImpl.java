package com.strandls.activity.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.rabbitmq.client.Channel;
import com.strandls.activity.RabbitMqConnection;
import com.strandls.activity.service.NotificationService;
import com.strandls.mail_utility.model.EnumModel.NOTIFICATION_DATA;
import com.strandls.mail_utility.model.EnumModel.NOTIFICATION_FIELDS;
import com.strandls.mail_utility.producer.RabbitMQProducer;
import com.strandls.mail_utility.util.JsonUtil;
import com.strandls.user.controller.UserServiceApi;
import com.strandls.user.pojo.Recipients;

public class NotificationServiceImpl implements NotificationService {

	@Inject
	private Channel channel;

	@Inject
	private UserServiceApi userService;

	@Override
	public void sendNotification(String objectType, Long objectId, String title, String content) {
		try {
			List<Recipients> recipients = userService.getRecipients(objectType, objectId);
			System.out.println("\n\n***** Log Recipients: " + recipients + " *****\n\n");
			for (Recipients recipient : recipients) {
				for (String token : recipient.getTokens()) {
					Map<String, Object> data = new HashMap<String, Object>();
					data.put(NOTIFICATION_FIELDS.TO.getAction(), token);
					Map<String, Object> notification = new HashMap<String, Object>();
					notification.put(NOTIFICATION_DATA.TITLE.getAction(), title);
					notification.put(NOTIFICATION_DATA.BODY.getAction(), content);
					data.put(NOTIFICATION_FIELDS.NOTIFICATION.getAction(), JsonUtil.unflattenJSON(notification));
					System.out.println("\n\n***** Json: " + JsonUtil.mapToJSON(data) + " *****\n\n");
					RabbitMQProducer producer = new RabbitMQProducer(channel);
					producer.produceNotification(RabbitMqConnection.EXCHANGE, RabbitMqConnection.NOTIFICATION_ROUTING_KEY, null,
							JsonUtil.mapToJSON(data));
				}
			}
		} catch (Exception ex) {
			System.out.println("\n\n***** Error in sendNotification *****\n\n");
			ex.printStackTrace();
		}
	}

}
