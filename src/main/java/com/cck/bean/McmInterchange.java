package com.cck.bean;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.MUX;
import org.jpos.security.SecureDESKey;

import com.cck.util.ResponseCodeUtil;

/**
 * 
 * @author coovy
 * 2023-01-28, handle incoming message card upload from CMS to EPS
 */
public class McmInterchange extends AInterchange{
	public static final String NIC_SIGN_ON = "061";
	public static final String NIC_SIGN_OFF = "062";
	public static final String NIC_ECHO_TEST = "301";

	private String createLabel(String nic) {
		if(NIC_ECHO_TEST.equals(nic)) {
			return "eho-test";
		}else if("101".equals(nic)) {
			return "key-gen";
		}else if(NIC_SIGN_ON.equals(nic)) {
			return "sign-on";
		}else if(NIC_SIGN_OFF.equals(nic)) {
			return "sign-off";
		}else {
			return "unknown";
		}
	}
	protected boolean doGeneralNetworkMsg(MUX mux,String nic) {
		NetworkProcessResponse response = new NetworkProcessResponse();
		response.setSuccess(false);

		ISOMsg msg = null;
		try{
			msg = constructNetworkMsg(nic);
		}catch(Exception e) {
			getLog().error("Exception when create " + createLabel(nic),e);
			return false;
		}

		response.setReqMsg(msg);
		return sendNetworkMsg(mux,response);
	}

	private boolean sendNetworkMsg(MUX mux, NetworkProcessResponse npr) {
		String nic = npr.getReqMsg().getString(70);
		if(!mux.isConnected()) {
			getLog().info("Cannot send " + createLabel(nic) + " message, the host is not connected");
			return false;
		}
		ISOMsg rsp =null;
		try{
			rsp = mux.request(npr.getReqMsg(), getRequestTimeout());
		}catch(ISOException ie) {
			getLog().error("Exception occur when send  " + createLabel(nic) + " message",ie);
			return false;
		}

		if(rsp==null) {
			getLog().info("No response when send " + createLabel(nic) + " message");
			return false;
		}
		npr.setRspMsg(rsp);
		if(ResponseCodeUtil.isApproved(rsp.getString(39))) {
			npr.setSuccess(true);
			return true;
		}else {
			getLog().info("Receive not approved response code when send " + createLabel(nic));
			return false;
		}

	}
	@Override
	public boolean doSignOn(MUX mux) {
		return doGeneralNetworkMsg(mux,NIC_SIGN_ON);
	}

	@Override
	public boolean doSignOff(MUX mux) {
		return doGeneralNetworkMsg(mux,NIC_ECHO_TEST);
	}

	@Override
	protected boolean doEchoTest(MUX mux) {
		return doGeneralNetworkMsg(mux,NIC_ECHO_TEST);
	}

	@Override
	public boolean doKeyChange(MUX mux) {
		//no key change implementation
		return true;
	}
}
