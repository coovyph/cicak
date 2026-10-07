package com.cck.bean;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.MUX;
import org.jpos.security.SecureDESKey;

import com.cck.component.KeyStoreManager;
import com.cck.util.CicakMsgUtility;
import com.cck.util.ResponseCodeUtil;

/**
 * 
 * @author acrcoovy
 * 2022-10-29, do sign-on, sign-off, keychange for AlcornB5
 */
public class AlcornB5Interchange extends AInterchange implements Runnable{

	public static final String SIGN_ON_NIC = "061";
	public static final String ECHOTEST_NIC = "301";
	public static final String SIGN_OFF_NIC = "062";
	public static final String KEY_GEN_NIC = "162";
	public static final String KEY_REQ_NIC = "161";

	private String idMemberBank;
	private int keyCounter = 0;

	private Thread t;


	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		super.setConfiguration(cfg);
		this.idMemberBank = cfg.get("cbc-member");
	}

	@Override
	protected void startService() throws Exception {
		super.startService();

		if(isNetworkManagementEnable()) {
			getLog().info("Start service, network management is enable");
			t = new Thread(this);
			t.start();
		}
	}


	@Override
	protected void stopService() throws Exception {
		super.stopService();
		if(this.isNetworkManagementEnable()) {
			getLog().info("Stop service, network management is enable");
			this.t.join(5000);
		}
	}


	@Override
	protected ISOMsg constructNetworkMsg(String nic)throws ISOException {
		ISOMsg msg =super.constructNetworkMsg(nic);
		msg.set(33,this.idMemberBank);

		return msg;
	}

	protected SecureDESKey constructWorkingKey() {
		SecureDESKey kek = getMasterKey();
		if(kek==null) {
			getLog().error("Cannot do key exchange, kek master key is not set");
			return null;
		}
		SecureDESKey kwk = null;
		try {
			kwk = CicakMsgUtility.generateNetworkKey(kek.getKeyLength(), getSecurityModule());

			byte[] result = CicakMsgUtility.exportKey(kwk, kek, getSecurityModule());

			kwk.setKeyBytes(result);
		}catch(Exception e){
			getLog().error("Cannot construct new working key",e);
			return null;
		}
		return kwk;
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

	protected String createLabel(String nic) {
		if(ECHOTEST_NIC.equals(nic)) {
			return "eho-test";
		}else if(KEY_GEN_NIC.equals(nic)) {
			return "key-gen";
		}else if(SIGN_ON_NIC.equals(nic)) {
			return "sign-on";
		}else if(SIGN_OFF_NIC.equals(nic)) {
			return "sign-off";
		}else if(KEY_REQ_NIC.equals(nic)) {
			return "key-req";
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


	@Override
	public boolean doSignOn(MUX mux) {
		return doGeneralNetworkMsg(mux,SIGN_ON_NIC);

	}




	@Override
	public boolean doSignOff(MUX mux) {
		return doGeneralNetworkMsg(mux,SIGN_OFF_NIC);
	}


	protected boolean constructAdditionalKeyData(ISOMsg reqMsg) {

		SecureDESKey kwk = constructWorkingKey();
		if(kwk==null) {
			return false;
		}

		StringBuilder sb  = new StringBuilder();
		sb.append("1154PK00");
		this.keyCounter++;
		sb.append(ISOUtil.zeropad(keyCounter, 2));
		sb.append(ISOUtil.hexString(kwk.getKeyBytes()));
		sb.append(ISOUtil.hexString(kwk.getKeyCheckValue()));

		reqMsg.set(48,sb.toString());

		return true;
	}



	@Override
	public boolean doKeyChange(MUX mux) {
		NetworkProcessResponse response = new NetworkProcessResponse();
		response.setSuccess(false);

		if(!mux.isConnected()) {
			getLog().info("Cannot send keychange, mux is not connected");
			return false;
		}


		ISOMsg msg =  null;
		try{
			msg = constructKeychangeMsg();
		}catch(ISOException ise) {
			getLog().error("Cannot construct key generation message",ise);
			return false;
		}


		if(!constructAdditionalKeyData(msg)) {
			return false;
		}


		response.setReqMsg(msg);

		if(sendNetworkMsg(mux,response)) {
			//on success load the key
			return loadKey(msg.getString(48));
		}
		return false;
	}



	protected ISOMsg constructKeychangeMsg()throws ISOException {
		return constructNetworkMsg(KEY_GEN_NIC);
	}

	@Override
	public boolean doEchoTest(MUX mux) {
		return doGeneralNetworkMsg(mux,ECHOTEST_NIC);
	}


	@Override
	public void run() {
		MUX mux = retrieveMUX();
		while(running()) {
			networkManagement(mux);
		}

	}

	@Override
	public boolean manualLoadKey(ISOMsg msg, ISOMsg rspMsg) {
		rspMsg.unset(48);
		if(loadKey(msg.getString(48))) {
			rspMsg.set(39, "00");
			setKeyChanged(true);
			return true;
		}
		rspMsg.set(39, "96");
		return false; 
	}
	protected boolean loadKey(String keyData) {
		if (keyData == null || keyData.length() < 48) {
			return false;
		} 
		int idx = 10;
		byte[] keyUnderKek = ISOUtil.hex2byte(keyData.substring(idx, idx + 32));
		idx += 32;
		String kcv = keyData.substring(idx, idx + 6);
		if (importKeys(keyUnderKek, kcv)) {
			setKeyChanged(true);
			return true;
		} 

		return false;
	}
	
	protected boolean importKeys(byte[] keyBytes, String kcv) {
		try {
			SecureDESKey kwk = CicakMsgUtility.importKey(keyBytes, "ZPK", getMasterKey(), getSecurityModule());
			kwk.setKeyCheckValue(ISOUtil.hex2byte(kcv));
			KeyStoreManager keyManager = KeyStoreManager.getInstance();
			keyManager.setAcqKwkKey(getName(), kwk);
			keyManager.setIssKwkKey(getName(), kwk);
			keyManager.persist();
			return true;
		} catch (Exception e) {
			getLog().error(e, "Cannot import acq key " + ISOUtil.hexString(keyBytes));
			return false;
		} 
	}
	
}
