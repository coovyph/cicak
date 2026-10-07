package com.cck.component.field;

import java.util.Map;

import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

import com.cck.service.GeneratorRegistrarService;

@Component("fieldF")
public class ISOFieldGenerator extends ACompoundFieldValueGenerator{

	public ISOFieldGenerator(GeneratorRegistrarService rgs) {
		super(rgs);
	}

	@Override
	public Object constructMsgValue(ISOMsg msg, String interchangeName,Map<String, Object> msgBody, FieldDefinition fd)
			throws FieldGeneratorException {
		Object value = msgBody.get(fd.getKey());
		String result = null;
		if(value instanceof Map) {
			result = constructSubValue(msg, interchangeName, (Map)value);
		}else if(value instanceof String) {
			result = (String)value;
		}else {
			throw new FieldGeneratorException("Invalid null value for key " +fd.getKey());
		}

		return formatValue(result);
	}

}
