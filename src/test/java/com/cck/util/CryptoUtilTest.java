package com.cck.util;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HexFormat;

import org.junit.jupiter.api.Test;

import com.cck.tlv.EmvFields;

public class CryptoUtilTest {

	@Test
	public void testNsiCcs1()throws CryptoUtilException {


		String pan = "4889501076030373";
		String panSeqNo = "00";
		byte[] imkac = StringUtils.parseHex("404142434445464748494A4B4C4D4E4F4041424344454647");
		EmvFields emvFields = new EmvFields();
		emvFields.setField(EmvFields._9F02_AMOUNT_AUTHORIZED_NUMERIC, "000005000000");
		emvFields.setField(EmvFields._9F03_AMOUNT_OTHER_NUMERIC,"000000000000");
		emvFields.setField(EmvFields._9F1A_TERMINAL_COUNTRY_CODE,"0360");
		emvFields.setField(EmvFields._95_TERMINAL_VERIFICATION_RESULTS,"0000000000");
		emvFields.setField(EmvFields._5F2A_TRANSACTION_CURRENCY_CODE,"0360");
		emvFields.setField(EmvFields._9A_TRANSACTION_DATE, "261007");
		emvFields.setField(EmvFields._9C_TRANSACTION_TYPE,"01");
		emvFields.setField(EmvFields._9F37_UNPREDICTABLE_NUMBER,"889A0812");
		emvFields.setField(EmvFields._82_APPLICATION_INTERCHANGE_PROFILE,"5400");
		emvFields.setField(EmvFields._9F36_APPLICATION_TRANSACTION_COUNTER,"012C");
		emvFields.setField(EmvFields._9F10_ISSUER_APPLICATION_DATA,"0101A000800000EDB26B8D0000000000000000000000000000000000");
		byte[] rslt = CryptoUtil.constructNSICCSArqc(pan,panSeqNo,imkac,emvFields);


		assertEquals("E1D3FFDCBB06F2C8", StringUtils.toHex(rslt));
	}

	@Test
	public void testNsiCcs2()throws CryptoUtilException {
		String pan = "4889501076030373";
		String panSeqNo = "00";
		byte[] imkac = StringUtils.parseHex("404142434445464748494A4B4C4D4E4F4041424344454647");

		EmvFields emvFields = new EmvFields();
		emvFields.setField(EmvFields._9F02_AMOUNT_AUTHORIZED_NUMERIC, "000000000000");
		emvFields.setField(EmvFields._9F03_AMOUNT_OTHER_NUMERIC,"000000000000");
		emvFields.setField(EmvFields._9F1A_TERMINAL_COUNTRY_CODE,"0360");
		emvFields.setField(EmvFields._95_TERMINAL_VERIFICATION_RESULTS,"0000000000");
		emvFields.setField(EmvFields._5F2A_TRANSACTION_CURRENCY_CODE,"0360");
		emvFields.setField(EmvFields._9A_TRANSACTION_DATE, "261007");
		emvFields.setField(EmvFields._9C_TRANSACTION_TYPE,"30");
		emvFields.setField(EmvFields._9F37_UNPREDICTABLE_NUMBER,"6901A5CB");
		emvFields.setField(EmvFields._82_APPLICATION_INTERCHANGE_PROFILE,"5400");
		emvFields.setField(EmvFields._9F36_APPLICATION_TRANSACTION_COUNTER,"012E");
		emvFields.setField(EmvFields._9F10_ISSUER_APPLICATION_DATA,"0101A000800000EDB26B8D0000000000000000000000000000000000");
		byte[] rslt = CryptoUtil.constructNSICCSArqc(pan,panSeqNo,imkac,emvFields);


		assertEquals("1732F62C6EA83033", StringUtils.toHex(rslt));
	}

	@Test
	public void testVchip()throws CryptoUtilException{
		String key = "EF19D6164526FE2A6E80CDDAAE62CD62";


		EmvFields emvFields = new EmvFields();
		emvFields.setField(EmvFields._9F02_AMOUNT_AUTHORIZED_NUMERIC, "000000012300");
		emvFields.setField(EmvFields._9F03_AMOUNT_OTHER_NUMERIC,"000000000000");
		emvFields.setField(EmvFields._9F1A_TERMINAL_COUNTRY_CODE,"0840");
		emvFields.setField(EmvFields._95_TERMINAL_VERIFICATION_RESULTS,"0000040000");
		emvFields.setField(EmvFields._5F2A_TRANSACTION_CURRENCY_CODE,"0840");
		emvFields.setField(EmvFields._9A_TRANSACTION_DATE, "010101");
		emvFields.setField(EmvFields._9C_TRANSACTION_TYPE,"01");
		emvFields.setField(EmvFields._9F37_UNPREDICTABLE_NUMBER,"9BADBCAB");
		emvFields.setField(EmvFields._82_APPLICATION_INTERCHANGE_PROFILE,"0000");
		emvFields.setField(EmvFields._9F36_APPLICATION_TRANSACTION_COUNTER,"00FF");
		emvFields.setField(EmvFields._9F10_ISSUER_APPLICATION_DATA,"06010A03A00000");


		byte [] arqc =	CryptoUtil.constructVchipArqc("4240965310160219", "01", HexFormat.of().parseHex(key), emvFields);

		//"1878A36D6CDF0149";
		assertEquals("1878A36D6CDF0149", StringUtils.toHex(arqc));
	}

	@Test
	public void testMchip()throws CryptoUtilException{
		String pan = "5393710500968746";
		String panSeqNo = "01";

		EmvFields emvFields = new EmvFields();
		emvFields.setField(EmvFields._9F02_AMOUNT_AUTHORIZED_NUMERIC, "000000000200");
		emvFields.setField(EmvFields._9F03_AMOUNT_OTHER_NUMERIC,"000000000000");
		emvFields.setField(EmvFields._9F1A_TERMINAL_COUNTRY_CODE,"0840");
		emvFields.setField(EmvFields._95_TERMINAL_VERIFICATION_RESULTS,"0000000000");
		emvFields.setField(EmvFields._5F2A_TRANSACTION_CURRENCY_CODE,"0840");
		emvFields.setField(EmvFields._9A_TRANSACTION_DATE, "251119");
		emvFields.setField(EmvFields._9C_TRANSACTION_TYPE,"00");
		emvFields.setField(EmvFields._9F37_UNPREDICTABLE_NUMBER,"25054363");
		emvFields.setField(EmvFields._82_APPLICATION_INTERCHANGE_PROFILE,"5C00");
		emvFields.setField(EmvFields._9F36_APPLICATION_TRANSACTION_COUNTER,"05AE");
		emvFields.setField(EmvFields._9F10_ISSUER_APPLICATION_DATA,"0110A04301A400000000FFFFFFFFFFFFFFFF");

		
				
		byte[] imkAc = StringUtils.parseHex("404043434545464649494A4A4C4C4F4F");
		
		byte[] arqc = CryptoUtil.constructMchipArqc(pan, panSeqNo, imkAc, emvFields);

		assertEquals("4DE6FE923054ACA9", StringUtils.toHex(arqc));

	}
}
