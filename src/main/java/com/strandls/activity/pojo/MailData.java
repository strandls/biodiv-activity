package com.strandls.activity.pojo;

import java.util.List;

public class MailData {

	private observationMailData observationData;
	private List<UserGroupMailData> userGroupData;

	/**
	 * 
	 */
	public MailData() {
		super();
	}

	/**
	 * @param observationData
	 * @param userGroupData
	 */
	public MailData(observationMailData observationData, List<UserGroupMailData> userGroupData) {
		super();
		this.observationData = observationData;
		this.userGroupData = userGroupData;
	}

	public observationMailData getObservationData() {
		return observationData;
	}

	public void setObservationData(observationMailData observationData) {
		this.observationData = observationData;
	}

	public List<UserGroupMailData> getUserGroupData() {
		return userGroupData;
	}

	public void setUserGroupData(List<UserGroupMailData> userGroupData) {
		this.userGroupData = userGroupData;
	}

}
