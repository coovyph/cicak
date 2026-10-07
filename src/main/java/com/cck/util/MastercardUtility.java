package com.cck.util;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;
import org.jpos.security.SMAdapter;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.jpos.tlv.TLVList;

public class MastercardUtility {

	private static final String extractCardVerificationResults(TLVList tlvList) throws ISOException{
		
		String issAppData = tlvList.getString(EMVTag._9F10_ISSUER_APPLICATION_DATA);
		if(issAppData==null || issAppData.length()<16)throw new ISOException("Invalid issuer application data");

		return issAppData.substring(4,16);
	}
	

	private static final byte[] createCryptogramData(TLVList tlvList)throws ISOException {
		String tranAmountAuthorised = tlvList.getString(EMVTag._9F02_AMOUNT_AUTHORIZED_NUMERIC);

		String tranAmountOther = tlvList.getString(EMVTag._9F03_AMOUNT_OTHER_NUMERIC);
		if(tranAmountOther==null) tranAmountOther = "000000000000";

		String terminalCountryCode = tlvList.getString(EMVTag._9F1A_TERMINAL_COUNTRY_CODE);
		String tvr = tlvList.getString(EMVTag._95_TERMINAL_VERIFICATION_RESULTS);
		String tranCurrencyCode = tlvList.getString(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE);
		String tranDateLocal = tlvList.getString(EMVTag._9A_TRANSACTION_DATE);
		String tranType=tlvList.getString(EMVTag._9C_TRANSACTION_TYPE);

		String 	unpredictableNumber = tlvList.getString(EMVTag._9F37_UNPREDICTABLE_NUMBER);
		String 	aip = tlvList.getString(EMVTag._82_APPLICATION_INTERCHANGE_PROFILE);
		String atc = tlvList.getString(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER);
		

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
		
		sb.append(extractCardVerificationResults(tlvList));
		sb.append(80);

		return ISOUtil.hex2byte(sb.toString());
	} 

	public static final void calculateCryptogram(GeckoSecurityModule adapter,
			SecureDESKey mkac,
			TLVList tlvList,
			String pan,
			String panSeqNum
			)throws ISOException {

		String atc = tlvList.getString(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER);
		byte[] crypto = createCryptogramData(tlvList);
		
		byte[] upn = tlvList.getValue(EMVTag._9F37_UNPREDICTABLE_NUMBER);
		
		
		System.out.println(ISOUtil.hexString(crypto));
		byte[] result = adapter.generateARQC(MKDMethod.OPTION_A, SKDMethod.MCHIP, mkac, pan, panSeqNum, ISOUtil.hex2byte(atc), upn, crypto);

		tlvList.append(EMVTag._9F26_APPLICATION_CRYPTOGRAM,result);
	}

	public static void main(String[] args)throws ISOException {
		GeckoSecurityModule adapter = new GeckoSecurityModule("resources/lmk");
		String imkAc = "A431AD34F5D9E0D18BE228C8B7FA515E";
		TLVList list = new TLVList();

		/*
		 * 5F2A TRAN-CURRNCY 0840
82 AIP 5800
84 DFNAME A0000000041010
95 (TVR) 0000040000
9A (tran date)221112
9C tran type 00
9F02 amount, auth 000000008500
9F10 iss app data , 010103250000DAC1
 -> CVR 03250000
9F1A term country code , 0840
9F27 crypt inf data , 80
9F33 Terminal cap, E0E8E8
9F34 CVM result, 410302
9F36 ATC 000A
9F37 Unpre num 38BAC117
		 */
		list.append(EMVTag._9F02_AMOUNT_AUTHORIZED_NUMERIC,"000000000200");
		list.append(EMVTag._9F03_AMOUNT_OTHER_NUMERIC,"000000000000");
		list.append(EMVTag._9F1A_TERMINAL_COUNTRY_CODE,"0840");
		list.append(EMVTag._95_TERMINAL_VERIFICATION_RESULTS,"0000000000");		
		list.append(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE, "0840");
		list.append(EMVTag._9A_TRANSACTION_DATE,"251119");
		list.append(EMVTag._9C_TRANSACTION_TYPE,"00");
		list.append(EMVTag._9F37_UNPREDICTABLE_NUMBER,"25054363");
		list.append(EMVTag._82_APPLICATION_INTERCHANGE_PROFILE,"5C00");
		list.append(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER,"05AE");
		list.append(EMVTag._9F10_ISSUER_APPLICATION_DATA,"0110A04301A400000000FFFFFFFFFFFFFFFF");
		
		list.append(EMVTag._84_DF_NAME,"A0000000041010");
		
		list.append(EMVTag._9F27_CRYPTOGRAM_INFORMATION_DATA,"80");
		list.append(EMVTag._9F33_TERMINAL_CAPABILITIES,"E0E8E8");
		list.append(EMVTag._9F34_CVM_RESULTS,"410302");

		String pan = "5393710500968746";
		String panSeqNum = "01";



		SecureDESKey mkac = KeyUtil.constructKey(imkAc, SMAdapter.TYPE_MK_AC,"0");

		MastercardUtility.calculateCryptogram(adapter,  mkac, list, pan, panSeqNum);
		
	list.dump(System.out, "");
	}
}
