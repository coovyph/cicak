package com.cck.util;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.jpos.iso.ISOUtil;
import org.jpos.tlv.TLVList;
import org.junit.jupiter.api.Test;

public class CryptoUtilTest {

	@Test
	public void testNsiCcs1()throws CryptoUtilException {


		String pan = "4889501076030373";
		String panSeqNo = "00";
		byte[] imkac = ISOUtil.hex2byte("404142434445464748494A4B4C4D4E4F4041424344454647");
		TLVList tlvList = new TLVList();
		tlvList.append(EMVTag._9F02_AMOUNT_AUTHORIZED_NUMERIC, "000005000000");
		tlvList.append(EMVTag._9F03_AMOUNT_OTHER_NUMERIC,"000000000000");
		tlvList.append(EMVTag._9F1A_TERMINAL_COUNTRY_CODE,"0360");
		tlvList.append(EMVTag._95_TERMINAL_VERIFICATION_RESULTS,"0000000000");
		tlvList.append(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE,"0360");
		tlvList.append(EMVTag._9A_TRANSACTION_DATE, "261007");
		tlvList.append(EMVTag._9C_TRANSACTION_TYPE,"01");
		tlvList.append(EMVTag._9F37_UNPREDICTABLE_NUMBER,"889A0812");
		tlvList.append(EMVTag._82_APPLICATION_INTERCHANGE_PROFILE,"5400");
		tlvList.append(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER,"012C");
		tlvList.append(EMVTag._9F10_ISSUER_APPLICATION_DATA,"0101A000800000EDB26B8D0000000000000000000000000000000000");
		byte[] rslt = CryptoUtil.constructNSICCSArqc(pan,panSeqNo,imkac,tlvList);


		assertEquals("E1D3FFDCBB06F2C8", ISOUtil.hexString(rslt));
	}

	@Test
	public void testNsiCcs2()throws CryptoUtilException {
		String pan = "4889501076030373";
		String panSeqNo = "00";
		byte[] imkac = ISOUtil.hex2byte("404142434445464748494A4B4C4D4E4F4041424344454647");
		byte[] atc = ISOUtil.hex2byte("012E");
		TLVList tlvList = new TLVList();
		tlvList.append(EMVTag._9F02_AMOUNT_AUTHORIZED_NUMERIC, "000000000000");
		tlvList.append(EMVTag._9F03_AMOUNT_OTHER_NUMERIC,"000000000000");
		tlvList.append(EMVTag._9F1A_TERMINAL_COUNTRY_CODE,"0360");
		tlvList.append(EMVTag._95_TERMINAL_VERIFICATION_RESULTS,"0000000000");
		tlvList.append(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE,"0360");
		tlvList.append(EMVTag._9A_TRANSACTION_DATE, "261007");
		tlvList.append(EMVTag._9C_TRANSACTION_TYPE,"30");
		tlvList.append(EMVTag._9F37_UNPREDICTABLE_NUMBER,"6901A5CB");
		tlvList.append(EMVTag._82_APPLICATION_INTERCHANGE_PROFILE,"5400");
		tlvList.append(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER,"012E");
		tlvList.append(EMVTag._9F10_ISSUER_APPLICATION_DATA,"0101A000800000EDB26B8D0000000000000000000000000000000000");
		byte[] rslt = CryptoUtil.constructNSICCSArqc(pan,panSeqNo,imkac,tlvList);


		assertEquals("1732F62C6EA83033", ISOUtil.hexString(rslt));
	}

	@Test
	public void testVchip()throws CryptoUtilException{
		String key = "EF19D6164526FE2A6E80CDDAAE62CD62";


		TLVList tlvList = new TLVList();
		tlvList.append(EMVTag._9F02_AMOUNT_AUTHORIZED_NUMERIC, "000000012300");
		tlvList.append(EMVTag._9F03_AMOUNT_OTHER_NUMERIC,"000000000000");
		tlvList.append(EMVTag._9F1A_TERMINAL_COUNTRY_CODE,"0840");
		tlvList.append(EMVTag._95_TERMINAL_VERIFICATION_RESULTS,"0000040000");
		tlvList.append(EMVTag._5F2A_TRANSACTION_CURRENCY_CODE,"0840");
		tlvList.append(EMVTag._9A_TRANSACTION_DATE, "010101");
		tlvList.append(EMVTag._9C_TRANSACTION_TYPE,"01");
		tlvList.append(EMVTag._9F37_UNPREDICTABLE_NUMBER,"9BADBCAB");
		tlvList.append(EMVTag._82_APPLICATION_INTERCHANGE_PROFILE,"0000");
		tlvList.append(EMVTag._9F36_APPLICATION_TRANSACTION_COUNTER,"00FF");
		tlvList.append(EMVTag._9F10_ISSUER_APPLICATION_DATA,"06010A03A00000");


		byte [] arqc =	CryptoUtil.constructVchipArqc("4240965310160219", "01", ISOUtil.hex2byte(key), tlvList);
		
		//"1878A36D6CDF0149";
		assertEquals("1878A36D6CDF0149", ISOUtil.hexString(arqc));
	}
}
