package com.cck.component.field;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.cck.service.GeneratorRegistrarService;

public abstract class ACompoundFieldValueGenerator extends AFieldValueGenerator{

	protected GeneratorRegistrarService registrar;

	public ACompoundFieldValueGenerator(GeneratorRegistrarService rgs) {
		this.registrar = rgs;
	}

	protected String constructSubValue(ISOMsg msg,String interchangeName,Map map)throws FieldGeneratorException {
		StringBuilder sb = new StringBuilder();
		Set<String> set = map.keySet();
		Iterator<String> iter = set.iterator();
		while(iter.hasNext()) {
			contructSubFieldValue(msg,interchangeName,map,iter.next(),sb);
		}
		return sb.toString();
	}

	private void contructSubFieldValue(ISOMsg msg,String interchangeName,Map<String,Object> map, String key,StringBuilder sb)throws FieldGeneratorException {
		FieldDefinition fd = new FieldDefinition();
		try {
			fd.parse(key);
		}catch(FieldDefinitionException fe) {
			throw new FieldGeneratorException("The sub msg field " + key + " is not valid");
		}

		IFieldValueGenerator valueGenerator = this.registrar.getFieldValueGenerator(fd.getSuffix());
		if(valueGenerator == null) {
			throw new FieldGeneratorException("Cannot find generator for generator-id " + fd.getSuffix() + " , sub-field " + key);			
		}

		Object obj = valueGenerator.constructMsgValue(msg,interchangeName, map, fd);
		if(obj instanceof byte[]) {
			sb.append(ISOUtil.hexString((byte[])obj));
		}else {
			sb.append(obj.toString());
		}

	}

}
