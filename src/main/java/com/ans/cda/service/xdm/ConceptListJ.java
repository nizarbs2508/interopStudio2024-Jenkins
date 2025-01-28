package com.ans.cda.service.xdm;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ConceptListJ
 * 
 * @author bensalem Nizar
 */
public class ConceptListJ {
	/**
	 * concept
	 */
	@JsonProperty("Concept")
	public List<ConceptJ> concept = new ArrayList<>();

}
