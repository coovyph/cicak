package com.cck.util;

public class CryptoCckException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CryptoCckException(String message) {
		super(message);
	}

	public CryptoCckException(Throwable e) {
		super(e);
	}
}
