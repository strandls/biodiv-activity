/**
 * 
 */
package com.strandls.activity.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
	private ObjectMapper objectMapper;

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
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

		return result;

	}

	private RecoVoteActivity extractName(String activityDesc) {
		RecoVoteActivity reco = new RecoVoteActivity();
		String name = "";
		String speciesId = "";
		String regexName = Pattern.quote("<i>") + "(.*?)" + Pattern.quote("</i>");
		Pattern patternName = Pattern.compile(regexName);
		Matcher matcherName = patternName.matcher(activityDesc);
		while (matcherName.find()) {
			name = matcherName.group(1); // Since (.*?) is capturing group 1
			reco.setGivenName(name);
		}
		String regexSpeciesId = Pattern.quote("/show/") + "(.*?)" + Pattern.quote("?");
		Pattern patternSpeciesId = Pattern.compile(regexSpeciesId);
		Matcher matcherSpeciesId = patternSpeciesId.matcher(activityDesc);
		while (matcherSpeciesId.find()) {
			speciesId = matcherSpeciesId.group(1); // Since (.*?) is capturing group 1
			if (speciesId.length() != 0)
				reco.setSpeciesId(Long.parseLong(speciesId));
		}
		return reco;
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

	@Override
	public void migrateData() {
		try {

			InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream("config.properties");

			Properties properties = new Properties();
			try {
				properties.load(in);
			} catch (IOException e) {
				logger.error(e.getMessage());
			}
			String portalName = properties.getProperty("portalName");
			String portalWebAddress = properties.getProperty("portalAddress");
			in.close();

			System.out.println("portal Name :" + portalName);
			System.out.println("portal webAddress :" + portalWebAddress);
			Integer startPosition = 0;
			Boolean nextBatch = true;
			Integer total = 0;
			while (nextBatch) {
				List<Activity> activities = activityDao.findAllObservationActivity(ActivityEnums.observation.getValue(),
						startPosition);
				total += activities.size();
				if (activities.size() == 50000)
					startPosition = total + 1;
				else
					nextBatch = false;
			}
			nextBatch = true;

			while (nextBatch) {
				List<Activity> activities = activityDao.findAllObservationActivity(ActivityEnums.observation.getValue(),
						startPosition);
				if (activities.size() == 50000)
					startPosition = total + 1;
				else
					nextBatch = false;

				int count = 0;
				System.out.println("Total Number of Count :" + total);
				String description = "";
				for (Activity activity : activities) {
					description = "";

					if (activity.getId() == 3710993 || activity.getId() == 13782175)
						continue;

					System.out.println("==========================BEGIN=======================");

					System.out.println("Activity id :" + activity.getId() + " activity description :"
							+ activity.getActivityDescription() + " activity Type:" + activity.getActivityType());

					if (traitsActivityList.contains(activity.getActivityType())) {
						if (activity.getActivityDescription().trim().length() == 0) {
							FactValuePair fact = traitsService.getFactIbp(activity.getActivityHolderId().toString());
							description = fact.getName() + ":" + fact.getValue();
							System.out.println("New facts description : " + description);
						}

					} else if (flagActivityList.contains(activity.getActivityType())) {

						FlagIbp flag = utilityService.getFlagsIbp(activity.getActivityHolderId().toString());
						if (flag == null) {
							description = activity.getDescriptionJson().getDescription();
							String[] desc = description.split("\n");
							flag = new FlagIbp();
							flag.setFlag(desc[0]);
							if (desc.length == 2)
								flag.setNotes(desc[1]);
						}
						description = flag.getFlag() + ":" + flag.getNotes();
						System.out.println("Flag Description :" + description);

					} else if (userGroupActivityList.contains(activity.getActivityType())) {
						if (!(activity.getActivityHolderId().equals(activity.getRootHolderId()))) {
							UserGroupIbp userGroup = userGroupService
									.getIbpData(activity.getActivityHolderId().toString());

							String activityDesc = activity.getActivityDescription();
							String feature = null;
							if (!(activityDesc.equalsIgnoreCase("Posted observation to group")
									|| activityDesc.equals("Removed observation from group")))
								feature = activityDesc;
							UserGroupActivity ugActivity = new UserGroupActivity(userGroup.getId(), userGroup.getName(),
									userGroup.getWebAddress(), feature);

							description = objectMapper.writeValueAsString(ugActivity);

							System.out.println("UserGroup description :" + description);
						}

					} else if (recommendationActivityList.contains(activity.getActivityType())
							&& activity.getActivityHolderType().equals(ActivityEnums.recommendationVote.getValue())) {
						RecoIbp recoIbp = null;
						if (activity.getActivityHolderId() != null)
							recoIbp = recoService.getRecoVote(activity.getActivityHolderId().toString());
						RecoVoteActivity recoVote = new RecoVoteActivity();
						if (activity.getActivityDescription() != null)
							recoVote = extractName(activity.getActivityDescription());
						if (recoVote.getGivenName() == null) {
							MyJson jsonData = activity.getDescriptionJson();
							if (jsonData != null) {
								if (jsonData.getName() != null)
									recoVote.setGivenName(jsonData.getName());
								if (jsonData.getRo_id() != null)
									recoVote.setSpeciesId(jsonData.getRo_id());
							}

						}

						String scientificName = null;
						String commonName = null;
						Long speciesId = null;
						if (recoIbp != null) {
							if (recoIbp.getScientificName() != null)
								scientificName = recoIbp.getScientificName();
							if (recoIbp.getCommonName() != null)
								commonName = recoIbp.getCommonName();
							if (recoIbp.getSpeciesId() != null)
								speciesId = recoIbp.getSpeciesId();
						}

						if (speciesId == null)
							speciesId = recoVote.getSpeciesId();

						RecoVoteActivity reco = new RecoVoteActivity(scientificName, commonName,
								recoVote.getGivenName(), speciesId);

						description = objectMapper.writeValueAsString(reco);
						System.out.println("Reco : " + description);

					} else if (observationActivityList.contains(activity.getActivityType())
							&& activity.getActivityHolderType().equals(ActivityEnums.observation.getValue())) {
						if (activity.getActivityType().equalsIgnoreCase("Suggestion removed")) {
							RecoVoteActivity recoVote = new RecoVoteActivity();
							if (activity.getActivityDescription() != null)
								recoVote = extractName(activity.getActivityDescription());
							if (recoVote.getGivenName() == null) {
								MyJson jsonData = activity.getDescriptionJson();
								if (jsonData != null) {
									if (jsonData.getName() != null)
										recoVote.setGivenName(jsonData.getName());
									if (jsonData.getRo_id() != null)
										recoVote.setSpeciesId(jsonData.getRo_id());
								}

							}

							RecoVoteActivity reco = new RecoVoteActivity(null, null, recoVote.getGivenName(),
									recoVote.getSpeciesId());

							description = objectMapper.writeValueAsString(reco);
							System.out.println("Obvservation Reco remove : " + description);

						} else if (activity.getActivityType().equalsIgnoreCase("UnFeatured")
								|| activity.getActivityType().equalsIgnoreCase("Featured")) {

							String activityDesc = activity.getActivityDescription();
							String feature = null;
							if (!(activityDesc.equalsIgnoreCase("Posted observation to group")
									|| activityDesc.equals("Removed observation from group")))
								feature = activityDesc;

							UserGroupActivity ugActivity = new UserGroupActivity(null, portalName, portalWebAddress,
									feature);

							description = objectMapper.writeValueAsString(ugActivity);
							System.out.println("observation Feature unfeature : " + description);
						}

					}

					activity.setActivityDescription(description);
					activityDao.update(activity);
					count++;
					System.out.println("Count :" + count + " out of " + total + "\t Activity Id :" + activity.getId());
					System.out.println("==========================END========================");
				}

				System.out.println("Migration Completed Successfully");

			}

		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}
}
