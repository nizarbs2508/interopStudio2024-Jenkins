package com.ans.cda.service.xdm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * RetrieveValueSetResponseJ
 * 
 * @author bensalem Nizar
 */
public class RetrieveValueSetResponseJ {
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
	public ValueSetJ valueSet;
}
