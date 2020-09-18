/**
 * 
 */
package com.strandls.activity;

/**
 * @author Abhishek Rudra
 *
 */
public enum ActivityEnums {

	observation("species.participation.Observation"), recommendationVote("species.participation.RecommendationVote"),
	userGroup("species.groups.UserGroup"), facts("species.trait.Fact"), comments("species.participation.Comment"),
	flag("species.participation.Flag"), user("species.auth.SUser"), customField("CustomField"),
	filterRule("FilterRule"), document("content.eml.Document");

	String value;

	private ActivityEnums(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
