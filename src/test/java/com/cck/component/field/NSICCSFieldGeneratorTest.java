package com.cck.component.field;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import com.cck.component.BinManager;
import com.cck.component.KeyStoreManager;
import com.cck.tlv.EmvFields;
import com.cck.util.CryptoUtil;
import com.cck.util.StringUtils;

public class NSICCSFieldGeneratorTest {
	@Test
	public void test()throws Exception {

		KeyStoreManager ksm = new KeyStoreManager("resources/key.prop");
		BinManager bm = new BinManager("resources/bin.prop");

		ksm.reloadKey();
		bm.reload();



		NSICCSFieldGenerator nif = new NSICCSFieldGenerator(ksm, bm);


		ISOMsg msg = new ISOMsg();

		msg.set(2,"4889501076030373");
		msg.set(3,"000000");
		msg.set(4, "000005000000");
		msg.set(23,"000");
		msg.set(49,"360");

		String interchangeName = "BCAD";
		Map<String,Object> msgBody = new HashMap<String, Object>();
		FieldDefinition fd = new FieldDefinition();
		fd.parse("ni_55");



		Map<String,Object> chipBody = new HashMap<String, Object>();
		chipBody.put("mkac", "TEST");
		chipBody.put("format", "B");
		chipBody.put("v95", "0000000000");
		chipBody.put("v5F2A", "0360");
		chipBody.put("v9A", "261007");
		chipBody.put("v9C", "01");
		chipBody.put("v9F37", "889A0812");
		chipBody.put("v82","5400");
		chipBody.put("v9F36", "012C");
		chipBody.put("v9F10","0101A000800000EDB26B8D0000000000000000000000000000000000");
		msgBody.put("ni_55", chipBody);

		Object rslt = nif.constructMsgValue(msg, interchangeName, msgBody, fd);

		EmvFields fields = new EmvFields();
		fields.unpack((byte[])rslt);

	
		assertEquals("0101A000800000EDB26B8D0000000000000000000000000000000000", fields.getString(EmvFields._9F10_ISSUER_APPLICATION_DATA));
		assertEquals("5400", fields.getString(EmvFields._82_APPLICATION_INTERCHANGE_PROFILE));
		assertEquals("000000000000", fields.getString(EmvFields._9F03_AMOUNT_OTHER_NUMERIC));		
		assertEquals("00", fields.getString(EmvFields._5F34_APPLICATION_PAN_SEQ_NR));
		assertEquals("0360", fields.getString(EmvFields._5F2A_TRANSACTION_CURRENCY_CODE));	
		assertEquals("020000", fields.getString(EmvFields._9F34_CVM_RESULTS));
		assertEquals("0000000000", fields.getString(EmvFields._95_TERMINAL_VERIFICATION_RESULTS));
		assertEquals("012C", fields.getString(EmvFields._9F36_APPLICATION_TRANSACTION_COUNTER));
		assertEquals("889A0812", fields.getString(EmvFields._9F37_UNPREDICTABLE_NUMBER));
		assertEquals("4889501076030373", fields.getString(EmvFields._5A_APPLICATION_PAN));
		assertEquals("261007", fields.getString(EmvFields._9A_TRANSACTION_DATE));
		assertEquals("0360", fields.getString(EmvFields._9F1A_TERMINAL_COUNTRY_CODE));
		assertEquals("01", fields.getString(EmvFields._9C_TRANSACTION_TYPE));
		assertEquals("E1D3FFDCBB06F2C8", fields.getString(EmvFields._9F26_APPLICATION_CRYPTOGRAM));
	}
}
