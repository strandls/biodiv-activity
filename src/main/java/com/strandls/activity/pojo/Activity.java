/**
 * 
 */
package com.strandls.activity.pojo;

import java.io.Serializable;
import java.sql.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * @author Abhishek Rudra
 *
 */

@Entity
@Table(name = "activity_feed")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Activity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7491402738483974055L;

	private Long id;
	private String activityDescription;
	private Long activityHolderId;
	private String activityHolderType;
	private String activityRootType;
	private String activityType;
	private Long authorId;
	private Date dateCreated;
	private Date lastUpdated;
	private Long rootHolderId;
	private String rootHolderType;
	private Long subRootHolderId;
	private String subRootHolderType;
	private Boolean isShowable;
	private String descriptionJson;

	@Id
	@GeneratedValue
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "activity_descrption")
	public String getActivityDescription() {
		return activityDescription;
	}

	public void setActivityDescription(String activityDescription) {
		this.activityDescription = activityDescription;
	}

	@Column(name = "activity_holder_id")
	public Long getActivityHolderId() {
		return activityHolderId;
	}

	public void setActivityHolderId(Long activityHolderId) {
		this.activityHolderId = activityHolderId;
	}

	@Column(name = "activity_holder_type")
	public String getActivityHolderType() {
		return activityHolderType;
	}

	public void setActivityHolderType(String activityHolderType) {
		this.activityHolderType = activityHolderType;
	}

	@Column(name = "activity_root_type")
	public String getActivityRootType() {
		return activityRootType;
	}

	public void setActivityRootType(String activityRootType) {
		this.activityRootType = activityRootType;
	}

	@Column(name = "activity_type")
	public String getActivityType() {
		return activityType;
	}

	public void setActivityType(String activityType) {
		this.activityType = activityType;
	}

	@Column(name = "author_id")
	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	@Column(name = "date_created")
	public Date getDateCreated() {
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated) {
		this.dateCreated = dateCreated;
	}

	@Column(name = "last_updated")
	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}

	@Column(name = "root_holder_id")
	public Long getRootHolderId() {
		return rootHolderId;
	}

	public void setRootHolderId(Long rootHolderId) {
		this.rootHolderId = rootHolderId;
	}

	@Column(name = "root_holder_type")
	public String getRootHolderType() {
		return rootHolderType;
	}

	public void setRootHolderType(String rootHolderType) {
		this.rootHolderType = rootHolderType;
	}

	@Column(name = "sub_root_holder_id")
	public Long getSubRootHolderId() {
		return subRootHolderId;
	}

	public void setSubRootHolderId(Long subRootHolderId) {
		this.subRootHolderId = subRootHolderId;
	}

	@Column(name = "sub_root_holder_type")
	public String getSubRootHolderType() {
		return subRootHolderType;
	}

	public void setSubRootHolderType(String subRootHolderType) {
		this.subRootHolderType = subRootHolderType;
	}

	@Column(name = "is_showable")
	public Boolean getIsShowable() {
		return isShowable;
	}

	public void setIsShowable(Boolean isShowable) {
		this.isShowable = isShowable;
	}

	@Column(name = "description_json")
	public String getDescriptionJson() {
		return descriptionJson;
	}

	public void setDescriptionJson(String descriptionJson) {
		this.descriptionJson = descriptionJson;
	}

}
