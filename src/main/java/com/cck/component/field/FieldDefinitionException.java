package com.cck.component.field;

public class FieldDefinitionException extends Exception{
	public FieldDefinitionException(Throwable t) {
		super(t);
	}
	
	public FieldDefinitionException(String message) {
		super(message);
	}
}
