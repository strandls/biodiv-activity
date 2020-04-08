package com.strandls.activity.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.strandls.activity.RabbitMqConnection;
import com.strandls.activity.pojo.ActivityLoggingData;
import com.strandls.activity.pojo.CommentLoggingData;
import com.strandls.activity.pojo.RecoVoteActivity;
import com.strandls.activity.pojo.TaggedUser;
import com.strandls.activity.pojo.UserGroupActivity;
import com.strandls.activity.pojo.UserGroupMailData;
import com.strandls.activity.pojo.observationMailData;
import com.strandls.activity.service.MailService;
import com.strandls.activity.util.ActivityUtil;
import com.strandls.mail_utility.model.EnumModel.COMMENT_POST;
import com.strandls.mail_utility.model.EnumModel.FIELDS;
import com.strandls.mail_utility.model.EnumModel.MAIL_TYPE;
import com.strandls.mail_utility.model.EnumModel.POST_TO_GROUP;
import com.strandls.mail_utility.model.EnumModel.SUGGEST_MAIL;
import com.strandls.mail_utility.producer.RabbitMQProducer;
import com.strandls.mail_utility.util.JsonUtil;
import com.strandls.user.controller.UserServiceApi;
import com.strandls.user.pojo.Recipients;
import com.strandls.user.pojo.User;

public class MailServiceImpl implements MailService {

	private static final Logger logger = LoggerFactory.getLogger(MailServiceImpl.class);
	private String siteName = "";
	private String serverUrl = "";

	@Inject
	private RabbitMQProducer producer;

	@Inject
	private UserServiceApi userService;

	@Inject
	private ObjectMapper mapper;

	List<String> recommendationActivityList = new ArrayList<String>(
			Arrays.asList("obv unlocked", "Suggested species name", "obv locked", "Agreed on species name"));

	List<String> userGroupActivityList = new ArrayList<String>(
			Arrays.asList("Posted resource", "Removed resoruce", "Featured", "UnFeatured"));

	public MailServiceImpl() {
		Properties props = PropertyFileUtil.fetchProperty("config.properties");
		siteName = props.getProperty("portalName");
		serverUrl = props.getProperty("portalAddress");
	}

	@Override
	public void sendMail(MAIL_TYPE type, String objectType, Long objectId, Long userId, CommentLoggingData comment,
			ActivityLoggingData activity, List<TaggedUser> taggedUsers) {
		try {
			List<Recipients> recipientsList = userService.getRecipients(objectType, objectId);
			observationMailData observation = activity.getMailData().getObservationData();
			List<UserGroupMailData> groups = activity.getMailData().getUserGroupData();
			User who = userService.getUser(String.valueOf(userId));
			RecoVoteActivity reco = null;
			UserGroupActivity userGroup = null;
			String name = "";
			if (recommendationActivityList.contains(activity.getActivityType())
					|| activity.getActivityType().equalsIgnoreCase("suggestion removed")) {
				reco = mapper.readValue(activity.getActivityDescription(), RecoVoteActivity.class);

				name = (reco.getScientificName() != null && !reco.getScientificName().isEmpty())
						? reco.getScientificName()
						: (reco.getCommonName() != null && !reco.getCommonName().isEmpty())
							? reco.getCommonName()
									: (reco.getGivenName() != null && !reco.getGivenName().isEmpty()) ? reco.getGivenName() : "";
			}
			if (userGroupActivityList.contains(activity.getActivityType())) {
				userGroup = mapper.readValue(activity.getActivityDescription(), UserGroupActivity.class);
				System.out.println("***** UserGroup ***** " + userGroup.getUserGroupName());
			}
			if (groups != null && groups.size() > 0) {
				for (UserGroupMailData mailData: groups) {
					System.out.println("***** GroupFromAPI *****" + mailData.getName());
				}
			} else if (groups != null && groups.size() == 0) {
				for (UserGroupMailData mailData: groups) {
					System.out.println("***** GroupFromAPI *****" + mailData.getName());
				}				
			} else {
				System.out.println("***** Groups Empty *****");
			}
			
			Map<String, Object> data = null;
			String linkTaggedUsers = "";
			if (type == MAIL_TYPE.COMMENT_POST && taggedUsers != null) {
				linkTaggedUsers = ActivityUtil.linkTaggedUsersProfile(taggedUsers, comment.getBody(), true);
			}
			if (type == MAIL_TYPE.TAGGED_MAIL && taggedUsers != null && taggedUsers.size() > 0) {
				for (TaggedUser user : taggedUsers) {
					User follower = userService.getUser(String.valueOf(user.getId()));
					if (follower.getSendNotification() != null && follower.getSendNotification()) {
						Recipients recipient = new Recipients();
						recipient.setId(follower.getId());
						data = prepareMailData(type, recipient, follower, who, reco, userGroup, activity, comment, name,
								observation, groups, linkTaggedUsers);
						producer.produceMail(RabbitMqConnection.EXCHANGE, RabbitMqConnection.ROUTING_KEY, null,
								JsonUtil.mapToJSON(data));
					}
				}
			} else {
				for (Recipients recipient : recipientsList) {
					if (recipient.getIsSubscribed() != null && recipient.getIsSubscribed()) {
						User follower = userService.getUser(String.valueOf(recipient.getId()));
						data = prepareMailData(type, recipient, follower, who, reco, userGroup, activity, comment, name,
								observation, groups, linkTaggedUsers);
						producer.produceMail(RabbitMqConnection.EXCHANGE, RabbitMqConnection.ROUTING_KEY, null,
								JsonUtil.mapToJSON(data));
					}
				}
			}
			String admins = PropertyFileUtil.fetchProperty("config.properties", "mail_bcc");
			data.put(FIELDS.TO.getAction(), admins.split(","));
			producer.produceMail(RabbitMqConnection.EXCHANGE, RabbitMqConnection.ROUTING_KEY, null,
					JsonUtil.mapToJSON(data));
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage());
		}
	}

	private Map<String, Object> prepareMailData(MAIL_TYPE type, Recipients recipient, User follower, User who,
			RecoVoteActivity reco, UserGroupActivity userGroup, ActivityLoggingData activity,
			CommentLoggingData comment, String name, observationMailData observation, List<UserGroupMailData> groups, String modifiedComment) {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(FIELDS.TYPE.getAction(), type.getAction());
		data.put(FIELDS.TO.getAction(), new String[] { recipient.getEmail() });
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(COMMENT_POST.TYPE.getAction(), type.getAction());
		model.put(COMMENT_POST.SITENAME.getAction(), siteName);
		model.put(COMMENT_POST.SERVER_URL.getAction(), serverUrl);
		model.put(SUGGEST_MAIL.RECO_VOTE.getAction(), name);
		if (comment != null) {
			model.put(COMMENT_POST.COMMENT_BODY.getAction(), modifiedComment);
		}

		if (type == MAIL_TYPE.FACT_UPDATED || type == MAIL_TYPE.TAG_UPDATED || type == MAIL_TYPE.CUSTOM_FIELD_UPDATED
				|| type == MAIL_TYPE.FEATURED_POST || type == MAIL_TYPE.FEATURED_POST_IBP
				|| type == MAIL_TYPE.OBSERVATION_FLAGGED) {
			model.put(COMMENT_POST.COMMENT_BODY.getAction(), activity.getActivityDescription());
		}
		model.put(COMMENT_POST.FOLLOWER_ID.getAction(), follower.getId());
		model.put(COMMENT_POST.FOLLOWER_NAME.getAction(), follower.getName());
		model.put(COMMENT_POST.WHO_POSTED_ID.getAction(), who.getId());
		String icon = who.getIcon() != null ? who.getIcon() : "";
		if (!icon.isEmpty()) {
			int dot = icon.lastIndexOf(".");
			String fileName = icon.substring(0, dot);
			String extension = icon.substring(dot);
			icon = String.join("_gall_th", fileName, extension);
		}
		model.put(COMMENT_POST.WHO_POSTED_ICON.getAction(), icon.isEmpty() ? "user_large.png" : icon);
		model.put(COMMENT_POST.WHO_POSTED_NAME.getAction(),
				recipient.getId().equals(who.getId()) ? "You" : who.getName());
		if (reco != null) {
			model.put(SUGGEST_MAIL.GIVEN_NAME_ID.getAction(), reco.getSpeciesId() == null ? 0 : reco.getSpeciesId());
			model.put(SUGGEST_MAIL.GIVEN_NAME_NAME.getAction(), name);
			model.put(SUGGEST_MAIL.GIVEN_NAME_IS_SCIENTIFIC_NAME.getAction(),
					reco.getScientificName() != null && !reco.getScientificName().isEmpty());
		}
		model.put(COMMENT_POST.WHAT_POSTED_ID.getAction(), observation.getObservationId());
		model.put(COMMENT_POST.WHAT_POSTED_NAME.getAction(),
				(observation.getScientificName() != null && !observation.getScientificName().isEmpty())
				? observation.getScientificName()
				: (observation.getCommonName() != null && !observation.getCommonName().isEmpty())
					? observation.getCommonName()
							: "");
		model.put(COMMENT_POST.WHAT_POSTED_LOCATION.getAction(),
				observation.getLocation() == null ? "" : observation.getLocation());
		SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");		
		model.put(COMMENT_POST.WHAT_POSTED_OBSERVED_ON.getAction(), sdf.format(observation.getObservedOn()));
		String image = observation.getIconURl() == null ? "" : observation.getIconURl();
		if (!image.isEmpty()) {
			int dot = image.lastIndexOf(".");
			String fileName = image.substring(0, dot);
			String extension = image.substring(dot);
			image = String.join("_th1", fileName, extension);
		}
		model.put(COMMENT_POST.WHAT_POSTED_ICON.getAction(), image);
		model.put(COMMENT_POST.WHAT_POSTED_USERGROUPS.getAction(), groups);
		if (userGroup != null) {
			model.put(POST_TO_GROUP.WHERE_WEB_ADDRESS.getAction(), userGroup.getWebAddress());
			model.put(POST_TO_GROUP.WHERE_USER_GROUPNAME.getAction(), userGroup.getUserGroupName());
		}
		model.put(POST_TO_GROUP.SUBMIT_TYPE.getAction(),
				activity.getActivityType().toLowerCase().contains("post") ? "post" : "");
		data.put(FIELDS.DATA.getAction(), JsonUtil.unflattenJSON(model));
		return data;
	}
}
