package com.ans.cda.service.xdm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RetrieveValueSetResponseJdv
 * 
 * @author bensalem Nizar
 */
public class RetrieveValueSetResponseJdv {
	/**
	 * xmlns
	 */
	@JsonProperty("xmlns")
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
	public ValueSetJdv valueSet;
}
