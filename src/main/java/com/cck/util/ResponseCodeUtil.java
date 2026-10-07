package com.cck.util;

public class ResponseCodeUtil {
	public static final String DONOT_HONOUR = "05";

	private ResponseCodeUtil() {

	}

	public static final boolean isApproved(String responseCode) {
		return "00".equals(responseCode);
	}

}
