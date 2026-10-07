package com.cck.component.field;

import java.util.Map;

import org.jpos.iso.ISOMsg;

public interface IFieldValueGenerator {
	public Object constructMsgValue(ISOMsg msg,String interchangeName, Map<String,Object> msgBody,FieldDefinition fd)throws FieldGeneratorException;
}
