package com.strandls.activity.pojo;

import java.util.List;

public class MailData {

	private observationMailData observationData;
	private DocumentMailData documentMailData;
	private List<UserGroupMailData> userGroupData;
	private SpeciesMailData speciesData;

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
	 * @param speciesData
	 */
	public MailData(observationMailData observationData, DocumentMailData documentMailData,
			List<UserGroupMailData> userGroupData, SpeciesMailData speciesData) {
		super();
		this.observationData = observationData;
		this.documentMailData = documentMailData;
		this.userGroupData = userGroupData;
		this.speciesData = speciesData;
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

	public SpeciesMailData getSpeciesData() {
		return speciesData;
	}

	public void setSpeciesData(SpeciesMailData speciesData) {
		this.speciesData = speciesData;
	}

}
