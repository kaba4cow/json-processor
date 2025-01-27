package com.kaba4cow.jsonprocessor;

/**
 * Enumeration for specifying how enum fields should be serialized or deserialized.
 */
public enum EnumFormat {

	/**
	 * Serialize or deserialize enum fields as their string names.
	 */
	STRING,

	/**
	 * Serialize or deserialize enum fields as their ordinal values.
	 */
	ORDINAL;

}