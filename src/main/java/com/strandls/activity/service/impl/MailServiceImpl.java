package com.strandls.activity.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.rabbitmq.client.Channel;
import com.strandls.activity.RabbitMqConnection;
import com.strandls.activity.pojo.ActivityLoggingData;
import com.strandls.activity.pojo.CommentLoggingData;
import com.strandls.activity.pojo.UserGroupActivity;
import com.strandls.activity.service.MailService;
import com.strandls.mail_utility.model.EnumModel.COMMENT_POST;
import com.strandls.mail_utility.model.EnumModel.FIELDS;
import com.strandls.mail_utility.model.EnumModel.MAIL_TYPE;
import com.strandls.mail_utility.model.EnumModel.POST_TO_GROUP;
import com.strandls.mail_utility.model.EnumModel.SUGGEST_MAIL;
import com.strandls.mail_utility.producer.RabbitMQProducer;
import com.strandls.mail_utility.util.JsonUtil;
import com.strandls.observation.controller.ObservationServiceApi;
import com.strandls.observation.pojo.ObservationMailData;
import com.strandls.user.controller.UserServiceApi;
import com.strandls.user.pojo.Recipients;
import com.strandls.user.pojo.User;
import com.strandls.userGroup.controller.UserGroupSerivceApi;
import com.strandls.userGroup.pojo.UserGroupIbp;

public class MailServiceImpl implements MailService {

	private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);
	private String siteName = "";
	private String serverUrl = "";

	@Inject
	private Channel channel;

	@Inject
	private ObservationServiceApi observationService;

	@Inject
	private UserServiceApi userService;

	@Inject
	private UserGroupSerivceApi userGroupService;

	@Inject
	private ObjectMapper mapper;

	public MailServiceImpl() {
		Properties props = PropertyFileUtil.fetchProperty("config.properties");
		siteName = props.getProperty("portalName");
		serverUrl = props.getProperty("portalAddress");
	}

	@Override
	public void sendMail(MAIL_TYPE type, String objectType, Long objectId, Long userId, CommentLoggingData comment,
			ActivityLoggingData activity) {
		try {
			List<Recipients> recipientsList = userService.getRecipients(objectType, objectId);
			ObservationMailData observation = observationService.getObservationMailData(String.valueOf(objectId));
			List<UserGroupIbp> groups = userGroupService.getObservationUserGroup(String.valueOf(objectId));
			User who = userService.getUser(String.valueOf(userId));
			for (Recipients recipient : recipientsList) {
				if (recipient.getIsSubscribed() != null && recipient.getIsSubscribed()) {
					User follower = userService.getUser(String.valueOf(recipient.getId()));
					Map<String, Object> data = new HashMap<String, Object>();
					data.put(FIELDS.TYPE.getAction(), type.getAction());
					data.put(FIELDS.TO.getAction(), new String[] { recipient.getEmail() });
					Map<String, Object> model = new HashMap<String, Object>();
					model.put(COMMENT_POST.TYPE.getAction(), type.getAction());
					model.put(COMMENT_POST.SITENAME.getAction(), siteName);
					model.put(COMMENT_POST.SERVER_URL.getAction(), serverUrl);
					model.put(SUGGEST_MAIL.RECO_VOTE.getAction(),
							(observation.getScientificName() != null || !observation.getScientificName().isEmpty())
									? observation.getScientificName()
									: observation.getCommonName());
					if (comment != null) {
						model.put(COMMENT_POST.COMMENT_BODY.getAction(), comment.getBody());
					}
					model.put(COMMENT_POST.FOLLOWER_ID.getAction(), follower.getId());
					model.put(COMMENT_POST.FOLLOWER_NAME.getAction(), follower.getName());
					model.put(COMMENT_POST.WHO_POSTED_ID.getAction(), who.getId());
					model.put(COMMENT_POST.WHO_POSTED_ICON.getAction(), who.getIcon());
					model.put(COMMENT_POST.WHO_POSTED_NAME.getAction(), who.getName());
					model.put(SUGGEST_MAIL.GIVEN_NAME_ID.getAction(), "");
					model.put(SUGGEST_MAIL.GIVEN_NAME_NAME.getAction(), "");
					model.put(COMMENT_POST.WHAT_POSTED_ID.getAction(), observation.getObservationId());
					model.put(COMMENT_POST.WHAT_POSTED_NAME.getAction(), observation.getCommonName());
					model.put(COMMENT_POST.WHAT_POSTED_LOCATION.getAction(), observation.getLocation());
					model.put(COMMENT_POST.WHAT_POSTED_OBSERVED_ON.getAction(), observation.getObservedOn());
					model.put(COMMENT_POST.WHAT_POSTED_ICON.getAction(), observation.getIconURl());
					model.put(COMMENT_POST.WHAT_POSTED_USERGROUPS.getAction(), groups);
					model.put(POST_TO_GROUP.WHERE_POSTED.getAction(), groups);
					if (activity != null) {
						UserGroupActivity groupActivity = mapper.readValue(activity.getActivityDescription(),
								UserGroupActivity.class);
					}
					data.put(FIELDS.DATA.getAction(), JsonUtil.unflattenJSON(model));
					RabbitMQProducer producer = new RabbitMQProducer(channel);
					producer.produceMail(RabbitMqConnection.EXCHANGE, RabbitMqConnection.ROUTING_KEY, null,
							JsonUtil.mapToJSON(data));
				}
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
	}

}