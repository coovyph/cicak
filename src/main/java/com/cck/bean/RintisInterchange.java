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
public class RintisInterchange extends AInterchange implements Runnable{

	public static final String RINTIS_ECHO_TEST = "301";
	public static final String RINTIS_SIGN_ON = "001";
	public static final String RINTIS_SIGN_OFF = "002";
	public static final String RINTIS_KEYCHANGE = "162";

	private String idMemberBank;
	private int keyCounter = 0;


	private Thread t;

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		super.setConfiguration(cfg);
		this.idMemberBank = cfg.get("cbc-member");
	}

	@Override
	public boolean doSignOn(MUX mux) {
		ISOMsg msg = null;
		try{
			msg = constructNetworkMsg(RINTIS_SIGN_ON);
		}catch(Exception e) {
			getLog().error("Exception when create sign-on message",e);
			return false;
		}
		msg.set(FieldId.F048_ADDITIONAL_INFO,"11001111M00360");

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
			msg = constructNetworkMsg(RINTIS_SIGN_OFF);
		}catch(Exception e) {
			getLog().error("Exception when create sign-off message",e);
			return false;
		}
		msg.set(FieldId.F048_ADDITIONAL_INFO,"11001111M00360");

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
			msg = constructNetworkMsg(RINTIS_KEYCHANGE);
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
		msg.set(53,"0003000000000000");
		msg.set(123,constructKwkAdditionalData(kwkUnderKek));
		msg.set(FieldId.F048_ADDITIONAL_INFO,"11001111M00360");

		return msg;

	}

	private String constructKwkAdditionalData(byte[] kwkUnderKek) {
		StringBuilder sb = new StringBuilder();
		sb.append("CSM(MCL/KSM RCV/");
		sb.append(this.idMemberBank);
		sb.append(" ORG/360002 KD/");
		sb.append(ISOUtil.hexString(kwkUnderKek));
		sb.append(" CTP/");
		this.keyCounter++;
		sb.append(ISOUtil.zeropad(keyCounter, 14));
		sb.append(" )");
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

		getLog().info("Here doing key change...");
		if(!mux.isConnected()) {
			getLog().info("Cannot send keychange message, the host is not connected");
			return false;
		}

		SecureDESKey kwk = constructWorkingKey();
		if(kwk==null) {

			getLog().info("Cannot generate kwk...");
			return false;
		}

		getLog().info("Before generate request message");
		ISOMsg msg = null;
		try {
			msg = prepareKeychangeMessage(kwk);
		}catch(Exception e) {
			getLog().error(e);
		}
		getLog().info("After generate request message");
		if(msg==null) {
			getLog().info("Cannot generate request message");
			return false;
		}
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


		KeyStoreManager.getInstance().setKey(SMAdapter.TYPE_ZPK, getName() + "-acq", kwk);
		KeyStoreManager.getInstance().setKey(SMAdapter.TYPE_ZPK, getName() + "-iss", kwk);
		KeyStoreManager.getInstance().persist();

		return true;
	}

	@Override
	protected boolean doEchoTest(MUX mux) {
		ISOMsg msg = null;
		try{
			msg = constructNetworkMsg(RINTIS_ECHO_TEST);
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
	public void run() {
		while(running()) {

			MUX mux = retrieveMUX();
			if(mux==null) {
				try {
					Thread.sleep(5000);
				}catch(InterruptedException ie) {}
				continue;
			}
			networkManagement(mux);
		}

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
	protected void stopService() throws Exception {
		super.stopService();
		if(this.isNetworkManagementEnable()) {
			this.t.join(5000);
		}
	}

}
