package com.cck.bean;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.MUX;
import org.jpos.security.SecureDESKey;

public class MastercardInterchange extends AlcornB5Interchange{

	public static final String MC_SIGN_ON = "001";
	public static final String MC_SIGN_OFF = "002";

	private String memberGroupId;
	private String finNetCode;
	private boolean useVariant;
	private int keyCounter=0;

	
	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		// TODO Auto-generated method stub
		super.setConfiguration(cfg);
		this.memberGroupId = cfg.get("member-group-id");
		this.finNetCode = cfg.get("financial-net-code","DAG");
		this.useVariant = cfg.getBoolean("use-variant",true);
	}
	@Override
	protected ISOMsg constructNetworkMsg(String nic) throws ISOException {
		// TODO Auto-generated method stub
		//7,11,33,70
		ISOMsg msg = super.constructNetworkMsg(nic);
		msg.set(2,this.memberGroupId);
		//bank net reference id
		String ranReference = String.valueOf(System.currentTimeMillis());
		ranReference = ranReference.substring(ranReference.length()-10);
		msg.set(63,this.finNetCode + ranReference);
		return msg;

	}

	@Override
	public boolean doSignOn(MUX mux) {
		// TODO Auto-generated method stub
		return doGeneralNetworkMsg(mux,MC_SIGN_ON);
	}

	@Override
	public boolean doSignOff(MUX mux) {
		// TODO Auto-generated method stub
		return doGeneralNetworkMsg(mux,MC_SIGN_OFF);
	}

	@Override
	protected boolean constructAdditionalKeyData(ISOMsg reqMsg) {
		// TODO Auto-generated method stub
		SecureDESKey kwk = constructWorkingKey();
		if(kwk==null) {
			return false;
		}

		if(this.useVariant) {
			return constructVariantAdditionalData(reqMsg, kwk);
		}else {
			//not implemented yet
			return false;
		}
	}

	protected boolean constructVariantAdditionalData(ISOMsg reqMsg,SecureDESKey kwk) {
		StringBuilder sb  = new StringBuilder();
		sb.append("PK00");
		this.keyCounter++;
		sb.append(ISOUtil.zeropad(keyCounter, 2));
		sb.append(ISOUtil.hexString(kwk.getKeyBytes()));
		sb.append(ISOUtil.hexString(kwk.getKeyCheckValue()));

		reqMsg.set(48,sb.toString());
		return true;
	}

}
