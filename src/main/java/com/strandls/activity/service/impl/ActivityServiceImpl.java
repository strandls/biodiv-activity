/**
 * 
 */
package com.strandls.activity.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strandls.activity.ActivityEnums;
import com.strandls.activity.Headers;
import com.strandls.activity.dao.ActivityDao;
import com.strandls.activity.dao.CommentsDao;
import com.strandls.activity.pojo.Activity;
import com.strandls.activity.pojo.ActivityIbp;
import com.strandls.activity.pojo.ActivityLoggingData;
import com.strandls.activity.pojo.ActivityResult;
import com.strandls.activity.pojo.CommentLoggingData;
import com.strandls.activity.pojo.Comments;
import com.strandls.activity.pojo.CommentsIbp;
import com.strandls.activity.pojo.DocumentActivityLogging;
import com.strandls.activity.pojo.MailActivityData;
import com.strandls.activity.pojo.MyJson;
import com.strandls.activity.pojo.RecoVoteActivity;
import com.strandls.activity.pojo.ShowActivityIbp;
import com.strandls.activity.pojo.SpeciesActivityLogging;
import com.strandls.activity.pojo.TaggedUser;
import com.strandls.activity.pojo.UserGroupActivity;
import com.strandls.activity.pojo.UserGroupActivityLogging;
import com.strandls.activity.service.ActivityService;
import com.strandls.activity.service.MailService;
import com.strandls.activity.service.NotificationService;
import com.strandls.activity.util.ActivityUtil;
import com.strandls.mail_utility.model.EnumModel.MAIL_TYPE;
import com.strandls.user.controller.UserServiceApi;
import com.strandls.user.pojo.UserIbp;

/**
 * @author Abhishek Rudra
 *
 */
public class ActivityServiceImpl implements ActivityService {

	private final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);

	@Inject
	private ObjectMapper objectMapper;

	@Inject
	private ActivityDao activityDao;

	@Inject
	private CommentsDao commentsDao;

	@Inject
	private UserServiceApi userService;

	@Inject
	private NotificationService notificationSevice;

	@Inject
	private MailService mailService;

	@Inject
	private Headers headers;

	List<String> userGroupActivities = new ArrayList<String>(
			Arrays.asList("Posted resource", "Removed resoruce", "Featured", "UnFeatured"));

	List<String> recoActivities = new ArrayList<String>(Arrays.asList("obv unlocked", "Suggested species name",
			"obv locked", "Agreed on species name", "Suggestion removed"));

//	OBSERVATION ACTIVITY LIST
	List<String> obvNullActivityList = new ArrayList<String>(
			Arrays.asList("Observation created", "Observation updated", "Rated media resource", "Observation Deleted"));

	List<String> obvRecommendationActivityList = new ArrayList<String>(
			Arrays.asList("obv unlocked", "Suggested species name", "obv locked", "Agreed on species name"));

	List<String> obvUserGroupActivityList = new ArrayList<String>(
			Arrays.asList("Posted resource", "Removed resoruce", "Featured", "UnFeatured"));

	List<String> obvTraitsActivityList = new ArrayList<String>(Arrays.asList("Updated fact", "Added a fact"));

	List<String> obvFlagActivityList = new ArrayList<String>(Arrays.asList("Flag removed", "Flagged"));

	List<String> commentActivityList = new ArrayList<String>(Arrays.asList("Added a comment"));

	List<String> observationActivityList = new ArrayList<String>(Arrays.asList("Featured", "Suggestion removed",
			"Observation tag updated", "Custom field edited", "UnFeatured", "Observation species group updated"));

//	USERGROUP ACTIVITY LIST

	List<String> ugNullActivityList = new ArrayList<String>(Arrays.asList("Group updated", "Group created"));

	List<String> userGroupActivityList = new ArrayList<String>(Arrays.asList("Removed resoruce", "Posted resource"));

	List<String> ugUserActivityList = new ArrayList<String>(Arrays.asList("Joined group", "Left Group", "Role updated",
			"Invitation Sent", "Removed user", "Requested Join"));

	List<String> ugCustomFieldActivityList = new ArrayList<String>(
			Arrays.asList("Added Custom Field", "Removed Custom Field"));

	List<String> ugFilterRuleActivityList = new ArrayList<String>(
			Arrays.asList("Added Filter Rule", "Removed Filter Rule", "Disabled Filter Rule", "Enabled Filter Rule"));

//	DOCUMENT ACTIVITY LIST 

	List<String> docNullActivityList = new ArrayList<String>(
			Arrays.asList("Document created", "Document updated", "Document Deleted"));

	List<String> docFlagActivityList = new ArrayList<String>(Arrays.asList("Flag removed", "Flagged"));

	List<String> docUserGroupActivityList = new ArrayList<String>(
			Arrays.asList("Featured", "Posted resource", "Removed resoruce", "UnFeatured"));

	List<String> documentActivityList = new ArrayList<String>(
			Arrays.asList("Document tag updated", "Featured", "UnFeatured"));

	List<String> docCommentActivityList = new ArrayList<String>(Arrays.asList("Added a comment"));

//	SPECIES ACTIVITY LIST

	List<String> speciesNullActivityList = new ArrayList<String>(Arrays.asList("Created species", "Added synonym",
			"Updated synonym", "Deleted synonym", "Added common name", "Updated common name", "Deleted common name"));

	List<String> speciesActivityList = new ArrayList<String>(
			Arrays.asList("Featured", "UnFeatured", "Updated species gallery"));

	List<String> speciesFieldActivityList = new ArrayList<String>(
			Arrays.asList("Added species field", "Updated species field", "Deleted species field"));

	List<String> speciesTaxonomyActivityList = new ArrayList<String>(
			Arrays.asList("Added hierarchy", "Deleted hierarchy"));

	List<String> speciesTraitActivityList = new ArrayList<String>(Arrays.asList("Added a fact", "Updated fact"));

	List<String> speciesCommentActivityList = new ArrayList<String>(Arrays.asList("Added a comment"));

	List<String> speciesUserGroupActivityList = new ArrayList<String>(
			Arrays.asList("Featured", "UnFeatured", "Posted resource", "Removed resoruce"));

	@Override
	public Integer activityCount(String objectType, Long objectId) {
		if (objectType.equalsIgnoreCase("observation"))
			objectType = ActivityEnums.observation.getValue();
		Integer count = activityDao.findActivityCountByObjectId(objectType, objectId);
		return count;
	}

	@Override
	public ActivityResult fetchActivityIbp(String objectType, Long objectId, String offset, String limit) {

		if (objectType.equalsIgnoreCase("observation"))
			objectType = ActivityEnums.observation.getValue();
		else if (objectType.equalsIgnoreCase("document"))
			objectType = ActivityEnums.document.getValue();
		else if (objectType.equalsIgnoreCase("usergroup"))
			objectType = ActivityEnums.userGroup.getValue();
		else if (objectType.equalsIgnoreCase("species"))
			objectType = ActivityEnums.species.getValue();

		List<ShowActivityIbp> ibpActivity = new ArrayList<ShowActivityIbp>();
		Integer commentCount = 0;
		ActivityResult activityResult = null;

		try {
			List<Activity> activites = activityDao.findByObjectId(objectType, objectId, offset, limit);
			commentCount = activityDao.findCommentCount(objectType, objectId);
			for (Activity activity : activites) {

				UserGroupActivity ugActivity = null;
				RecoVoteActivity recoVoteActivity = null;
				Comments comment = null;
				Comments reply = null;
				CommentsIbp commentIbp = null;
				CommentsIbp replyIbp = null;
				ActivityIbp activityIbp = null;

				if (commentActivityList.contains(activity.getActivityType())) {

					if (activity.getActivityHolderId().equals(activity.getSubRootHolderId())) {
						comment = commentsDao.findById(activity.getActivityHolderId());
						commentIbp = new CommentsIbp(comment.getBody());

					} else {
						reply = commentsDao.findById(activity.getSubRootHolderId());
						comment = commentsDao.findById(activity.getActivityHolderId());
						replyIbp = new CommentsIbp(comment.getBody());
						commentIbp = new CommentsIbp(reply.getBody());
					}

				} else if (userGroupActivities.contains(activity.getActivityType())) {
					String description = activity.getActivityDescription();
					ugActivity = objectMapper.readValue(description, UserGroupActivity.class);
				} else if (recoActivities.contains(activity.getActivityType())) {
					String description = activity.getActivityDescription();
					recoVoteActivity = objectMapper.readValue(description, RecoVoteActivity.class);

				}
				activityIbp = new ActivityIbp(activity.getActivityDescription(), activity.getActivityType(),
						activity.getDateCreated(), activity.getLastUpdated());

				UserIbp user = userService.getUserIbp(activity.getAuthorId().toString());
				ibpActivity.add(
						new ShowActivityIbp(activityIbp, commentIbp, replyIbp, ugActivity, recoVoteActivity, user));

			}
			activityResult = new ActivityResult(ibpActivity, commentCount);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return activityResult;
	}

//	OBSERVATION ACTIVITY LOGGING

	@Override
	public Activity logActivities(HttpServletRequest request, Long userId, ActivityLoggingData loggingData) {
		Activity activity = null;
		MAIL_TYPE type = null;
		System.out.println("\n\n***** LoggingData: " + loggingData.getActivityType() + " *****\n\n");

		Boolean isUsergroupFeatured = false;
		isUsergroupFeatured = checkUserGroupFeatured(loggingData.getActivityType(),
				loggingData.getActivityDescription());

		if (obvNullActivityList.contains(loggingData.getActivityType())) {
			activity = new Activity(null, 0L, null, null, null, null, loggingData.getActivityType(), userId, new Date(),
					new Date(), loggingData.getRootObjectId(), ActivityEnums.observation.getValue(),
					loggingData.getRootObjectId(), ActivityEnums.observation.getValue(), true, null);

		} else if (obvRecommendationActivityList.contains(loggingData.getActivityType())) {

			activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
					ActivityEnums.recommendationVote.getValue(), null, loggingData.getActivityType(), userId,
					new Date(), new Date(), loggingData.getRootObjectId(), ActivityEnums.observation.getValue(),
					loggingData.getRootObjectId(), ActivityEnums.observation.getValue(), true, null);
		} else if (obvUserGroupActivityList.contains(loggingData.getActivityType()) && isUsergroupFeatured) {
			activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
					ActivityEnums.userGroup.getValue(), null, loggingData.getActivityType(), userId, new Date(),
					new Date(), loggingData.getRootObjectId(), ActivityEnums.observation.getValue(),
					loggingData.getRootObjectId(), ActivityEnums.observation.getValue(), true, null);
		} else if (obvTraitsActivityList.contains(loggingData.getActivityType())) {
			activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
					ActivityEnums.facts.getValue(), null, loggingData.getActivityType(), userId, new Date(), new Date(),
					loggingData.getRootObjectId(), ActivityEnums.observation.getValue(), loggingData.getRootObjectId(),
					ActivityEnums.observation.getValue(), true, null);
		} else if (obvFlagActivityList.contains(loggingData.getActivityType())) {
			MyJson myJson = new MyJson();
			String[] description = loggingData.getActivityDescription().split(":");
			String desc = description[0] + "\n" + description[1];
			myJson.setAid(loggingData.getActivityId());
			myJson.setDescription(desc);
			activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
					ActivityEnums.flag.getValue(), null, loggingData.getActivityType(), userId, new Date(), new Date(),
					loggingData.getRootObjectId(), ActivityEnums.observation.getValue(), loggingData.getRootObjectId(),
					ActivityEnums.observation.getValue(), true, myJson);

		} else if (observationActivityList.contains(loggingData.getActivityType())) {
			activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
					ActivityEnums.observation.getValue(), null, loggingData.getActivityType(), userId, new Date(),
					new Date(), loggingData.getRootObjectId(), ActivityEnums.observation.getValue(),
					loggingData.getRootObjectId(), ActivityEnums.observation.getValue(), true, null);
		} else if (commentActivityList.contains(loggingData.getActivityType())) {
			activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
					ActivityEnums.comments.getValue(), null, loggingData.getActivityType(), userId, new Date(),
					new Date(), loggingData.getRootObjectId(), ActivityEnums.observation.getValue(),
					loggingData.getSubRootObjectId(), ActivityEnums.comments.getValue(), true, null);
		}

		Activity result = activityDao.save(activity);
		try {
			userService = headers.addUserHeader(userService, request.getHeader(HttpHeaders.AUTHORIZATION));
			userService.updateFollow("observation", loggingData.getRootObjectId().toString());
			if (loggingData.getMailData() != null) {
				Map<String, Object> data = ActivityUtil.getMailType(activity.getActivityType(), loggingData);
				System.out.println("\n\n***** Activity Type Data: " + data + " *****\n\n");
				type = (MAIL_TYPE) data.get("type");
				if (type != null && type != MAIL_TYPE.COMMENT_POST) {
					MailActivityData mailActivityData = new MailActivityData(loggingData.getActivityType(),
							loggingData.getActivityDescription(), loggingData.getMailData());
					mailService.sendMail(type, result.getRootHolderType(), result.getRootHolderId(), userId, null,
							mailActivityData, null);
					notificationSevice.sendNotification(mailActivityData, result.getRootHolderType(),
							result.getRootHolderId(), "India Biodiversity Portal", data.get("text").toString());
				}
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return result;

	}

	@Override
	public String sendObvCreateMail(Long userId, ActivityLoggingData loggingData) {
		try {
			MailActivityData mailActivityData = new MailActivityData(loggingData.getActivityType(),
					loggingData.getActivityDescription(), loggingData.getMailData());
			mailService.sendMail(MAIL_TYPE.OBSERVATION_ADDED, ActivityEnums.observation.getValue(),
					loggingData.getRootObjectId(), userId, null, mailActivityData, null);
			notificationSevice.sendNotification(mailActivityData, ActivityEnums.observation.getValue(),
					loggingData.getRootObjectId(), "India Biodiversity Portal", "Observation created");
			return "Mail Sent";
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return "Mail not sent";
	}

	@Override
	public Activity addComment(HttpServletRequest request, Long userId, String commentType,
			CommentLoggingData commentData) {

		if (commentData.getSubRootHolderId() == null) {
			commentData.setSubRootHolderId(commentData.getRootHolderId());
			commentData.setSubRootHolderType(commentData.getRootHolderType());
		}
		commentData.setSubRootHolderType(ActivityEnums.valueOf(commentData.getSubRootHolderType()).getValue());
		commentData.setRootHolderType(ActivityEnums.valueOf(commentData.getRootHolderType()).getValue());
		Comments comment = null;
		if (commentData.getRootHolderId().equals(commentData.getSubRootHolderId())) {
			comment = new Comments(null, 0L, userId, commentData.getBody(), commentData.getSubRootHolderId(),
					commentData.getSubRootHolderType(), new Date(), new Date(), commentData.getRootHolderId(),
					commentData.getRootHolderType(), null, null, null, 205L);

		} else {
			comment = new Comments(null, 0L, userId, commentData.getBody(), commentData.getSubRootHolderId(),
					commentData.getSubRootHolderType(), new Date(), new Date(), commentData.getRootHolderId(),
					commentData.getRootHolderType(), commentData.getSubRootHolderId(), commentData.getSubRootHolderId(),
					null, 205L);

		}

		Comments result = commentsDao.save(comment);

		Activity activityResult = null;
		if (commentType.equals("observation")) {
			ActivityLoggingData activity = null;
			if (result.getCommentHolderId().equals(result.getRootHolderId())) {
				activity = new ActivityLoggingData(null, result.getRootHolderId(), result.getId(),
						result.getRootHolderType(), result.getId(), "Added a comment", commentData.getMailData());
			} else {
				activity = new ActivityLoggingData(null, result.getRootHolderId(), result.getCommentHolderId(),
						result.getRootHolderType(), result.getId(), "Added a comment", commentData.getMailData());
			}
			activityResult = logActivities(request, userId, activity);

		} else if (commentType.equals("document")) {

			DocumentActivityLogging loggingData = null;
			if (result.getCommentHolderId().equals(result.getRootHolderId())) {
				loggingData = new DocumentActivityLogging(null, result.getRootHolderId(), result.getId(),
						result.getRootHolderType(), result.getId(), "Added a comment", commentData.getMailData());

			} else {
				loggingData = new DocumentActivityLogging(null, result.getRootHolderId(), result.getCommentHolderId(),
						result.getRootHolderType(), result.getId(), "Added a comment", commentData.getMailData());
			}

			activityResult = logDocActivities(request, userId, loggingData);
		} else if (commentType.equalsIgnoreCase("species")) {
			SpeciesActivityLogging loggingData = null;
			if (result.getCommentHolderId().equals(result.getRootHolderId())) {
				loggingData = new SpeciesActivityLogging(null, result.getRootHolderId(), result.getId(),
						result.getRootHolderType(), result.getId(), "Added a comment", commentData.getMailData());
			} else {
				loggingData = new SpeciesActivityLogging(null, result.getRootHolderId(), result.getCommentHolderId(),
						result.getRootHolderType(), result.getId(), "Added a comment", commentData.getMailData());
			}
			activityResult = logSpeciesActivities(request, userId, loggingData);
		}

		if (activityResult != null) {
			if (commentData.getMailData() != null) {
				MailActivityData mailActivityData = new MailActivityData("Added a comment", null,
						commentData.getMailData());
				List<TaggedUser> taggedUsers = ActivityUtil.getTaggedUsers(commentData.getBody());
				if (taggedUsers.size() > 0) {
					mailService.sendMail(MAIL_TYPE.TAGGED_MAIL, activityResult.getRootHolderType(),
							activityResult.getRootHolderId(), userId, commentData, mailActivityData, taggedUsers);
				}
				mailService.sendMail(MAIL_TYPE.COMMENT_POST, activityResult.getRootHolderType(),
						activityResult.getRootHolderId(), userId, commentData, mailActivityData, taggedUsers);
				notificationSevice.sendNotification(mailActivityData, result.getRootHolderType(),
						result.getRootHolderId(), "India Biodiversity Portal", mailActivityData.getActivityType());

			}
		}

		return activityResult;
	}

//	USERGROUP ACTIVITY LOGGING

	@Override
	public Activity logUGActivities(Long userId, UserGroupActivityLogging loggingData) {

		Activity activity = null;
		if (ugNullActivityList.contains(loggingData.getActivityType())) {
			activity = new Activity(null, 0L, null, null, null, null, loggingData.getActivityType(), userId, new Date(),
					new Date(), loggingData.getRootObjectId(), ActivityEnums.userGroup.getValue(),
					loggingData.getSubRootObjectId(), ActivityEnums.userGroup.getValue(), true, null);

		} else if (userGroupActivityList.contains(loggingData.getActivityType())) {
			activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
					ActivityEnums.userGroup.getValue(), null, loggingData.getActivityType(), userId, new Date(),
					new Date(), loggingData.getRootObjectId(), ActivityEnums.userGroup.getValue(),
					loggingData.getSubRootObjectId(), ActivityEnums.userGroup.getValue(), true, null);

		} else if (ugUserActivityList.contains(loggingData.getActivityType())) {
			activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
					ActivityEnums.user.getValue(), null, loggingData.getActivityType(), userId, new Date(), new Date(),
					loggingData.getRootObjectId(), ActivityEnums.userGroup.getValue(), loggingData.getSubRootObjectId(),
					ActivityEnums.userGroup.getValue(), true, null);

		} else if (ugCustomFieldActivityList.contains(loggingData.getActivityType())) {
			activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
					ActivityEnums.customField.getValue(), null, loggingData.getActivityType(), userId, new Date(),
					new Date(), loggingData.getRootObjectId(), ActivityEnums.userGroup.getValue(),
					loggingData.getSubRootObjectId(), ActivityEnums.userGroup.getValue(), true, null);
		} else if (ugFilterRuleActivityList.contains(loggingData.getActivityType())) {

			activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
					ActivityEnums.filterRule.getValue(), null, loggingData.getActivityType(), userId, new Date(),
					new Date(), loggingData.getRootObjectId(), ActivityEnums.userGroup.getValue(),
					loggingData.getSubRootObjectId(), ActivityEnums.userGroup.getValue(), true, null);
		}

		if (activity != null)
			activity = activityDao.save(activity);

		return activity;
	}

//	DOCUMENT ACTIVITY LOGGING

	@Override
	public Activity logDocActivities(HttpServletRequest request, Long userId, DocumentActivityLogging loggingData) {
		try {
			Activity activity = null;
			Boolean isUsergroupFeatured = false;
			isUsergroupFeatured = checkUserGroupFeatured(loggingData.getActivityType(),
					loggingData.getActivityDescription());

			if (docNullActivityList.contains(loggingData.getActivityType())) {
				activity = new Activity(null, 0L, null, null, null, null, loggingData.getActivityType(), userId,
						new Date(), new Date(), loggingData.getRootObjectId(), ActivityEnums.document.getValue(),
						loggingData.getSubRootObjectId(), ActivityEnums.document.getValue(), true, null);

			} else if (docFlagActivityList.contains(loggingData.getActivityType())) {
				activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
						ActivityEnums.flag.getValue(), null, loggingData.getActivityType(), userId, new Date(),
						new Date(), loggingData.getRootObjectId(), ActivityEnums.document.getValue(),
						loggingData.getSubRootObjectId(), ActivityEnums.document.getValue(), true, null);

			} else if (docCommentActivityList.contains(loggingData.getActivityType())) {

				activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
						ActivityEnums.comments.getValue(), null, loggingData.getActivityType(), userId, new Date(),
						new Date(), loggingData.getRootObjectId(), ActivityEnums.document.getValue(),
						loggingData.getSubRootObjectId(), ActivityEnums.comments.getValue(), true, null);

			} else if (docUserGroupActivityList.contains(loggingData.getActivityType()) && isUsergroupFeatured) {
				activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
						ActivityEnums.userGroup.getValue(), null, loggingData.getActivityType(), userId, new Date(),
						new Date(), loggingData.getRootObjectId(), ActivityEnums.document.getValue(),
						loggingData.getSubRootObjectId(), ActivityEnums.document.getValue(), true, null);

			} else if (documentActivityList.contains(loggingData.getActivityType())) {
				activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
						ActivityEnums.document.getValue(), null, loggingData.getActivityType(), userId, new Date(),
						new Date(), loggingData.getRootObjectId(), ActivityEnums.document.getValue(),
						loggingData.getSubRootObjectId(), ActivityEnums.document.getValue(), true, null);

			}

			if (activity != null)
				activity = activityDao.save(activity);

			try {
				userService = headers.addUserHeader(userService, request.getHeader(HttpHeaders.AUTHORIZATION));
				userService.updateFollow("document", loggingData.getRootObjectId().toString());
//				TODO add mail service for document

			} catch (Exception e) {
				logger.error(e.getMessage());
			}
			return activity;

		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

//	SPECIES ACTIVITY LOGGING
	@Override
	public Activity logSpeciesActivities(HttpServletRequest request, Long userId, SpeciesActivityLogging loggingData) {
		try {

			Activity activity = null;
			Boolean isUsergroupFeatured = false;

			isUsergroupFeatured = checkUserGroupFeatured(loggingData.getActivityType(),
					loggingData.getActivityDescription());

			if (speciesNullActivityList.contains(loggingData.getActivityType())) {
				activity = new Activity(null, 0L, loggingData.getActivityDescription(), null, null, null,
						loggingData.getActivityType(), userId, new Date(), new Date(), loggingData.getRootObjectId(),
						ActivityEnums.species.getValue(), loggingData.getSubRootObjectId(),
						ActivityEnums.species.getValue(), true, null);
			} else if (speciesFieldActivityList.contains(loggingData.getActivityType())) {
				activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
						ActivityEnums.speciesField.getValue(), null, loggingData.getActivityType(), userId, new Date(),
						new Date(), loggingData.getRootObjectId(), ActivityEnums.species.getValue(),
						loggingData.getSubRootObjectId(), ActivityEnums.species.getValue(), true, null);
			} else if (speciesTaxonomyActivityList.contains(loggingData.getActivityType())) {
				activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
						ActivityEnums.taxonomyRegistry.getValue(), null, loggingData.getActivityType(), userId,
						new Date(), new Date(), loggingData.getRootObjectId(), ActivityEnums.species.getValue(),
						loggingData.getSubRootObjectId(), ActivityEnums.species.getValue(), true, null);
			} else if (speciesUserGroupActivityList.contains(loggingData.getActivityType()) && isUsergroupFeatured) {
				activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
						ActivityEnums.userGroup.getValue(), null, loggingData.getActivityType(), userId, new Date(),
						new Date(), loggingData.getRootObjectId(), ActivityEnums.species.getValue(),
						loggingData.getSubRootObjectId(), ActivityEnums.species.getValue(), true, null);
			} else if (speciesTraitActivityList.contains(loggingData.getActivityType())) {
				activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
						ActivityEnums.facts.getValue(), null, loggingData.getActivityType(), userId, new Date(),
						new Date(), loggingData.getRootObjectId(), ActivityEnums.species.getValue(),
						loggingData.getSubRootObjectId(), ActivityEnums.species.getValue(), true, null);
			} else if (speciesCommentActivityList.contains(loggingData.getActivityType())) {
				activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
						ActivityEnums.comments.getValue(), null, loggingData.getActivityType(), userId, new Date(),
						new Date(), loggingData.getRootObjectId(), ActivityEnums.species.getValue(),
						loggingData.getSubRootObjectId(), ActivityEnums.comments.getValue(), true, null);
			} else if (speciesActivityList.contains(loggingData.getActivityType())) {
				activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
						ActivityEnums.species.getValue(), null, loggingData.getActivityType(), userId, new Date(),
						new Date(), loggingData.getRootObjectId(), ActivityEnums.species.getValue(),
						loggingData.getSubRootObjectId(), ActivityEnums.species.getValue(), true, null);
			}

			if (activity != null)
				activity = activityDao.save(activity);

//			TODO mailData integration

			return activity;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return null;
	}

//	check if its a usergroup featuring or mother portal featuring and also if its usergroup activity
	private Boolean checkUserGroupFeatured(String acitivityType, String description) {
		try {
			if (acitivityType.equals("Featured") || acitivityType.equals("UnFeatured")) {
				UserGroupActivity ugActivity = objectMapper.readValue(description, UserGroupActivity.class);
				if (ugActivity.getUserGroupId() != null)
					return true;
				return false;
			}
			if (acitivityType.equals("Posted resource") || acitivityType.equals("Removed resoruce"))
				return true;
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return false;

	}
}
