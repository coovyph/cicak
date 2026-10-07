package com.cck.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;
import org.jpos.security.SMAdapter;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.jpos.tlv.TLVList;
import org.jpos.util.NameRegistrar;

public class CicakMsgUtility {
	private static final SimpleDateFormat TRAN_DATE_LOCAL_FORMAT = new SimpleDateFormat("yyMMdd");
	private static final String HEX_PAD = "80";
	private static final String HEX_NEXT_PAD = "0";

	public static final String DEFAULT_CARD_VERIFICATION_RESULT = "020000";

	public static final String TERMINAL_VERIFICATION_RESULT = "8000048000";

	private static final String constructUnpredictableNumber(){
		byte[] result = new byte[4];
		Random random = new Random();
		for(int i=0;i<4;i++) {
			result[i] = (byte)random.nextInt(255);
		}
		return ISOUtil.hexString(result);
	}
	private static final GeckoSecurityModule retrieveSecurityModule(String name)throws Exception {
		Object obj = NameRegistrar.getIfExists(name);
		if(obj==null) {
			throw new Exception("Cannot find security-module " + name);
		}

		if(obj instanceof GeckoSecurityModule) {		
			return (GeckoSecurityModule)obj;
		}
		throw new Exception("Bean object " + name + " is not valid security module");
	}


	public static final SecureDESKey generateNetworkKey(SecureDESKey kek,GeckoSecurityModule adapter)throws Exception {

		SecureDESKey kwkKey =  generateNetworkKey(kek.getKeyLength(),adapter);


		byte[] result = adapter.exportKey(kwkKey, kek);

		kwkKey.setKeyBytes(result);

		return kwkKey;
	}

	public static final SecureDESKey generateNetworkKey(short length,GeckoSecurityModule adapter)throws Exception{

		return  adapter.generateKey(length, KeyUtil.constructKeyType(SMAdapter.TYPE_ZPK, "0", length));

	}

	public static final byte[] exportKey(SecureDESKey kwk,SecureDESKey kek,GeckoSecurityModule adapter) throws Exception {
		return adapter.exportKey(kwk, kek);		
	}

	public static final SecureDESKey importKey(byte[] encryptedKey,String mainKeyType,SecureDESKey kek,GeckoSecurityModule adapter) throws Exception {
		short keyLength = KeyUtil.calculateKeyLength(encryptedKey);
		String keyType = KeyUtil.constructKeyType(mainKeyType, "0", keyLength);
		return adapter.importKey(keyLength, keyType, encryptedKey, kek, false);
	}
	private static final byte[] constructARQC(GeckoSecurityModule adapter,String imkAc,
			String pan,String panSeqNum,byte[] atc,byte[] crypto)
					throws Exception{

		SecureDESKey mkac = KeyUtil.constructKey(imkAc, SMAdapter.TYPE_MK_AC,"0");


		return adapter.generateARQC(MKDMethod.OPTION_A, SKDMethod.EMV_CSKD, mkac, pan, panSeqNum, atc, null, crypto);
	}




	public static final void constructNsiccsAcqICCData(GeckoSecurityModule adapter,
			ISOMsg msg,
			String imkAc,
			String dfName,
			String aip,
			String pan,
			String panSequenceNo,
			String atc,
			String terminalCurrencyCode,
			String terminalType,
			String terminalCapabilities,
			String issAppData
			)throws Exception {


		String transactionAmount = msg.getString(FieldId.F004_AMOUNT);

		TLVList tlvList = new TLVList();

		tlvList.append(EMVTag._84_DF_NAME,dfName);
		tlvList.append(EMVTag._9F02_AMOUNT_AUTHORIZED_NUMERIC,ISOUtil.zeropad(transactionAmount, 12));
		//CARD AID APPLICATION
		tlvList.append(EMVTag._9F03_AMOUNT_OTHER_NUMERIC,"000000000000");

		String transactionCurrencyCode = msg.getString(FieldId.F49_CURRENCY_CODE);
		if(transactionCurrencyCode==null) {
			transactionCurrencyCode = "360";
		}
		transactionCurrencyCode="0" + transactionCurrencyCode;

		tlvList.append(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE,transactionCurrencyCode);

		String tranDateLocal = TRAN_DATE_LOCAL_FORMAT.format(new Date());
		tlvList.append(EMVTag._9A_TRANSACTION_DATE, tranDateLocal);

		String processingCode = msg.getString(FieldId.F003_PROCCODE);
		if(processingCode!=null && !processingCode.isEmpty()) {
			String tranType = processingCode.substring(0,2);
			tlvList.append(EMVTag._9C_TRANSACTION_TYPE,tranType);
		}

		tlvList.append(EMVTag._9F1A_TERMINAL_COUNTRY_CODE,terminalCurrencyCode);
		tlvList.append(EMVTag._95_TERMINAL_VERIFICATION_RESULTS,TERMINAL_VERIFICATION_RESULT);

		tlvList.append(EMVTag._9F37_UNPREDICTABLE_NUMBER,constructUnpredictableNumber());
		tlvList.append(EMVTag._82_APPLICATION_INTERCHANGE_PROFILE,aip);
		tlvList.append(EMVTag._5F34_APPLICATION_PAN_SEQ_NR,panSequenceNo);
		tlvList.append(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER,atc);

		tlvList.append(EMVTag._9F10_ISSUER_APPLICATION_DATA,issAppData);



		byte[] crypto = createNSICCSCryptogramData(tlvList);
		byte[] arqc = CicakMsgUtility.constructARQC(adapter,imkAc,pan,panSequenceNo,ISOUtil.hex2byte(atc),crypto);

		tlvList.append(EMVTag._9F26_APPLICATION_CRYPTOGRAM, ISOUtil.hexString(arqc));

		//CRYPTOGRAM INFO DATA
		tlvList.append(EMVTag._9F27_CRYPTOGRAM_INFORMATION_DATA, "80");

		tlvList.append(EMVTag._9F35_TERMINAL_TYPE,terminalType);
		tlvList.append(EMVTag._9F34_CVM_RESULTS,DEFAULT_CARD_VERIFICATION_RESULT);
		tlvList.append(EMVTag._9F33_TERMINAL_CAPABILITIES,terminalCapabilities);


		msg.set(FieldId.F055_ICC_DATA,tlvList.pack());

	}

	public static byte[] createMchipCryptogramData(TLVList tlvList) {
		String tranAmountAuthorised = tlvList.getString(EMVTag._9F02_AMOUNT_AUTHORIZED_NUMERIC);
		String tranAmountOther = tlvList.getString(EMVTag._9F03_AMOUNT_OTHER_NUMERIC);
		String terminalCountryCode = tlvList.getString(EMVTag._9F1A_TERMINAL_COUNTRY_CODE);
		String tranCurrencyCode = tlvList.getString(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE);
		String tvr = tlvList.getString(EMVTag._95_TERMINAL_VERIFICATION_RESULTS);
		String tranDateLocal = tlvList.getString(EMVTag._9A_TRANSACTION_DATE);
		String tranType=tlvList.getString(EMVTag._9C_TRANSACTION_TYPE);
		String 	unpredictableNumber = tlvList.getString(EMVTag._9F37_UNPREDICTABLE_NUMBER);
		String 	aip = tlvList.getString(EMVTag._82_APPLICATION_INTERCHANGE_PROFILE);
		String atc = tlvList.getString(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER);
		String 	iad = tlvList.getString(EMVTag._9F10_ISSUER_APPLICATION_DATA);

		StringBuilder sb = new StringBuilder();
		sb.append(tranAmountAuthorised);
		sb.append(tranAmountOther);
		sb.append(terminalCountryCode);
		sb.append(tvr);
		sb.append(tranCurrencyCode);
		sb.append(tranDateLocal);
		sb.append(tranType);
		sb.append(unpredictableNumber);
		sb.append(aip);
		sb.append(atc);
		sb.append(iad.substring(4,16));
		/*
		String data = sb.toString();

		int length = data.length();
		if(length%16!=0) {
			int multiplier = (length/16) + 1;
			int padLength = (multiplier*16)-length;
			for(int i=0;i<padLength;i++) {
				sb.append(HEX_NEXT_PAD);
			}
		}*/
		return ISOUtil.hex2byte(sb.toString());
	} 

	public static final byte[] createVisaChipCryptogramData(TLVList tlvList) {
		//	9F02 + 9F03 + 9F1A + 95 + 5F2A + 9A + 9C + 9F37 + 82 + 9F36 + 9F10 (only CVR for CVN 10)

		String tranAmountAuthorised = tlvList.getString(EMVTag._9F02_AMOUNT_AUTHORIZED_NUMERIC);
		String tranAmountOther = tlvList.getString(EMVTag._9F03_AMOUNT_OTHER_NUMERIC);
		String terminalCountryCode = tlvList.getString(EMVTag._9F1A_TERMINAL_COUNTRY_CODE);
		String tvr = tlvList.getString(EMVTag._95_TERMINAL_VERIFICATION_RESULTS);
		String tranCurrencyCode = tlvList.getString(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE);
		String tranDateLocal = tlvList.getString(EMVTag._9A_TRANSACTION_DATE);
		String tranType=tlvList.getString(EMVTag._9C_TRANSACTION_TYPE);
		String unpredictableNumber = tlvList.getString(EMVTag._9F37_UNPREDICTABLE_NUMBER);
		String aip = tlvList.getString(EMVTag._82_APPLICATION_INTERCHANGE_PROFILE);
		String atc = tlvList.getString(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER);
		String iad = tlvList.getString(EMVTag._9F10_ISSUER_APPLICATION_DATA);

		StringBuilder sb = new StringBuilder();
		sb.append(tranAmountAuthorised);
		sb.append(tranAmountOther);
		sb.append(terminalCountryCode);
		sb.append(tvr);
		sb.append(tranCurrencyCode);
		sb.append(tranDateLocal);
		sb.append(tranType);
		sb.append(unpredictableNumber);
		sb.append(aip);
		sb.append(atc);

		if(iad!=null && "0A".equals(iad.substring(3,5))) {
			//CVN 0A(10) only get CVR
			sb.append(iad.substring(5));
		}else{
			sb.append(iad);
		}
		
		return ISOUtil.hex2byte(sb.toString());
	}

	public static final byte[] createNSICCSCryptogramData(TLVList tlvList) {
		String tranAmountAuthorised = tlvList.getString(EMVTag._9F02_AMOUNT_AUTHORIZED_NUMERIC);
		String tranAmountOther = tlvList.getString(EMVTag._9F03_AMOUNT_OTHER_NUMERIC);
		String terminalCountryCode = tlvList.getString(EMVTag._9F1A_TERMINAL_COUNTRY_CODE);
		String tranCurrencyCode = tlvList.getString(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE);
		String tvr = tlvList.getString(EMVTag._95_TERMINAL_VERIFICATION_RESULTS);
		String tranDateLocal = tlvList.getString(EMVTag._9A_TRANSACTION_DATE);
		String tranType=tlvList.getString(EMVTag._9C_TRANSACTION_TYPE);
		String 	unpredictableNumber = tlvList.getString(EMVTag._9F37_UNPREDICTABLE_NUMBER);
		String 	aip = tlvList.getString(EMVTag._82_APPLICATION_INTERCHANGE_PROFILE);
		String atc = tlvList.getString(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER);
		String 	iad = tlvList.getString(EMVTag._9F10_ISSUER_APPLICATION_DATA);

		StringBuilder sb = new StringBuilder();
		sb.append(tranAmountAuthorised);
		sb.append(tranAmountOther);
		sb.append(terminalCountryCode);
		sb.append(tvr);
		sb.append(tranCurrencyCode);
		sb.append(tranDateLocal);
		sb.append(tranType);
		sb.append(unpredictableNumber);
		sb.append(aip);
		sb.append(atc);
		sb.append(iad);
		sb.append(HEX_PAD);

		String data = sb.toString();
		int length = data.length();
		if(length%16!=0) {
			int multiplier = (length/16) + 1;
			int padLength = (multiplier*16)-length;
			for(int i=0;i<padLength;i++) {
				sb.append(HEX_NEXT_PAD);
			}
		}
		return ISOUtil.hex2byte(sb.toString());
	} 
}

