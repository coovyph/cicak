package com.cck.bean;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.MUX;

import lombok.extern.slf4j.Slf4j;

/**
 * Based on JAGO ATM terminal implementation
 */
@Slf4j
public class JagoAtmB5Interchange extends AlcornB5Interchange{
	@Override
	public boolean doKeyChange(MUX mux) {
		ISOMsg msg = null;
		try{
			msg = constructNetworkMsg(AlcornB5Interchange.KEY_REQ_NIC);
		}catch(ISOException e) {
			log.error("Cannot construct key change network request message",e);
			return false;
		}

		ISOMsg rsp = null;
		try {
			rsp = mux.request(msg,30000);
		}catch(ISOException ioe) {
			log.error("Exception when send key request");
		}

		return (rsp!=null && "00".equals(rsp.getString(39)));
	}
}
