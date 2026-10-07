package com.cck.component.field;

import java.util.Map;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.springframework.stereotype.Component;

import com.cck.service.GeneratorRegistrarService;

/**
 * Use TLV tag(2 length) + length (2) + value
 */
@Component("fieldTLL")
public class TlvLLFieldGenerator extends ACompoundFieldValueGenerator{

	public TlvLLFieldGenerator(GeneratorRegistrarService rgs) {
		super(rgs);
		// TODO Auto-generated constructor stub
	}

	@Override
	public Object constructMsgValue(ISOMsg msg, String interchangeName, Map<String, Object> msgBody, FieldDefinition fd)
			throws FieldGeneratorException {
		Object objValue = msgBody.get(fd.getKey());
		String value = null;
		if(objValue instanceof Map) {			
			value = constructSubValue(msg, interchangeName, (Map)objValue);
		}else if(objValue instanceof String) {
			value = (String)objValue;
		}else {
			throw new FieldGeneratorException("Field " + fd.getKey() + " value is not string");
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append(fd.getFieldId());
		sb.append(ISOUtil.zeropad(value.length(), 2));
		sb.append(value);
		return sb.toString();
	}
	


}
