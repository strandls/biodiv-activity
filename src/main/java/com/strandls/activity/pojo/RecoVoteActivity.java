/**
 * 
 */
package com.strandls.activity.pojo;

/**
 * @author Abhishek Rudra
 *
 */
public class RecoVoteActivity {

	private String scientificName;
	private String commonName;
	private String givenName;
	private Long speciesId;

	/**
	 * 
	 */
	public RecoVoteActivity() {
		super();
	}

	/**
	 * @param scientificName
	 * @param commonName
	 * @param givenName
	 * @param speciesId
	 */
	public RecoVoteActivity(String scientificName, String commonName, String givenName, Long speciesId) {
		super();
		this.scientificName = scientificName;
		this.commonName = commonName;
		this.givenName = givenName;
		this.speciesId = speciesId;
	}

	public String getScientificName() {
		return scientificName;
	}

	public void setScientificName(String scientificName) {
		this.scientificName = scientificName;
	}

	public String getCommonName() {
		return commonName;
	}

	public void setCommonName(String commonName) {
		this.commonName = commonName;
	}

	public String getGivenName() {
		return givenName;
	}

	public void setGivenName(String givenName) {
		this.givenName = givenName;
	}

	public Long getSpeciesId() {
		return speciesId;
	}

	public void setSpeciesId(Long speciesId) {
		this.speciesId = speciesId;
	}

}
