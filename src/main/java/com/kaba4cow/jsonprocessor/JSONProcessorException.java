package com.kaba4cow.jsonprocessor;

/**
 * Thrown to indicate an error occurred during JSON processing operations. This includes validation errors, mapping errors, and
 * reflection-related issues.
 */
public class JSONProcessorException extends Exception {

	private static final long serialVersionUID = 1L;

	JSONProcessorException(String format, Object... args) {
		super(String.format(format, args));
	}

	JSONProcessorException(Throwable cause, String format, Object... args) {
		super(String.format(format, args), cause);
	}

}