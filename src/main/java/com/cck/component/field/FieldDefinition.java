package com.cck.component.field;

import lombok.Data;

@Data
public class FieldDefinition {
	public static final String SEPARATOR = "_";

	private String suffix;
	private String fieldId;
	private String key;

	public FieldDefinition() {
		super();
		//default
	}
	public FieldDefinition(String key,String suffix,String fieldId) {
		setSuffix(suffix);
		setFieldId(fieldId);
		setKey(key);
	}

	public void parse(String prKey) throws FieldDefinitionException{
		this.key = prKey;
		int idx = key.indexOf(SEPARATOR);
		int nextIdx = 0;
		if(idx==-1) {
			idx=1;
			nextIdx = 1;
		}else {
			nextIdx = idx+1;
		}

		this.suffix = key.substring(0,idx).toUpperCase();
		this.fieldId = key.substring(nextIdx);
		if(this.fieldId.isEmpty())throw new FieldDefinitionException("Invalid empty field id");

	}

}
