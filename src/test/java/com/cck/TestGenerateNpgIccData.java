package com.cck;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import org.jpos.iso.ISOUtil;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;
import org.jpos.security.SMAdapter;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.jpos.security.jceadapter.JCESecurityModule;

import com.cck.util.EMVTag;

public class TestGenerateNpgIccData {
	public TestGenerateNpgIccData() throws Exception{
		// TODO Auto-generated constructor stub
		GeckoSecurityModule jce = new GeckoSecurityModule("resources/lmk");

		Key key = new SecretKeySpec(ISOUtil.hex2byte("57D01643D0010BD6325252D50BD6CD2357D01643D0010BD6"), "DESede");
		SecureDESKey desKey =	jce.encryptToLMK(SMAdapter.LENGTH_DES3_2KEY, SMAdapter.TYPE_ZPK + ";0U", key);

		System.out.println(ISOUtil.hexString(desKey.getKeyBytes()));
		System.out.println(ISOUtil.hexString(desKey.getKeyCheckValue()));

		/*
		//prepare cryptogram data
		String tranAmountAuthorised = tlvList.getString(NSICCSTag._9F02_AMOUNT_AUTHORIZED_NUMERIC);
		String tranAmountOther = tlvList.getString(NSICCSTag._9F03_AMOUNT_OTHER_NUMERIC);
		String terminalCountryCode = tlvList.getString(NSICCSTag._9F1A_TERMINAL_COUNTRY_CODE);
		String tranCurrencyCode = tlvList.getString(NSICCSTag._5F2A_TRANSACTION_CURRENCY_CODE);
		String tvr = tlvList.getString(NSICCSTag._95_TERMINAL_VERIFICATION_RESULTS);
		String tranDateLocal = tlvList.getString(NSICCSTag._9A_TRANSACTION_DATE);
		String tranType=tlvList.getString(NSICCSTag._9C_TRANSACTION_TYPE);
		String unpredictableNumber = tlvList.getString(NSICCSTag._9F37_UNPREDICTABLE_NUMBER);
		String aip = tlvList.getString(NSICCSTag._82_APPLICATION_INTERCHANGE_PROFILE);
		String atc = tlvList.getString(NSICCSTag._9F36_APPLICATION_TRANSACTION_COUNTER);
		String iad = tlvList.getString(NSICCSTag._9F10_ISSUER_APPLICATION_DATA);

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
		sb.append("80");

		String data = sb.toString();
		int length = data.length();
		if(length%16!=0) {
			int multiplier = (length/16) + 1;
			int padLength = (multiplier*16)-length;
			for(int i=0;i<padLength;i++) {
				sb.append("80");
			}
		}
		byte[] cryptoBytes = ISOUtil.hex2byte(sb.toString());

		SecureDESKey mkac;

		String pan;
		String panSeqNum;


		jce.generateARQC(MKDMethod.OPTION_A, SKDMethod.EMV_CSKD, mkac, pan, panSeqNum, atc, null, cryptoBytes);
		 */
	}

	public static void main(String[] args) throws Exception{
		new TestGenerateNpgIccData();
	}
}
