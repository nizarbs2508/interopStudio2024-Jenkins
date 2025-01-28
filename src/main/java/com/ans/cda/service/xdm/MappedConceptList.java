package com.ans.cda.service.xdm;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * MappedConceptList
 * 
 * @author bensalem Nizar
 */
public class MappedConceptList {
	/**
	 * mappedConcept
	 */
	@JsonProperty("MappedConcept")
	public List<MappedConcept> mappedConcept;
}
