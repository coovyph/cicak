// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.cck.bean;

import java.time.LocalDateTime;

import javax.net.ssl.KeyManager;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.MUX;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.iso.QMUX;
import org.jpos.security.SMAdapter;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.jpos.util.NameRegistrar;

import com.cck.component.KeyStoreManager;
import com.cck.service.impl.Q2Service;
import com.cck.util.CicakDateUtil;
import com.cck.util.CicakMsgUtility;
import com.cck.util.FieldId;
import com.cck.util.GeckoSequencer;
import com.cck.util.ResponseCodeUtil;


public abstract class AInterchange extends QBeanSupport{

	public static final String P_INST_CODE = "inst-code";
	public static final String P_USE_SIGN_ON = "use-sign-on";
	public static final String P_USE_KEY_EXCHANGE = "use-key-exchange";
	public static final String P_INIT_SIGN_ON = "init-sign-on";
	public static final String P_INIT_ECHO_TEST = "init-echo-test";
	public static final String P_INIT_KEYCHANGE = "init-keychange";
	public static final String P_STATIC_WORKIN_KEY = "static-kwk";
	public static final String P_MUX = "mux";
	public static final String P_ISS_MGR = "iss-mgr";
	public static final String P_RSP_MGR = "rsp-mgr";
	public static final String P_SECURITY_MODULE = "sm-module";

	public static final String P_REQUEST_TIMEOUT = "request-timeout";
	public static final String P_ECHOTEST_DELAY ="echo-delay";
	public static final String P_SIGNON_DELAY = "signon-delay";
	public static final String P_KEYCHANGE_DELAY = "keychange-delay";
	public static final String P_FORWARDING_INST_ID = "forward-inst-id";

	public static final String P_READY = "ready";


	public static final int NP_SUCCESS = 0;
	public static final int NP_TIMEOUT = 1;
	public static final int NP_FAILED = 2;
	public static final int NP_DISCONNECT = 3;
	public static final int NP_EXCEPTION = 4;


	private String instCode;
	private boolean useSignOn;
	private boolean useKeyChange;


	private boolean initSignOn;
	private boolean initKeyChange;
	private boolean initEchoTest;


	protected boolean signedOn = false;
	protected boolean keyChanged = false;


	private String smName = null;
	private long requestTimeout;
	private String muxName = null;

	protected long echoTestDelay;
	protected long signonDelay;
	protected long keychangeDelay;
	private String ready;

	private String forwardingInstId;

	public synchronized void setSignedOn(boolean signedOn) {
		this.signedOn = signedOn;
	}

	public synchronized void setKeyChanged(boolean keyChanged) {
		this.keyChanged = keyChanged;
	}

	public synchronized boolean isSignedOn() {
		return signedOn;
	}

	protected String getReady() {
		return this.ready;
	}

	public synchronized boolean isKeyChanged() {
		return keyChanged;
	}

	protected String getInstitutionName() {
		return this.instCode;
	}


	public long getRequestTimeout() {
		return this.requestTimeout;
	}

	public String getInstCode() {
		return this.instCode;
	}

	protected boolean isUseSignOn() {
		return this.useSignOn;
	}

	protected boolean isUseKeyChange() {
		return this.useKeyChange;
	}

	protected boolean isInitSignOn() {
		return this.initSignOn;
	}

	protected boolean isInitKeyChange() {
		return this.initKeyChange;
	}

	protected void setInitKeyChange(boolean initKeyChange) {
		this.initKeyChange = initKeyChange;
	}

	protected boolean isInitEchoTest() {
		return this.initEchoTest;
	}



	protected void resetNetworkStatus() {
		if(isUseSignOn()) {
			this.setSignedOn(false);
		}

		if(isUseKeyChange()) {
			this.setKeyChanged(false);
		}
	}

	protected boolean isNetworkManagementEnable() {
		return isInitSignOn() || isInitKeyChange() || isInitEchoTest();
	}
	protected void networkManagement(MUX mux){

		if(isUseSignOn() && !isSignedOn() && isInitSignOn()) {
			processSignOn(mux);
		}else if(isUseKeyChange() && !isKeyChanged() && isInitKeyChange()) {
			processKeychange(mux);
		}else  if(isSignedOn() && isInitEchoTest()) {
			doEchoTest(mux);
			ISOUtil.sleep(this.echoTestDelay);
		}else {
			ISOUtil.sleep(10000);
		}
	}

	protected void processKeychange(MUX mux) {
		setKeyChanged(doKeyChange(mux));
		if(!isKeyChanged()) {
			try {
				getLog().info("Sleep" + this.keychangeDelay + " ms before retry key-exchange");
				ISOUtil.sleep(this.keychangeDelay);
			}catch(Exception e) {
			}
		}
	}
	protected void processSignOn(MUX mux) {
		setSignedOn(doSignOn(mux));
		if(!isSignedOn()) {
			try {
				getLog().info("Sleep " + this.signonDelay +" ms before re sign-on");
				ISOUtil.sleep(this.signonDelay);
			}catch(Exception e) {}
		}
	}


	public abstract boolean doSignOn(MUX mux);
	public abstract boolean doSignOff(MUX mux);
	public abstract boolean doKeyChange(MUX mux);
	protected abstract boolean doEchoTest(MUX mux);

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		super.setConfiguration(cfg);
		this.instCode = cfg.get(P_INST_CODE);
		this.initSignOn = cfg.getBoolean(P_INIT_SIGN_ON,false);
		this.initKeyChange = cfg.getBoolean(P_INIT_KEYCHANGE,false);
		this.initEchoTest = cfg.getBoolean(P_INIT_ECHO_TEST,true);
		this.forwardingInstId = cfg.get(P_FORWARDING_INST_ID, "");
		this.useSignOn = cfg.getBoolean(P_USE_SIGN_ON,true);

		this.useKeyChange = cfg.getBoolean(P_USE_KEY_EXCHANGE,false);


		this.requestTimeout = cfg.getLong(P_REQUEST_TIMEOUT,20000L);
		this.muxName = cfg.get(P_MUX,"");
		this.smName = cfg.get(P_SECURITY_MODULE,"sm");

		this.echoTestDelay = cfg.getLong(P_ECHOTEST_DELAY,60000L);
		this.signonDelay = cfg.getLong(P_SIGNON_DELAY,10000L);
		this.keychangeDelay = cfg.getLong(P_KEYCHANGE_DELAY,10000L);
		this.ready = cfg.get(P_READY,"");
	}

	public long getSignonDelay() {
		return signonDelay;
	}

	public long getKeychangeDelay() {
		return keychangeDelay;
	}

	public long getEchoTestDelay() {
		return echoTestDelay;
	}

	public MUX retrieveMUX() {
		try {
			return QMUX.getMUX(this.muxName);
		}catch(Exception e) {
			getLog().error(e, "Cannot retrieve mux " + this.muxName);
		}
		return null;
	}


	protected ISOMsg constructNetworkMsg(String nic)throws ISOException {
		ISOMsg msg = new ISOMsg();
		msg.setMTI("0800");
		msg.set(FieldId.F007_TRANSMISSION_DATE_TIME,CicakDateUtil.formatGmtDateTime(LocalDateTime.now()));
		msg.set(FieldId.F011_STAN,GeckoSequencer.constructStan());

		msg.set(FieldId.F070_NETWORK_INFO_CODE,nic);
		return msg;
	}


	public SecureDESKey getImkAc() {
		return KeyStoreManager.getInstance().getKey(SMAdapter.TYPE_MK_AC,getName());
	}
	public SecureDESKey getMasterKey() {
		return KeyStoreManager.getInstance().getKey(SMAdapter.TYPE_ZMK,getName());
	}

	public SecureDESKey getWorkingKey() {
		return KeyStoreManager.getInstance().getKey(SMAdapter.TYPE_ZMK,getName());
	}

	public GeckoSecurityModule getSecurityModule() {
		return Q2Service.getBean(GeckoSecurityModule.class);
	}


	public boolean importKey(byte[] keyBytes,String keyCheckValue) {
		try {
			GeckoSecurityModule gsm = getSecurityModule();
			SecureDESKey kwk = CicakMsgUtility.importKey(keyBytes,SMAdapter.TYPE_ZPK, getMasterKey(), gsm);

			if(ISOUtil.hexString(kwk.getKeyCheckValue()).equals(keyCheckValue)) {
				getLog().info("Set new working key with kcv  " + keyCheckValue);
				KeyStoreManager keyManager = KeyStoreManager.getInstance();
				keyManager.setKey(SMAdapter.TYPE_ZPK,getName(), kwk);
				keyManager.persist();
				return true;
			}else {
				getLog().info("Cannot set new working key , kcv is " +  ISOUtil.hexString(kwk.getKeyCheckValue()) + " expected is " + keyCheckValue);
			}
		}catch(Exception e) {
			getLog().error(e,"Cannot import key " + ISOUtil.hexString(keyBytes));
		}
		return false;
	}

	@Override
	protected void initService() throws Exception {
		super.initService();
		NameRegistrar.register(AInterchange.constructName(getName()), this);
	}

	@Override
	protected void startService() throws Exception {
		super.startService();
		resetNetworkStatus();
		this.signedOn = !this.useSignOn; // anggap saja sudah signon kalau tidak menggunakan signon

		getLog().info("Using key change " + this.useKeyChange);
		this.keyChanged = !this.useKeyChange;
	}


	protected int processNetworkRequest(MUX mux,String nic) {
		ISOMsg msg = null;
		try{
			msg = constructNetworkMsg(nic);
		}catch(Exception e) {
			getLog().error(e,"Exception occur on process network " + nic);
			return NP_EXCEPTION;
		}
		return processNetworkRequest(mux, msg);
	}


	protected int processNetworkRequest(MUX mux,ISOMsg msg) {

		if(mux.isConnected()) {
			try {
				ISOMsg rsp = mux.request(msg, this.requestTimeout);
				if(rsp==null) {
					getLog().info("No response when send network request");
					return NP_TIMEOUT;
				}else {
					if(ResponseCodeUtil.isApproved(rsp.getString(FieldId.F039_RESPONSE_CODE))) {
						return NP_SUCCESS;
					}else {
						getLog().info("network process has failed " + rsp.getString(39));
						return NP_FAILED;
					}
				}
			}catch(Exception e) {
				String nic = msg.getString(70);
				getLog().error(e,"Exception occur on process network " + nic);
				return NP_EXCEPTION;
			}
		}else {
			//disconnected
			getLog().info("No connection to host reset all networks status");
			resetNetworkStatus();
			return NP_DISCONNECT;
		}
	}



	@Override
	protected void destroyService() throws Exception {
		super.destroyService();
		NameRegistrar.unregister(AInterchange.constructName(getName()));
	}


	public void startAutomaticNmm() {
	}

	public void stopAutomaticNmm() {
	}

	public boolean isAutomaticNmmStarted() {
		return true;
	}

	private static String constructName(String name) {
		return "ICH" + name;
	}

	public String getForwardingInstId() {
		return forwardingInstId;
	}

	public boolean manualSignOn() {
		return doSignOn(retrieveMUX());
	}

	public boolean manualKeyChange() {
		return doKeyChange(retrieveMUX());
	}

	public boolean manualSignOff() {
		return doSignOff(retrieveMUX());
	}

	public boolean manualEchoTest() {
		return doEchoTest(retrieveMUX());
	}


	public String getClearName() {
		return getName().substring(3);
	}

	public static final AInterchange getInterchange(String name) {
		Object obj = NameRegistrar.getIfExists(constructName(name));
		if(obj==null)return null;
		if(obj instanceof AInterchange) {
			return (AInterchange)obj;
		}
		return null;
	}

	public boolean importAcquirerWorkingKey(byte[] keyBytes, String keyCheckValue, boolean persist) {
		try {
			SecureDESKey kwk = CicakMsgUtility.importKey(keyBytes, "ZPK", getMasterKey(), getSecurityModule());
			if (ISOUtil.hexString(kwk.getKeyCheckValue()).equals(keyCheckValue)) {
				getLog().info("Set new  acq working key with kcv  " + keyCheckValue);
				KeyStoreManager keyManager = KeyStoreManager.getInstance();
				keyManager.setAcqKwkKey(getName(), kwk);
				if (persist)
					keyManager.persist(); 
				return true;
			} 
			getLog().info("Cannot set new acq working key , kcv is " + ISOUtil.hexString(kwk.getKeyCheckValue()) + 
					" expected is " + keyCheckValue);
		} catch (Exception e) {
			getLog().error(e, "Cannot import acq key " + ISOUtil.hexString(keyBytes));
		} 
		return false;
	}

	public boolean importIssuerWorkingKey(byte[] keyBytes, String keyCheckValue, boolean persist) {
		try {
			SecureDESKey kwk = CicakMsgUtility.importKey(keyBytes, "ZPK", getMasterKey(), getSecurityModule());
			if (ISOUtil.hexString(kwk.getKeyCheckValue()).equals(keyCheckValue)) {
				getLog().info("Set new iss working key with kcv  " + keyCheckValue);
				KeyStoreManager keyManager = KeyStoreManager.getInstance();
				keyManager.setIssKwkKey(getName(), kwk);
				if (persist)
					keyManager.persist(); 
				return true;
			} 
			getLog().info("Cannot set new iss working key , kcv is " + ISOUtil.hexString(kwk.getKeyCheckValue()) + 
					" expected is " + keyCheckValue);
		} catch (Exception e) {
			getLog().error(e, "Cannot import iss key " + ISOUtil.hexString(keyBytes));
		} 
		return false;
	}

	public  boolean manualLoadKey(ISOMsg msg, ISOMsg rspMsg) {
		return false;
	}
	
}
