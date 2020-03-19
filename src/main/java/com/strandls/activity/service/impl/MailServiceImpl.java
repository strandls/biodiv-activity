package com.strandls.activity.service.impl;

import java.util.Arrays;
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
import com.strandls.activity.pojo.RecoVoteActivity;
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
			RecoVoteActivity reco = null;
			UserGroupActivity userGroup = null;
			String name = "";
			try {
				reco = mapper.readValue(activity.getActivityDescription(), RecoVoteActivity.class);

				name = (reco.getScientificName() != null || !reco.getScientificName().isEmpty())
						? reco.getScientificName()
						: reco.getCommonName();
						
				userGroup = mapper.readValue(activity.getActivityDescription(), UserGroupActivity.class);						
			} catch (Exception ex) {
				ex.printStackTrace();
				logger.error(ex.getMessage());				
			}
			for (Recipients recipient : recipientsList) {
				if (recipient.getIsSubscribed() != null && recipient.getIsSubscribed()) {
					User follower = userService.getUser(String.valueOf(recipient.getId()));
					Map<String, Object> data = new HashMap<String, Object>();
					data.put(FIELDS.TYPE.getAction(), type.getAction());
					data.put(FIELDS.TO.getAction(),
							PropertyFileUtil.fetchProperty("config.properties", "temp_email").split(","));
					Map<String, Object> model = new HashMap<String, Object>();
					model.put(COMMENT_POST.TYPE.getAction(), type.getAction());
					model.put(COMMENT_POST.SITENAME.getAction(), siteName);
					model.put(COMMENT_POST.SERVER_URL.getAction(), serverUrl);
					model.put(SUGGEST_MAIL.RECO_VOTE.getAction(), name);
					if (comment != null) {
						model.put(COMMENT_POST.COMMENT_BODY.getAction(), comment.getBody());
					}
					model.put(COMMENT_POST.FOLLOWER_ID.getAction(), follower.getId());
					model.put(COMMENT_POST.FOLLOWER_NAME.getAction(), follower.getName());
					model.put(COMMENT_POST.WHO_POSTED_ID.getAction(), who.getId());
					model.put(COMMENT_POST.WHO_POSTED_ICON.getAction(), who.getIcon() == null ? "" : who.getIcon());
					model.put(COMMENT_POST.WHO_POSTED_NAME.getAction(), who.getId() == userId ? "You" : who.getName());
					if (reco != null) {
						model.put(SUGGEST_MAIL.GIVEN_NAME_ID.getAction(),
								reco.getSpeciesId() == null ? 0 : reco.getSpeciesId());
						model.put(SUGGEST_MAIL.GIVEN_NAME_NAME.getAction(), name);
						model.put(SUGGEST_MAIL.GIVEN_NAME_IS_SCIENTIFIC_NAME.getAction(),
								reco.getScientificName() != null || !reco.getScientificName().isEmpty());
					}
					model.put(COMMENT_POST.WHAT_POSTED_ID.getAction(), observation.getObservationId());
					model.put(COMMENT_POST.WHAT_POSTED_NAME.getAction(), observation.getCommonName());
					model.put(COMMENT_POST.WHAT_POSTED_LOCATION.getAction(), observation.getLocation());
					model.put(COMMENT_POST.WHAT_POSTED_OBSERVED_ON.getAction(), observation.getObservedOn());
					model.put(COMMENT_POST.WHAT_POSTED_ICON.getAction(),
							observation.getIconURl() == null ? "" : observation.getIconURl());
					model.put(COMMENT_POST.WHAT_POSTED_USERGROUPS.getAction(), groups);
					model.put(POST_TO_GROUP.WHERE_WEB_ADDRESS.getAction(), userGroup.getWebAddress());
					model.put(POST_TO_GROUP.WHERE_USER_GROUPNAME.getAction(), userGroup.getUserGroupName());
					model.put(POST_TO_GROUP.SUBMIT_TYPE.getAction(),
							activity.getActivityType().toLowerCase().contains("post") ? "post": "");
					data.put(FIELDS.DATA.getAction(), JsonUtil.unflattenJSON(model));
					RabbitMQProducer producer = new RabbitMQProducer(channel);
					producer.produceMail(RabbitMqConnection.EXCHANGE, RabbitMqConnection.ROUTING_KEY, null,
							JsonUtil.mapToJSON(data));
				}
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage());
		}
	}

}
