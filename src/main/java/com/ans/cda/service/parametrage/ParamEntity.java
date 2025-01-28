package com.ans.cda.service.parametrage;

/**
 * ParamEntity
 * 
 * @author bensalem Nizar
 */
public final class ParamEntity {
	/**
	 * oid
	 */
	public String oid;
	/**
	 * value
	 */
	public String value;
	
	/**
	 * ParamEntity
	 */
	ParamEntity() {
		//empty constructor
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
	 * @return the value
	 */
	public String getValue() {
		return value;
	}

	/**
	 * @param value the value to set
	 */
	public void setValue(final String value) {
		this.value = value;
	}

}
