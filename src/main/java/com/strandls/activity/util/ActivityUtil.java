package com.strandls.activity.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.strandls.activity.pojo.TaggedUser;
import com.strandls.activity.service.impl.PropertyFileUtil;
import com.strandls.mail_utility.model.EnumModel.MAIL_TYPE;

public class ActivityUtil {
	
	private final static Logger logger = LoggerFactory.getLogger(ActivityUtil.class);
	
	private static final String TAGGED_USER_REGEX = "@\\[\\w+( \\w+)*\\]\\(\\d+\\)";

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
	
	public static String linkTaggedUsersProfile(List<TaggedUser> users, String commentBody) {
		String comment = commentBody;
		try {
			for (TaggedUser user: users) {
				String taggedUserLink = "<a href=\"*$URL$*\" target=\"_blank\">*$NAME$*</a>";
				String url = PropertyFileUtil.fetchProperty("config.properties", "portalAddress") + "/user/show/" + user.getId();
				taggedUserLink = taggedUserLink.replace("*$URL$*", url);
				taggedUserLink = taggedUserLink.replace("*$NAME$*", user.getName());
				comment = comment.replaceFirst(TAGGED_USER_REGEX, taggedUserLink);
			}
		} catch (Exception ex) {
			logger.error(ex.getMessage());
		}
		return comment;
	}

	public static MAIL_TYPE getMailType(String activity, boolean isUserGroup) {
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
			type = isUserGroup ? MAIL_TYPE.FEATURED_POST : MAIL_TYPE.FEATURED_POST_IBP;
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

		default:
			type = null;
			break;
		}
		return type;
	}

}
