/**
 * 
 */
package com.strandls.activity.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Inject;
import com.strandls.activity.ActivityEnums;
import com.strandls.activity.dao.ActivityDao;
import com.strandls.activity.dao.CommentsDao;
import com.strandls.activity.pojo.Activity;
import com.strandls.activity.pojo.ActivityIbp;
import com.strandls.activity.pojo.ActivityLoggingData;
import com.strandls.activity.pojo.ActivityResult;
import com.strandls.activity.pojo.CommentLoggingData;
import com.strandls.activity.pojo.Comments;
import com.strandls.activity.pojo.CommentsIbp;
import com.strandls.activity.pojo.MyJson;
import com.strandls.activity.pojo.RecoVoteActivity;
import com.strandls.activity.pojo.ShowActivityIbp;
import com.strandls.activity.pojo.UserGroupActivity;
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

	List<String> nullActivityList = new ArrayList<String>(Arrays.asList("Observation created", "Observation updated"));

	List<String> recommendationActivityList = new ArrayList<String>(
			Arrays.asList("obv unlocked", "Suggested species name", "obv locked", "Agreed on species name"));

	List<String> userGroupActivityList = new ArrayList<String>(
			Arrays.asList("Posted resource", "Removed resoruce", "Featured", "UnFeatured"));

	List<String> traitsActivityList = new ArrayList<String>(Arrays.asList("Updated fact", "Added a fact"));

	List<String> flagActivityList = new ArrayList<String>(Arrays.asList("Flag removed", "Flagged"));

	List<String> commentActivityList = new ArrayList<String>(Arrays.asList("Added a comment"));

	List<String> observationActivityList = new ArrayList<String>(Arrays.asList("Featured", "Suggestion removed",
			"Observation tag updated", "Custom field edited", "UnFeatured", "Observation species group updated"));

	@Override
	public ActivityResult fetchActivityIbp(String objectType, Long objectId, String offset, String limit) {

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

				} else if (userGroupActivityList.contains(activity.getActivityType())) {
					String description = activity.getActivityDescription();
					ugActivity = objectMapper.readValue(description, UserGroupActivity.class);
				} else if (recommendationActivityList.contains(activity.getActivityType())
						|| activity.getActivityType().equalsIgnoreCase("Suggestion removed")) {
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

	@Override
	public Activity logActivities(Long userId, ActivityLoggingData loggingData) {
		Activity activity = null;
		MAIL_TYPE type = null;
		if (nullActivityList.contains(loggingData.getActivityType())) {
			activity = new Activity(null, 0L, null, null, null, null, loggingData.getActivityType(), userId, new Date(),
					new Date(), loggingData.getRootObjectId(), ActivityEnums.observation.getValue(),
					loggingData.getRootObjectId(), ActivityEnums.observation.getValue(), true, null);

		} else if (recommendationActivityList.contains(loggingData.getActivityType())) {

			activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
					ActivityEnums.recommendationVote.getValue(), null, loggingData.getActivityType(), userId,
					new Date(), new Date(), loggingData.getRootObjectId(), ActivityEnums.observation.getValue(),
					loggingData.getRootObjectId(), ActivityEnums.observation.getValue(), true, null);
		} else if (userGroupActivityList.contains(loggingData.getActivityType())) {
			activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
					ActivityEnums.userGroup.getValue(), null, loggingData.getActivityType(), userId, new Date(),
					new Date(), loggingData.getRootObjectId(), ActivityEnums.observation.getValue(),
					loggingData.getRootObjectId(), ActivityEnums.observation.getValue(), true, null);
		} else if (traitsActivityList.contains(loggingData.getActivityType())) {
			activity = new Activity(null, 0L, loggingData.getActivityDescription(), loggingData.getActivityId(),
					ActivityEnums.facts.getValue(), null, loggingData.getActivityType(), userId, new Date(), new Date(),
					loggingData.getRootObjectId(), ActivityEnums.observation.getValue(), loggingData.getRootObjectId(),
					ActivityEnums.observation.getValue(), true, null);
		} else if (flagActivityList.contains(loggingData.getActivityType())) {
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
			userService.updateFollow("observation", loggingData.getRootObjectId().toString());
			if (loggingData.getMailData() != null) {
				type = ActivityUtil.getMailType(activity.getActivityType(),
						userGroupActivityList.contains(activity.getActivityType()));
				if (type != null && type != MAIL_TYPE.COMMENT_POST) {
					mailService.sendMail(type, result.getRootHolderType(), result.getRootHolderId(), userId, null,
							loggingData);
					notificationSevice.sendNotification(result.getRootHolderType(), result.getRootHolderId(),
							"India Biodiversity Portal", activity.getActivityType());
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
			mailService.sendMail(MAIL_TYPE.OBSERVATION_ADDED, ActivityEnums.observation.getValue(),
					loggingData.getRootObjectId(), userId, null, loggingData);
			notificationSevice.sendNotification(ActivityEnums.observation.getValue(), loggingData.getRootObjectId(),
					"India Biodiversity Portal", "Observation created");
			return "Mail Sent";
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		return "Mail not sent";
	}

	@Override
	public Activity addComment(Long userId, CommentLoggingData commentData) {

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

		ActivityLoggingData activity = null;
		if (result.getCommentHolderId().equals(result.getRootHolderId())) {
			activity = new ActivityLoggingData(null, result.getRootHolderId(), result.getId(),
					result.getRootHolderType(), result.getId(), "Added a comment", commentData.getMailData());
		} else {
			activity = new ActivityLoggingData(null, result.getRootHolderId(), result.getCommentHolderId(),
					result.getRootHolderType(), result.getId(), "Added a comment", commentData.getMailData());
		}
		Activity activityResult = logActivities(userId, activity);
		mailService.sendMail(MAIL_TYPE.COMMENT_POST, activityResult.getRootHolderType(),
				activityResult.getRootHolderId(), userId, commentData, activity);
		notificationSevice.sendNotification(result.getRootHolderType(), result.getRootHolderId(),
				"India Biodiversity Portal", activity.getActivityType());

		return activityResult;
	}

}
