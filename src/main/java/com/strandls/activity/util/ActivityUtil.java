package com.strandls.activity.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.activity.pojo.ActivityLoggingData;
import com.strandls.activity.pojo.TaggedUser;
import com.strandls.activity.pojo.UserGroupActivity;
import com.strandls.activity.service.impl.PropertyFileUtil;
import com.strandls.mail_utility.model.EnumModel.MAIL_TYPE;

public class ActivityUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(ActivityUtil.class);
	
	private static final String TAGGED_USER_REGEX = "@\\[(.*?)\\]\\(\\d+\\)";
	
	private static final Map<String, String> flaggedMessages = new HashMap<String, String>();
	
	static {
		flaggedMessages.put("DETAILS_INAPPROPRIATE", "Details Inapppropriate");
		flaggedMessages.put("LOCATION_INAPPROPRIATE", "Location Inapppropriate");
		flaggedMessages.put("DATE_INAPPROPRIATE", "Date Inapppropriate");
	}	

	public static List<TaggedUser> getTaggedUsers(String comment) {
		List<TaggedUser> users = new ArrayList<TaggedUser>();
		Pattern pattern = Pattern.compile(TAGGED_USER_REGEX);
		try {
			Matcher matcher = pattern.matcher(comment);
			while (matcher.find()) {
				String match = matcher.group();
				TaggedUser user = new TaggedUser();
				user.setName(match.substring(match.indexOf("[") + 1, match.lastIndexOf("]")));
				user.setId(Long.parseLong(match.substring(match.indexOf("(") + 1, match.lastIndexOf(")"))));
				users.add(user);
			}			
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return users;
	}
	
	public static String linkTaggedUsersProfile(List<TaggedUser> users, String commentBody, boolean withURL) {
		String comment = commentBody;
		try {
			for (TaggedUser user: users) {
				String taggedUserLink = null;
				if (withURL) {
					taggedUserLink = "<a href=\"*$URL$*\" target=\"_blank\">*$NAME$*</a>";
					String url = PropertyFileUtil.fetchProperty("config.properties", "portalAddress") + "/user/show/" + user.getId();
					taggedUserLink = taggedUserLink.replace("*$URL$*", url);
				} else {
					taggedUserLink = "*$NAME$*";					
				}
				taggedUserLink = taggedUserLink.replace("*$NAME$*", user.getName());
				comment = comment.replaceFirst(TAGGED_USER_REGEX, taggedUserLink);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return comment;
	}
	
	public static String replaceFlaggedMessage(String message) {
		for (Map.Entry<String, String> m: flaggedMessages.entrySet()) {
			if (message.contains(m.getKey())) {
				message = message.replaceAll(m.getKey(), m.getValue());
			}
		}
		return message;
	}

	public static MAIL_TYPE getMailType(String activity, ActivityLoggingData loggingData) {
		boolean featuredToIBP = false;
		try {
			ObjectMapper mapper = new ObjectMapper();
			UserGroupActivity data = mapper.readValue(loggingData.getActivityDescription(), UserGroupActivity.class);
			featuredToIBP = (data.getUserGroupId() == null);
		} catch (Exception ex) {
			ex.printStackTrace();
			logger.error(ex.getMessage());			
		}
		MAIL_TYPE type = null;
		switch (activity) {
		case "Observation created":
			type = MAIL_TYPE.OBSERVATION_ADDED;
			break;
		case "Observation updated":
			type = MAIL_TYPE.OBSERVATION_UPDATED;
			break;
		case "obv unlocked":
			type = MAIL_TYPE.OBSERVATION_UNLOCKED;
			break;
		case "Suggested species name":
			type = MAIL_TYPE.SUGGEST_MAIL;
			break;
		case "obv locked":
			type = MAIL_TYPE.OBSERVATION_LOCKED;
			break;
		case "Agreed on species name":
			type = MAIL_TYPE.AGREED_SPECIES;
			break;
		case "Posted resource":
			type = MAIL_TYPE.POST_TO_GROUP;
			break;
		case "Removed resoruce":
			type = MAIL_TYPE.POST_TO_GROUP;
			break;
		case "Featured":
			type = !featuredToIBP ? MAIL_TYPE.FEATURED_POST : MAIL_TYPE.FEATURED_POST_IBP;
			break;
		case "Updated fact":
			type = MAIL_TYPE.FACT_UPDATED;
			break;
		case "Flagged":
			type = MAIL_TYPE.OBSERVATION_FLAGGED;
			break;
		case "Suggestion removed":
			type = MAIL_TYPE.REMOVED_SPECIES;
			break;
		case "Observation tag updated":
			type = MAIL_TYPE.TAG_UPDATED;
			break;
		case "Custom field edited":
			type = MAIL_TYPE.CUSTOM_FIELD_UPDATED;
			break;
		case "Observation species group updated":
			type = null;
			break;
		case "Added a comment":
			type = MAIL_TYPE.COMMENT_POST;
			break;
		case "Observation Deleted":
			type = MAIL_TYPE.OBSERVATION_DELETED;
			break;
		case "Rated media resource":
			type = MAIL_TYPE.RATED_MEDIA_RESOURCE;
			break;
			
		default:
			type = null;
			break;
		}
		return type;
	}
	
	public static String getFormattedDate(String date) {
		String[] d = date.split(" ");
		return String.join(" ", Integer.parseInt(d[0]) + getDateSuffix(d[0]), d[1], d[2]);
	}
	
	private static String getDateSuffix(String date) {
		try {
			int d = Integer.parseInt(date);
			switch (d % 10) {
			case 1: return "st";
			case 2: return "nd";
			case 3: return "rd";
			default: return "th";
			}			
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return "";
	}

}
