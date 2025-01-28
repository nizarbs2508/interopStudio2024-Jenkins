package com.ans.cda.service.xdm;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * ValueSetJdv
 * 
 * @author bensalem Nizar
 */
public class ValueSetJdv {
	/**
	 * conceptList
	 */
	@JsonProperty("ConceptList")
	public ConceptList conceptList;
	/**
	 * urlFichier
	 */
	public String urlFichier;
	/**
	 * displayName
	 */
	public String displayName;
	/**
	 * typeFichier
	 */
	public String typeFichier;
	/**
	 * description
	 */
	public String description;
	/**
	 * dateFin
	 */
	public String dateFin;
	/**
	 * id
	 */
	public String id;
	/**
	 * dateValid
	 */
	public long dateValid;
	/**
	 * dateMaj
	 */
	public long dateMaj;
}
