/**
 * 
 */
package com.strandls.activity.pojo;

/**
 * @author Abhishek Rudra
 *
 */
public class ActivityLoggingData {

	private String activityDescription;
	private Long rootObjectId;
	private Long subRootObjectId;
	private String rootObjectType;
	private Long activityId;
	private String activityType;
	private MailData mailData;

	/**
	 * 
	 */
	public ActivityLoggingData() {
		super();
	}

	/**
	 * @param activityDescription
	 * @param rootObjectId
	 * @param subRootObjectId
	 * @param rootObjectType
	 * @param activityId
	 * @param activityType
	 * @param mailData
	 */
	public ActivityLoggingData(String activityDescription, Long rootObjectId, Long subRootObjectId,
			String rootObjectType, Long activityId, String activityType, MailData mailData) {
		super();
		this.activityDescription = activityDescription;
		this.rootObjectId = rootObjectId;
		this.subRootObjectId = subRootObjectId;
		this.rootObjectType = rootObjectType;
		this.activityId = activityId;
		this.activityType = activityType;
		this.mailData = mailData;
	}

	public String getActivityDescription() {
		return activityDescription;
	}

	public void setActivityDescription(String activityDescription) {
		this.activityDescription = activityDescription;
	}

	public Long getRootObjectId() {
		return rootObjectId;
	}

	public void setRootObjectId(Long rootObjectId) {
		this.rootObjectId = rootObjectId;
	}

	public Long getSubRootObjectId() {
		return subRootObjectId;
	}

	public void setSubRootObjectId(Long subRootObjectId) {
		this.subRootObjectId = subRootObjectId;
	}

	public String getRootObjectType() {
		return rootObjectType;
	}

	public void setRootObjectType(String rootObjectType) {
		this.rootObjectType = rootObjectType;
	}

	public Long getActivityId() {
		return activityId;
	}

	public void setActivityId(Long activityId) {
		this.activityId = activityId;
	}

	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	public MailData getMailData() {
		return mailData;
	}

	public void setMailData(MailData mailData) {
		this.mailData = mailData;
	}

}
