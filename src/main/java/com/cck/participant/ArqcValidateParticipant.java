package com.cck.participant;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.jpos.tlv.TLVList;
import org.jpos.transaction.Context;

import com.cck.component.BinManager;
import com.cck.component.KeyStoreManager;
import com.cck.service.impl.Q2Service;
import com.cck.util.CicakMsgUtility;
import com.cck.util.ISOContextUtil;
import com.cck.util.EMVTag;
import com.cck.util.PanUtil;

/**
 * 
 * @author coovy
 * 2021-07-06, doing arqc validation 
 * 
 * 2025-05-13, lookup IMKAC from bin
 */
public class ArqcValidateParticipant extends BaseResponseParticipant{

	public static final String ARQC_VAL = "arqc_val";

	private List<String> iccPanEntryModes;

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		this.iccPanEntryModes = new ArrayList<>();

		String rawPosEntryModes = cfg.get("pem","");
		StringTokenizer tokenz = new StringTokenizer(rawPosEntryModes);
		while(tokenz.hasMoreTokens()) {
			this.iccPanEntryModes.add(tokenz.nextToken());
		}
	}

	protected GeckoSecurityModule getSecurityModule() {
		return Q2Service.getBean(GeckoSecurityModule.class);
	}

	protected KeyStoreManager getKeyStoreManager() {
		return Q2Service.getBean(KeyStoreManager.class);
	}

	protected BinManager getBinManager() {
		return Q2Service.getBean(BinManager.class);
	}

	protected SecureDESKey getSecureDesKey(String pan,BinManager binManager,KeyStoreManager keyStoreManager) {
		String imkAcName = binManager.getImkAcKeyName(pan);
		if(imkAcName == null) {
			imkAcName = KeyStoreManager.DEFAULT_IMK_AC;
		}
		
		return keyStoreManager.getKey(imkAcName);
	}

	private boolean validateArqc(String pan,String panSeqNum,byte[] atc,byte[] crypto,byte[] arqc) {
		BinManager binManager = getBinManager();
		if(binManager == null) {
			error("Expected BinManager is not exists...");
			return false;
		}

		KeyStoreManager ksm = getKeyStoreManager();
		if(ksm == null) {
			error("Expected KeyStoreManager is not exists...");
			return false;
		}
		GeckoSecurityModule gsm = getSecurityModule();
		if(gsm==null) {
			error("Expected SecurityModule is not exists...");
			return false;
		}	



		SecureDESKey imkAc = getSecureDesKey(pan, binManager, ksm);

		if(imkAc == null) {
			info("Imk-ac not found fo card " + pan);
			return false;
		}
		try{
			return gsm.verifyARQC(MKDMethod.OPTION_A,SKDMethod.EMV_CSKD,imkAc,pan,panSeqNum,arqc,atc, null, crypto);
		}catch(Exception e) {
			error("Invalid arqc " ,e);
			return false;
		}
	}

	private void setArqcValidated(Context ctx) {
		ISOContextUtil.putSession(ctx, ARQC_VAL, "1");
	}

	protected boolean isArqcValidated(Context ctx) {
		return "1".equals(ISOContextUtil.getSessionString(ctx, ARQC_VAL,"0"));
	}

	protected boolean unpackTlvInformations(TLVList tlvList,ISOMsg msg) {
		byte []rawIccBytes = msg.getBytes(55);
		if(rawIccBytes==null)return false;

		try {
			tlvList.unpack(rawIccBytes);
			return true;
		}catch(Exception e) {
			error("Cannot unpack raw icc data '" + ISOUtil.hexString(rawIccBytes) + "'");
			return false;
		}
	}

	protected boolean isIccTransaction(ISOMsg msg) {
		String de22 = msg.getString(22);

		if(de22==null || de22.length()<3)return false;
		String pem = de22.substring(0,2);

		return this.iccPanEntryModes.contains(pem);
	}

	private boolean validateTranType(TLVList tlvList,ISOMsg msg) {
		String procCode = msg.getString(3);
		if(procCode==null) {
			info("Processing code is not set, invalid transaction type");
			return false;
		}

		String tranType = procCode.substring(0,2);

		byte[] emvTranTypeBytes = tlvList.getValue(EMVTag._9C_TRANSACTION_TYPE);
		if(emvTranTypeBytes==null) {
			info("Tag 9C is not set");
			return false;
		}

		return ISOUtil.hexString(emvTranTypeBytes).equals(tranType);

	}

	protected String extractPan(ISOMsg msg) {
		String pan = msg.getString(2);
		if(pan == null) pan = PanUtil.extractPanFromTrack2(msg.getString(35));
		return pan;
	}

	@Override
	protected int prepareImpl(long id,Context ctx,ISOMsg msg,ISOMsg rspMsg){

		TLVList tlvList = new TLVList();

		if(!isIccTransaction(rspMsg)) {
			//skip no checking
			return PREPARED;
		}

		if(!unpackTlvInformations(tlvList, msg)) {
			rspMsg.set(39,"05");
			info("Cannot unpack tlv informations...");
			return ABORTED;
		}

		String pan = extractPan(msg);

		//validate tran type must same with tag 9C
		if(!validateTranType(tlvList,msg)) {
			rspMsg.set(39,"05");
			return ABORTED;
		}

		String panSeqNum = msg.getString(23); 
		if(panSeqNum==null)panSeqNum = tlvList.getString(EMVTag._5F34_APPLICATION_PAN_SEQ_NR);
		if(panSeqNum.length()>2)panSeqNum = panSeqNum.substring(panSeqNum.length()-2);
		byte[] atc = tlvList.getValue(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER);
		byte[] arqc = tlvList.getValue(EMVTag._9F26_APPLICATION_CRYPTOGRAM);
		byte[] crypto = null;
		try{
			crypto = CicakMsgUtility.createNSICCSCryptogramData(tlvList);
		}catch(Exception e) {
			rspMsg.set(39,"05");
			error("Cannot create cryptogram data");
			return ABORTED;
		}

		if(validateArqc(pan, panSeqNum, atc, crypto, arqc)) {
			setArqcValidated(ctx);
			return PREPARED;
		}else {

			info("ARQC checking failed...");
			rspMsg.set(39,"05");
			return ABORTED;
		}
	}

}
