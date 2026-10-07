package com.cck.bean;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

/**
 * 
 * @author acrcoovy
 * 2022-11-10, do sign-on, sign-off, keychange for Alcorn D2
 */
public class AlcornD2Interchange extends AlcornB5Interchange{

	public static final String KEY_GEN_NIC = "101";

	@Override
	protected ISOMsg constructKeychangeMsg()throws ISOException {
		return constructNetworkMsg("101");
	}

	@Override
	protected String createLabel(String nic) {
		if(ECHOTEST_NIC.equals(nic)) {
			return "eho-test";
		}else if("101".equals(nic)) {
			return "key-gen";
		}else if(SIGN_ON_NIC.equals(nic)) {
			return "sign-on";
		}else if(SIGN_OFF_NIC.equals(nic)) {
			return "sign-off";
		}else {
			return "unknown";
		}
	}
}
