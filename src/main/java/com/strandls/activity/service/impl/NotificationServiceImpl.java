package com.strandls.activity.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import com.rabbitmq.client.Channel;
import com.strandls.activity.RabbitMqConnection;
import com.strandls.activity.pojo.ActivityLoggingData;
import com.strandls.activity.pojo.observationMailData;
import com.strandls.activity.service.NotificationService;
import com.strandls.mail_utility.model.EnumModel.COMMENT_POST;
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
	public void sendNotification(ActivityLoggingData activity, String objectType, Long objectId, String title,
			String content) {
		try {
			List<Recipients> recipients = userService.getRecipients(objectType, objectId);
			observationMailData observation = activity.getMailData().getObservationData();
			String image = observation.getIconURl() == null ? "" : observation.getIconURl();
			if (!image.isEmpty()) {
				int dot = image.lastIndexOf(".");
				String fileName = image.substring(0, dot);
				String extension = image.substring(dot);
				image = String.join("_th1", fileName, extension);
			}
			System.out.println("\n\n***** Log Recipients: " + recipients + " *****\n\n");
			Map<String, Object> data = new HashMap<String, Object>();
			Map<String, Object> notification = new HashMap<String, Object>();
			notification.put(NOTIFICATION_DATA.TITLE.getAction(), title);
			notification.put(NOTIFICATION_DATA.BODY.getAction(), content);
			if (!image.isEmpty()) {
				notification.put(NOTIFICATION_DATA.ICON.getAction(), image);
			}
			data.put(NOTIFICATION_FIELDS.NOTIFICATION.getAction(), JsonUtil.unflattenJSON(notification));
			for (Recipients recipient : recipients) {
				for (String token : recipient.getTokens()) {
					data.put(NOTIFICATION_FIELDS.TO.getAction(), token);
					RabbitMQProducer producer = new RabbitMQProducer(channel);
					producer.produceNotification(RabbitMqConnection.EXCHANGE,
							RabbitMqConnection.NOTIFICATION_ROUTING_KEY, null, JsonUtil.mapToJSON(data));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
