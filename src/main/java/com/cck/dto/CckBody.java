package com.cck.dto;

import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
@JsonInclude(Include.NON_NULL)
public class CckBody extends LinkedHashMap<String, Object>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String getValueAsString(String id) {
		Object val = get(id);
		if(val==null)return null;
		return val.toString();
	}


}
