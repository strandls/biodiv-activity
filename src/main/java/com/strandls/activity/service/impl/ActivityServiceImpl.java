/**
 * 
 */
package com.strandls.activity.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.strandls.activity.dao.ActivityDao;
import com.strandls.activity.dao.CommentsDao;
import com.strandls.activity.pojo.Activity;
import com.strandls.activity.pojo.ActivityIbp;
import com.strandls.activity.pojo.ActivityResult;
import com.strandls.activity.pojo.Comments;
import com.strandls.activity.pojo.CommentsIbp;
import com.strandls.activity.pojo.ShowActivity;
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

	@Override
	public List<ShowActivity> fetchActivity(String objectType, Long objectId) {

		Comments comment = null;
		Comments reply = null;
		List<ShowActivity> showActivites = new ArrayList<ShowActivity>();
		List<Activity> activites = activityDao.findByObjectId(objectType, objectId);
		for (Activity activity : activites) {
			comment = null;
			reply = null;
			if (activity.getActivityType().equals("Added a comment")) {

				if (activity.getActivityHolderId().equals(activity.getSubRootHolderId())) {
					comment = commentsDao.findById(activity.getActivityHolderId());

				} else {
					comment = commentsDao.findById(activity.getSubRootHolderId());
					reply = commentsDao.findById(activity.getActivityHolderId());
				}
			}
			showActivites.add(new ShowActivity(activity, comment, reply));
		}
		return showActivites;
	}

	@Override
	public ActivityResult fetchActivityIbp(String objectType, Long objectId) {

		List<ShowActivityIbp> ibpActivity = new ArrayList<ShowActivityIbp>();
		Integer commentCount = 0;
		ActivityResult activityResult = null;

		try {

			List<Activity> activites = activityDao.findByObjectId(objectType, objectId);
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

				if (activity.getActivityType().equals("Added a comment")) {

					if (activity.getActivityHolderId().equals(activity.getSubRootHolderId())) {
						comment = commentsDao.findById(activity.getActivityHolderId());
						commentIbp = new CommentsIbp(comment.getBody());

					} else {
						reply = commentsDao.findById(activity.getSubRootHolderId());
						comment = commentsDao.findById(activity.getActivityHolderId());
						replyIbp = new CommentsIbp(comment.getBody());
						commentIbp = new CommentsIbp(reply.getBody());
					}

				} else if (activity.getActivityType().equals("Added a fact")
						|| activity.getActivityType().equals("Updated fact")) {

					fact = traitsService.getFactIbp(activity.getActivityHolderId().toString());

				} else if (activity.getActivityType().equals("Flagged")
						|| activity.getActivityType().equals("Flag removed")) {

					flag = utilityService.getFlagsIbp(activity.getActivityHolderId().toString());

				} else if (activity.getActivityType().equals("Posted resource")
						|| activity.getActivityType().equals("Removed resoruce")) {

					userGroup = userGroupService.getIbpData(activity.getActivityHolderId().toString());
				} else if (activity.getActivityType().equals("Suggested species name")
						|| activity.getActivityType().equals("Agreed on species name")
						|| activity.getActivityType().equals("Suggestion removed")) {

					recoIbp = recoService.getRecoVote(activity.getActivityHolderId().toString());
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

}
