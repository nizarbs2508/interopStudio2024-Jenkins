package com.ans.cda.fhir;

public class CodeSystemEntity {

	/**
	 * 
	 */
	private String code;
	/**
	 * displayName
	 */
	private String displayName;
	/**
	 * systemName
	 */
	private String systemName;
	/**
	 * systemCode
	 */
	private String systemCode;
	/**
	 * obsolete
	 */
	private String obsolete;
	/**
	 * active
	 */
	private String active;

	public CodeSystemEntity() {

	}

	/**
	 * constructor
	 * 
	 * @param column1
	 * @param column2
	 * @param column3
	 */
	public CodeSystemEntity(final String code, final String displayName, final String systemName,
			final String systemCode, final String active) {
		this.code = code;
		this.displayName = displayName;
		this.systemName = systemName;
		this.systemCode = systemCode;
		this.active = active;
	}

	/**
	 * @return the code
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code the code to set
	 */
	public void setCode(final String code) {
		this.code = code;
	}

	/**
	 * @return the displayName
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @param displayName the displayName to set
	 */
	public void setDisplayName(final String displayName) {
		this.displayName = displayName;
	}

	/**
	 * @return the systemName
	 */
	public String getSystemName() {
		return systemName;
	}

	/**
	 * @param systemName the systemName to set
	 */
	public void setSystemName(final String systemName) {
		this.systemName = systemName;
	}

	/**
	 * @return the systemCode
	 */
	public String getSystemCode() {
		return systemCode;
	}

	/**
	 * @param systemCode the systemCode to set
	 */
	public void setSystemCode(final String systemCode) {
		this.systemCode = systemCode;
	}

	/**
	 * @return the active
	 */
	public String getActive() {
		return active;
	}

	/**
	 * @param active the active to set
	 */
	public void setActive(final String active) {
		this.active = active;
	}

	/**
	 * @return the obsolete
	 */
	public String getObsolete() {
		return obsolete;
	}

	/**
	 * @param obsolete the obsolete to set
	 */
	public void setObsolete(String obsolete) {
		this.obsolete = obsolete;
	}
}
