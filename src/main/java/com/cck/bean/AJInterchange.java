package com.cck.bean;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.MUX;
import org.jpos.security.SMAdapter;
import org.jpos.security.SecureDESKey;

import com.cck.component.KeyStoreManager;
import com.cck.util.CicakMsgUtility;
import com.cck.util.FieldId;
import com.cck.util.ResponseCodeUtil;

/**
 * 
 * @author acrcoovy
 * 2022-07-15, do sign-on, sign-off, keychange
 */
public class AJInterchange extends AInterchange implements Runnable{

	public static final String ECHO_TEST = "301";
	public static final String SIGN_ON = "001";
	public static final String SIGN_OFF = "002";

	public static final String KEY_CHANGE_NIC = "101";
	public static final String CUT_OFF_NIC = "201";


	private Thread t;
	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		super.setConfiguration(cfg);
	}

	@Override
	protected void startService() throws Exception {
		super.startService();
		if(isNetworkManagementEnable()) {
			t = new Thread(this);
			t.start();
		}
	}

	@Override
	public boolean doSignOn(MUX mux) {
		ISOMsg msg = null;
		try{
			msg = constructNetworkMsg(SIGN_ON);
		}catch(Exception e) {
			getLog().error("Exception when create sign-on message",e);
			return false;
		}

		if(!mux.isConnected()) {
			getLog().info("Cannot send logon message, the host is not connected");
			return false;
		}

		ISOMsg rsp =null;
		try{
			rsp = mux.request(msg, getRequestTimeout());
		}catch(ISOException ie) {
			getLog().error("Exception occur when send logon message",ie);
			return false;
		}

		if(rsp==null) {
			getLog().info("No response when do logon");
			return false;
		}

		if(!ResponseCodeUtil.isApproved(rsp.getString(39))) {
			getLog().info("Receive no approved response code when do logon");
			return false;
		}

		return true;
	}

	@Override
	public boolean doSignOff(MUX mux) {
		ISOMsg msg = null;
		try{
			msg = constructNetworkMsg(SIGN_OFF);
		}catch(Exception e) {
			getLog().error("Exception when create sign-off message",e);
			return false;
		}

		if(!mux.isConnected()) {
			getLog().info("Cannot send sign-off message, the host is not connected");
			return false;
		}

		ISOMsg rsp =null;
		try{
			rsp = mux.request(msg, getRequestTimeout());
		}catch(ISOException ie) {
			getLog().error("Exception occur when send sign-off message",ie);
			return false;
		}

		if(rsp==null) {
			getLog().info("No response when do sign-off");
			return false;
		}

		if(!ResponseCodeUtil.isApproved(rsp.getString(39))) {
			getLog().info("Receive no approved response code when do sign-off");
			return false;
		}

		return true;

	}


	private ISOMsg prepareKeychangeMessage(SecureDESKey kwk) {
		ISOMsg msg = null;
		try{
			msg = constructNetworkMsg(KEY_CHANGE_NIC);
		}catch(Exception e) {
			getLog().error("Exception when create keychange message",e);
			return null;
		}

		byte[] kwkUnderKek = null;
		try{
			kwkUnderKek = CicakMsgUtility.exportKey(kwk, getMasterKey(), getSecurityModule());
		}catch(Exception e) {
			getLog().error("Cannot extract kwk under kek",e);
			return null;
		}
		msg.set(FieldId.F048_ADDITIONAL_INFO,constructKwkAdditionalData(kwkUnderKek,kwk.getKeyCheckValue()));

		return msg;

	}

	private String constructKwkAdditionalData(byte[] kwkUnderKek,byte[] kcv) {
		StringBuilder sb = new StringBuilder();
		sb.append("U");
		sb.append(ISOUtil.hexString(kwkUnderKek));
		sb.append(ISOUtil.hexString(kcv));
		sb.append(ISOUtil.hexString(kwkUnderKek));
		sb.append(ISOUtil.hexString(kcv));
		return sb.toString();
	}

	private SecureDESKey constructWorkingKey() {
		SecureDESKey kek = getMasterKey();
		if(kek==null) {
			getLog().error("Cannot do key exchange, kek master key is not set");
			return null;
		}
		SecureDESKey kwk = null;
		try {
			kwk = CicakMsgUtility.generateNetworkKey(kek.getKeyLength(), getSecurityModule());

		}catch(Exception e){
			getLog().error("Cannot construct new working key",e);
			return null;
		}
		return kwk;
	}
	@Override
	public boolean doKeyChange(MUX mux) {


		if(!mux.isConnected()) {
			getLog().info("Cannot send keychange message, the host is not connected");
			return false;
		}

		SecureDESKey kwk = constructWorkingKey();
		if(kwk==null)return false;

		ISOMsg msg = prepareKeychangeMessage(kwk);
		if(msg==null)return false;
		ISOMsg rsp =null;
		try{
			rsp = mux.request(msg, getRequestTimeout());
		}catch(ISOException ie) {
			getLog().error("Exception occur when send keychange message",ie);
			return false;
		}

		if(rsp==null) {
			getLog().info("No response when do keychange");
			return false;
		}

		if(!ResponseCodeUtil.isApproved(rsp.getString(39))) {
			getLog().info("Receive no approved response code when do keychange");
			return false;
		}


		KeyStoreManager.getInstance().setKey(SMAdapter.TYPE_ZPK, getName(), kwk);
		KeyStoreManager.getInstance().persist();

		return true;
	}

	@Override
	protected boolean doEchoTest(MUX mux) {
		ISOMsg msg = null;
		try{
			msg = constructNetworkMsg(ECHO_TEST);
		}catch(Exception e) {
			getLog().error("Exception when create echo-test message",e);
			return false;
		}
		msg.set(FieldId.F048_ADDITIONAL_INFO,"11001111M00360");

		if(!mux.isConnected()) {
			getLog().info("Cannot send echo-test message, the host is not connected");
			return false;
		}

		ISOMsg rsp =null;
		try{
			rsp = mux.request(msg, getRequestTimeout());
		}catch(ISOException ie) {
			getLog().error("Exception occur when send echo-test message",ie);
			return false;
		}

		if(rsp==null) {
			getLog().info("No response when do echo-test");
			return false;
		}


		return true;
	}



	@Override
	protected void stopService() throws Exception {
		super.stopService();
		if(this.isNetworkManagementEnable()) {
			this.t.join(5000);
		}
	}

	@Override
	protected boolean isNetworkManagementEnable() {
		return isInitSignOn() || isInitEchoTest();
	}


	@Override
	protected void networkManagement(MUX mux){

		if(isUseSignOn() && !isSignedOn() && isInitSignOn()) {
			processSignOn(mux);
			if(isSignedOn() && isInitKeyChange()) {
				doKeyChange(mux);
			}
		}else if(isSignedOn() && isInitEchoTest()) {
			doEchoTest(mux);
			ISOUtil.sleep(this.echoTestDelay);
		}else {
			ISOUtil.sleep(10000);
		}
	}


	@Override
	public void run() {
		while(running()) {

			MUX mux = retrieveMUX();
			if(mux==null) {
				try {
					Thread.sleep(5000);
				}catch(InterruptedException ie) {}
				continue;
			}
			networkManagement(null);
		}

	}


	public boolean manualLoadKey(ISOMsg msg, ISOMsg rspMsg) {
		rspMsg.unset(48);
		try {
			if (!msg.getMTI().equals("0800")) {
				this.log.info("Invalid load key msg , load key message must be 0800");
				rspMsg.set(39, "96");
				return false;
			} 
		} catch (Exception e) {
			this.log.info("Invalid load key msg " + e.getMessage());
			rspMsg.set(39, "96");
			return false;
		} 
		String de48 = msg.getString(48);
		String identifier = de48.substring(0, 1);
		int idx = 1;
		if ("U".equals(identifier)) {
			String rawKey = de48.substring(idx, idx + 32);
			idx += 32;
			String kcv = de48.substring(idx, idx + 6);
			idx += 16;
			importAcquirerWorkingKey(ISOUtil.hex2byte(rawKey), kcv, false);
			rawKey = de48.substring(idx, idx + 32);
			idx += 32;
			kcv = de48.substring(idx, idx + 6);
			importIssuerWorkingKey(ISOUtil.hex2byte(rawKey), kcv, false);
			KeyStoreManager.getInstance().persist();
		} 
		return true;
	}


}
