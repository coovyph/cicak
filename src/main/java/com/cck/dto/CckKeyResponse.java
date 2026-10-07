package com.cck.dto;

import lombok.Data;

@Data
public class CckKeyResponse {
	private boolean success;
	private String message;
	private CckKey key;

	public CckKeyResponse() {
		//do nothing
	}
	public CckKeyResponse(boolean prSuccess,String prMessage) {
		setSuccess(prSuccess);
		setMessage(prMessage);
	}
}
