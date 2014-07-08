package com.example.myfirstapp.exception;

public class BadFormedBase64NumberException extends Exception {

	private static final long serialVersionUID = 1L;

	public BadFormedBase64NumberException(String message) {
		super(message);
	}
}
