package com.ans.cda.service.xdm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MappedConcept
 * 
 * @author bensalem Nizar
 */
public class MappedConcept {
	/**
	 * concept
	 */
	@JsonProperty("Concept")
	public List<Concept> concept;
}
