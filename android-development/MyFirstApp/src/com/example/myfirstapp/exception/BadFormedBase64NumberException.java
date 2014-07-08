package com.example.myfirstapp.exception;

/**
 * Represents an error in the base64 codification of a safetycode number.
 * */
public class BadFormedBase64NumberException extends Exception {

	private static final long serialVersionUID = 1L;

	public BadFormedBase64NumberException(String message) {
		super(message);
	}
}
