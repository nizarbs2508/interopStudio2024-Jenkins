package com.ans.cda.service.xdm;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ConceptList
 * 
 * @author bensalem Nizar
 */
public class ConceptList {
	/**
	 * concept
	 */
	@JsonProperty("Concept")
	public List<ConceptJdv> concept = new ArrayList<>();
	/**
	 * system
	 */
	private String system;

	/**
	 * @return the concept
	 */
	public List<ConceptJdv> getConcept() {
		return concept;
	}

	/**
	 * @param concept the concept to set
	 */
	public void setConcept(final List<ConceptJdv> concept) {
		this.concept = concept;
	}

	/**
	 * @return the system
	 */
	public String getSystem() {
		return system;
	}

	/**
	 * @param system the system to set
	 */
	public void setSystem(final String system) {
		this.system = system;
	}
}
