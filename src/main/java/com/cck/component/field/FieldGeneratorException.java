package com.cck.component.field;

public class FieldGeneratorException extends Exception{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public FieldGeneratorException(Throwable e){
		super(e);
	}

	public FieldGeneratorException(String message) {
		super(message);
	}

	public FieldGeneratorException(String message,Throwable e) {
		super(message,e);
	}
}
