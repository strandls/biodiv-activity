/**
 * 
 */
package com.strandls.activity.pojo;

import java.util.Date;

/**
 * @author Abhishek Rudra
 *
 */
public class DocumentMailData {

	private Long documentId;
	private Date createdOn;
	private Long authorId;

	/**
	 * 
	 */
	public DocumentMailData() {
		super();
	}

	/**
	 * @param documentId
	 * @param createdOn
	 * @param authorId
	 */
	public DocumentMailData(Long documentId, Date createdOn, Long authorId) {
		super();
		this.documentId = documentId;
		this.createdOn = createdOn;
		this.authorId = authorId;
	}

	public Long getDocumentId() {
		return documentId;
	}

	public void setDocumentId(Long documentId) {
		this.documentId = documentId;
	}

	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
	}

}
