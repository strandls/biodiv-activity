/**
 * 
 */
package com.strandls.activity.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.strandls.activity.pojo.ShowActivityIbp;
import com.strandls.activity.service.ActivityService;
import com.strandls.observation.controller.RecommendationServicesApi;
import com.strandls.observation.pojo.RecoIbp;
import com.strandls.traits.controller.TraitsServiceApi;
import com.strandls.traits.pojo.FactValuePair;
import com.strandls.user.controller.UserServiceApi;
import com.strandls.user.pojo.UserIbp;
import com.strandls.userGroup.controller.UserGroupSerivceApi;
import com.strandls.userGroup.pojo.UserGroupIbp;
import com.strandls.utility.controller.UtilityServiceApi;
import com.strandls.utility.pojo.FlagIbp;

/**
 * @author Abhishek Rudra
 *
 */
public class ActivityServiceImpl implements ActivityService {

	private final Logger logger = LoggerFactory.getLogger(ActivityServiceImpl.class);

	@Inject
	private ActivityDao activityDao;

	@Inject
	private CommentsDao commentsDao;

	@Inject
	private UserGroupSerivceApi userGroupService;

	@Inject
	private TraitsServiceApi traitsService;

	@Inject
	private UtilityServiceApi utilityService;

	@Inject
	private UserServiceApi userService;

	@Inject
	private RecommendationServicesApi recoService;

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

				FactValuePair fact = null;
				FlagIbp flag = null;
				UserGroupIbp userGroup = null;
				RecoIbp recoIbp = null;
				Comments comment = null;
				Comments reply = null;
				CommentsIbp commentIbp = null;
				CommentsIbp replyIbp = null;

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

				} else if (traitsActivityList.contains(activity.getActivityType())) {

					fact = traitsService.getFactIbp(activity.getActivityHolderId().toString());

				} else if (flagActivityList.contains(activity.getActivityType())) {

					flag = utilityService.getFlagsIbp(activity.getActivityHolderId().toString());
					if (flag == null) {
						String description = activity.getDescriptionJson().getDescription();
						String[] desc = description.split("\n");
						flag = new FlagIbp();
						flag.setFlag(desc[0]);
						flag.setNotes(desc[1]);
					}

				} else if (userGroupActivityList.contains(activity.getActivityType())) {
					if (!(activity.getActivityHolderId().equals(activity.getRootHolderId())))
						userGroup = userGroupService.getIbpData(activity.getActivityHolderId().toString());
				} else if (recommendationActivityList.contains(activity.getActivityType())
						&& activity.getActivityHolderType().equals(ActivityEnums.recommendationVote.getValue())) {

					if (activity.getActivityHolderId() != null)
						recoIbp = recoService.getRecoVote(activity.getActivityHolderId().toString());
					if (recoIbp == null)
						if (activity.getActivityDescription() != null)
							recoIbp = extractName(activity.getActivityDescription());
						else {
							MyJson jsonData = activity.getDescriptionJson();
							recoIbp = new RecoIbp();
							recoIbp.setScientificName(jsonData.getName());
							recoIbp.setSpeciesId(jsonData.getRo_id());
						}

				} else if (observationActivityList.contains(activity.getActivityType())
						&& activity.getActivityHolderType().equals(ActivityEnums.observation.getValue())) {
					if (activity.getActivityType().equalsIgnoreCase("Suggestion removed")) {
						recoIbp = extractName(activity.getActivityDescription());
					}

				}
				UserIbp user = userService.getUserIbp(activity.getAuthorId().toString());
				ActivityIbp activityIbp = new ActivityIbp(activity.getActivityDescription(), activity.getActivityType(),
						activity.getDateCreated(), activity.getLastUpdated());

				ibpActivity.add(
						new ShowActivityIbp(activityIbp, recoIbp, commentIbp, replyIbp, user, fact, userGroup, flag));

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
		return result;

	}

	private RecoIbp extractName(String activityDesc) {
		RecoIbp recoIbp = new RecoIbp();
		String name = "";
		String speciesId = "";
		String regexName = Pattern.quote("<i>") + "(.*?)" + Pattern.quote("</i>");
		Pattern patternName = Pattern.compile(regexName);
		Matcher matcherName = patternName.matcher(activityDesc);
		while (matcherName.find()) {
			name = matcherName.group(1); // Since (.*?) is capturing group 1
			recoIbp.setScientificName(name);
		}
		String regexSpeciesId = Pattern.quote("/show/") + "(.*?)" + Pattern.quote("?");
		Pattern patternSpeciesId = Pattern.compile(regexSpeciesId);
		Matcher matcherSpeciesId = patternSpeciesId.matcher(activityDesc);
		while (matcherSpeciesId.find()) {
			speciesId = matcherSpeciesId.group(1); // Since (.*?) is capturing group 1
			if (speciesId.length() != 0)
				recoIbp.setSpeciesId(Long.parseLong(speciesId));
		}
		return recoIbp;
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
					result.getRootHolderType(), result.getId(), "Added a comment");
		} else {
			activity = new ActivityLoggingData(null, result.getRootHolderId(), result.getCommentHolderId(),
					result.getRootHolderType(), result.getId(), "Added a comment");
		}
		Activity activityResult = logActivities(userId, activity);

		return activityResult;
	}

}
