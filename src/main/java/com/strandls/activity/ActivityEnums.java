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
	filterRule("FilterRule"), document("content.eml.Document"), species("species.Species"),
	speciesField("species.SpeciesField"), taxonomyRegistry("species.TaxonomyRegistry"),
	taxonomyDefinition("species.TaxonomyDefinition"), commonName("species.CommonNames");

	String value;

	private ActivityEnums(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}

}
