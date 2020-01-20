/**
 * 
 */
package com.strandls.activity.dao;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.inject.Inject;
import com.strandls.activity.pojo.Activity;
import com.strandls.activity.util.AbstractDAO;

/**
 * @author Abhishek Rudra
 *
 */
public class ActivityDao extends AbstractDAO<Activity, Long> {

	private final Logger logger = LoggerFactory.getLogger(ActivityDao.class);

	/**
	 * @param sessionFactory
	 */
	@Inject
	protected ActivityDao(SessionFactory sessionFactory) {
		super(sessionFactory);
	}

	@Override
	public Activity findById(Long id) {
		Session session = sessionFactory.openSession();
		Activity entity = null;
		try {
			entity = session.get(Activity.class, id);
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return entity;
	}

	@SuppressWarnings("unchecked")
	public List<Activity> findByObjectId(String objectType, Long id, String offset, String limit) {

		String qry = "from Activity a where a.rootHolderType = :objectType and a.rootHolderId = :id order by a.lastUpdated desc";
		Session session = sessionFactory.openSession();
		List<Activity> result = null;
		try {
			Query<Activity> query = session.createQuery(qry);
			query.setParameter("objectType", objectType);
			query.setParameter("id", id);
			query.setFirstResult(Integer.parseInt(offset));
			query.setMaxResults(Integer.parseInt(limit));
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public Integer findCommentCount(String objectType, Long objectId) {
		String qry = "from Activity where rootHolderType = :objectType and rootHolderId = :id and activityType = \'Added a comment\' ";
		Session session = sessionFactory.openSession();
		Integer commentCount = 0;
		try {
			Query<Activity> query = session.createQuery(qry);
			query.setParameter("objectType", objectType);
			query.setParameter("id", objectId);
			commentCount = query.getResultList().size();

		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}
		return commentCount;
	}

	public List<Activity> findAllObservationActivity(String type) {
		Session session = sessionFactory.openSession();
		String qry = "from Activity where rootHolderType = :type order by id";
		List<Activity> result = null;
		try {
			@SuppressWarnings("unchecked")
			Query<Activity> query = session.createQuery(qry);
			query.setParameter("type", type);
			result = query.getResultList();
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {
			session.close();
		}

		return result;
	}

}
