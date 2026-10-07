package com.cck.dto;

import java.util.HashMap;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class CckHeaders extends HashMap<String,String>{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	public static final String INTERCHANGE = "interchange";
	
	public void setInterchange(String interchange) {
		put(INTERCHANGE, interchange);
	}
	
	public String getInterchange() {
		return get(INTERCHANGE);
	}

}
