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
@Table(name = "comment")
@JsonIgnoreProperties(ignoreUnknown = true)
public class Comments implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1560748571733072408L;

	private Long id;
	private Long authorId;
	private String body;
	private Long commentHolderId;
	private String commentHolderType;
	private Date dateCreated;
	private Date lastUpdated;
	private Long rootHolderId;
	private String rootHolderType;
	private Long mainParentId;
	private Long ParentId;
	private String subject;
	private Long languageId;

	@Id
	@GeneratedValue
	@Column(name = "id")
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "author_id")
	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

	@Column(name = "body")
	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	@Column(name = "comment_holder_id")
	public Long getCommentHolderId() {
		return commentHolderId;
	}

	public void setCommentHolderId(Long commentHolderId) {
		this.commentHolderId = commentHolderId;
	}

	@Column(name = "comment_holder_type")
	public String getCommentHolderType() {
		return commentHolderType;
	}

	public void setCommentHolderType(String commentHolderType) {
		this.commentHolderType = commentHolderType;
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

	@Column(name = "main_parent_id")
	public Long getMainParentId() {
		return mainParentId;
	}

	public void setMainParentId(Long mainParentId) {
		this.mainParentId = mainParentId;
	}

	@Column(name = "parent_id")
	public Long getParentId() {
		return ParentId;
	}

	public void setParentId(Long parentId) {
		ParentId = parentId;
	}

	@Column(name = "subject")
	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Column(name = "language_id")
	public Long getLanguageId() {
		return languageId;
	}

	public void setLanguageId(Long languageId) {
		this.languageId = languageId;
	}

}
