package com.cck.participant;

import org.jpos.iso.ISOMsg;
import org.jpos.security.ARPCMethod;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.jpos.tlv.TLVList;
import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.Context;

import com.cck.component.BinManager;
import com.cck.component.KeyStoreManager;
import com.cck.util.EMVTag;

/**
 * 
 * @author coovy
 * 2021-07-06, doing arpc validation 
 * 
 * 2025-05-13, lookup IMKAC from bin
 */
public class ArpcGenParticipant extends ArqcValidateParticipant implements AbortParticipant{


	@Override
	protected int prepareImpl(long id,Context ctx,ISOMsg msg,ISOMsg rspMsg){

		if(!isIccTransaction(rspMsg)) {
			//skip no aprc gen
			return PREPARED;
		}

		byte[] rawIccBytes = msg.getBytes(55);
		if(rawIccBytes==null) {
			rspMsg.set(39,"96");
			return ABORTED;
		}

		TLVList tlvList = new TLVList();
		if(!unpackTlvInformations(tlvList, msg)) {
			rspMsg.set(39,"96");
			return ABORTED;
		}


		BinManager binManager = getBinManager();
		if(binManager == null) {
			rspMsg.set(39,"96");
			error("Expected BinManager is not exists...");
			return ABORTED;
		}

		KeyStoreManager ksm = getKeyStoreManager();
		if(ksm == null) {
			rspMsg.set(39,"96");
			error("Expected KeyStoreManager is not exists...");
			return ABORTED;
		}
		GeckoSecurityModule gsm = getSecurityModule();
		if(gsm==null) {
			rspMsg.set(39,"96");
			error("Expected SecurityModule is not exists...");
			return ABORTED;
		}	

		String pan = extractPan(msg);
		String panSeqNum = msg.getString(23); 

		SecureDESKey imkAc = getSecureDesKey(pan, binManager, ksm);

		if(imkAc == null) {
			rspMsg.set(39,"96");
			info("Imk-ac not found fo card " + pan);
			return ABORTED;
		}
		
		if(panSeqNum==null)panSeqNum = tlvList.getString(EMVTag._5F34_APPLICATION_PAN_SEQ_NR);
		if(panSeqNum.length()>2)panSeqNum = panSeqNum.substring(panSeqNum.length()-2);
		byte[] atc = tlvList.getValue(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER);
		byte[] arqc = tlvList.getValue(EMVTag._9F26_APPLICATION_CRYPTOGRAM);

		byte[] arc = rspMsg.getString(39).getBytes();
		try {
			byte [] arpc =	gsm.generateARPC(MKDMethod.OPTION_A,SKDMethod.EMV_CSKD, imkAc,pan,panSeqNum,arqc,atc,null,ARPCMethod.METHOD_1,arc,null );



			byte[] resultBytes = new byte[arpc.length + arc.length];

			System.arraycopy(arpc, 0, resultBytes, 0, arpc.length);
			System.arraycopy(arc, 0, resultBytes, arpc.length,arc.length);


			TLVList tlvRsp = new TLVList();
			tlvRsp.append(EMVTag._91_ISSUER_AUTHENTICATION_DATA, resultBytes);


			byte[] tlvRspBytes = tlvRsp.pack();


			rspMsg.set(55,tlvRspBytes);

			return PREPARED;
		}catch(Exception e) {
			error("Cannot create arpc",e);
			rspMsg.set(39,"96");
			return ABORTED;
		}
	}
}
