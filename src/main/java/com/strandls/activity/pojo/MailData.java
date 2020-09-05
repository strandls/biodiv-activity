package com.strandls.activity.pojo;

import java.util.List;

public class MailData {

	private observationMailData observationData;
	private DocumentMailData documentMailData;
	private List<UserGroupMailData> userGroupData;

	/**
	 * 
	 */
	public MailData() {
		super();
	}

	/**
	 * @param observationData
	 * @param documentMailData
	 * @param userGroupData
	 */
	public MailData(observationMailData observationData, DocumentMailData documentMailData,
			List<UserGroupMailData> userGroupData) {
		super();
		this.observationData = observationData;
		this.documentMailData = documentMailData;
		this.userGroupData = userGroupData;
	}

	public observationMailData getObservationData() {
		return observationData;
	}

	public void setObservationData(observationMailData observationData) {
		this.observationData = observationData;
	}

	public DocumentMailData getDocumentMailData() {
		return documentMailData;
	}

	public void setDocumentMailData(DocumentMailData documentMailData) {
		this.documentMailData = documentMailData;
	}

	public List<UserGroupMailData> getUserGroupData() {
		return userGroupData;
	}

	public void setUserGroupData(List<UserGroupMailData> userGroupData) {
		this.userGroupData = userGroupData;
	}

}
