package com.cck.component.field;

import java.util.Map;

import org.jpos.iso.ISOMsg;
import org.springframework.stereotype.Component;

import com.cck.iso.RintisISOHeader;

@Component("fieldRH")
public class RintisHeaderGenerator extends AFieldValueGenerator {



	@Override
	public Object constructMsgValue(ISOMsg msg, String interchangeName,Map<String, Object> msgBody, FieldDefinition fd)
			throws FieldGeneratorException {

		Object val = msgBody.get(fd.getKey());
		if (!(val instanceof Map))
			throw new FieldGeneratorException("Invalid header parameter value for " + fd.getKey()); 
		Map<String, String> valMap = (Map<String, String>)val;
		RintisISOHeader header = new RintisISOHeader();
		header.setValue(valMap);

		return header;
	}

}