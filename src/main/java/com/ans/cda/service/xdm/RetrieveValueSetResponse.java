package com.ans.cda.service.xdm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RetrieveValueSetResponse
 * 
 * @author bensalem Nizar
 */
public class RetrieveValueSetResponse {
	/**
	 * xmlns
	 */
	public String xmlns;
	/**
	 * xmlnsxsi
	 */
	@JsonProperty("xmlns:xsi")
	public String xmlnsxsi;
	/**
	 * valueSet
	 */
	@JsonProperty("ValueSet")
	public ValueSet valueSet;
}
