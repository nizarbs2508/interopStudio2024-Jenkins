package com.ans.cda.fhir;

/**
 * class Item
 * 
 * @author nbensalem
 */
public class ItemEntity {
	/**
	 * name
	 */
	private String name;
	/**
	 * oid
	 */
	private String oid;
	/**
	 * url
	 */
	private String url;
	/**
	 * contenu
	 */
	private String contenu;
	/**
	 * lineNumber
	 */
	private Integer lineNumber;
	/**
	 * section
	 */
	private String section;
	/**
	 * constructor
	 */
	public ItemEntity() {
		//empty constructor
	}

	/**
	 * constructor
	 * 
	 * @param column1
	 * @param column2
	 * @param column3
	 */
	public ItemEntity(final String column1, final String column2, final String column3, final String column4, final Integer column5, final String column6) {
		name = column1;
		oid = column2;
		url = column3;
		contenu = column4;
		lineNumber = column5;
		section = column6;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(final String name) {
		this.name = name;
	}

	/**
	 * @return the oid
	 */
	public String getOid() {
		return oid;
	}

	/**
	 * @param oid the oid to set
	 */
	public void setOid(final String oid) {
		this.oid = oid;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(final String url) {
		this.url = url;
	}

	/**
	 * @return the contenu
	 */
	public String getContenu() {
		return contenu;
	}

	/**
	 * @param contenu the contenu to set
	 */
	public void setContenu(final String contenu) {
		this.contenu = contenu;
	}

	/**
	 * @return the lineNumber
	 */
	public Integer getLineNumber() {
		return lineNumber;
	}

	/**
	 * @param lineNumber the lineNumber to set
	 */
	public void setLineNumber(final Integer lineNumber) {
		this.lineNumber = lineNumber;
	}

	/**
	 * @return the section
	 */
	public String getSection() {
		return section;
	}

	/**
	 * @param section the section to set
	 */
	public void setSection(final String section) {
		this.section = section;
	}

}
