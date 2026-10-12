package com.cck.component.field;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.jpos.iso.ISOMsg;
import org.junit.jupiter.api.Test;

import com.cck.component.BinManager;
import com.cck.component.KeyStoreManager;
import com.cck.tlv.EmvFields;

public class VisaChipFieldGeneratorTest {
	@Test
	public void test()throws Exception {

		KeyStoreManager ksm = new KeyStoreManager("resources/key.prop");
		BinManager bm = new BinManager("resources/bin.prop");
		Random random = new Random();

		ksm.reloadKey();
		bm.reload();

		VISAChipFieldGenerator nif = new VISAChipFieldGenerator(random,ksm, bm);


		ISOMsg msg = new ISOMsg();

		msg.set(2,"4240965310160219");
		msg.set(3,"010000");
		msg.set(4, "000000012300");
		msg.set(23,"001");
		msg.set(49,"0840");

		String interchangeName = "BCAD";
		Map<String,Object> msgBody = new HashMap<String, Object>();
		FieldDefinition fd = new FieldDefinition();
		fd.parse("vi_55");



		Map<String,Object> chipBody = new HashMap<String, Object>();
		chipBody.put("mkac", "TEST-VISA");
		chipBody.put("format", "B");
		chipBody.put("v95", "0000040000");
		chipBody.put("v5F2A", "0840");
		chipBody.put("v9A", "010101");
		chipBody.put("v9C", "01");
		chipBody.put("v9F37", "9BADBCAB");
		chipBody.put("v82","0000");
		chipBody.put("v9F36", "00FF");
		chipBody.put("v9F10","06010A03A00000");
		chipBody.put("v9F1A", "840");
		chipBody.put("ommit", "5F34 5A");
		msgBody.put("vi_55", chipBody);



		Object rslt = nif.constructMsgValue(msg, interchangeName, msgBody, fd);

		EmvFields fields = new EmvFields();
		fields.unpack((byte[])rslt);


		assertEquals("06010A03A00000", fields.getString(EmvFields._9F10_ISSUER_APPLICATION_DATA));
		assertEquals("0000", fields.getString(EmvFields._82_APPLICATION_INTERCHANGE_PROFILE));
		assertEquals("000000000000", fields.getString(EmvFields._9F03_AMOUNT_OTHER_NUMERIC));		

		assertEquals("0840", fields.getString(EmvFields._5F2A_TRANSACTION_CURRENCY_CODE));	
		assertEquals("0000040000", fields.getString(EmvFields._95_TERMINAL_VERIFICATION_RESULTS));
		assertEquals("00FF", fields.getString(EmvFields._9F36_APPLICATION_TRANSACTION_COUNTER));
		assertEquals("9BADBCAB", fields.getString(EmvFields._9F37_UNPREDICTABLE_NUMBER));

		
		assertNull(fields.getString(EmvFields._5A_APPLICATION_PAN));
		assertNull(fields.getString(EmvFields._5F34_APPLICATION_PAN_SEQ_NR));
		
		assertEquals("010101", fields.getString(EmvFields._9A_TRANSACTION_DATE));
		assertEquals("0840", fields.getString(EmvFields._9F1A_TERMINAL_COUNTRY_CODE));
		assertEquals("01", fields.getString(EmvFields._9C_TRANSACTION_TYPE));
		assertEquals("1878A36D6CDF0149", fields.getString(EmvFields._9F26_APPLICATION_CRYPTOGRAM));

		chipBody.put("v9F26", "1234567890123456");
		
		 rslt = nif.constructMsgValue(msg, interchangeName, msgBody, fd);

		fields = new EmvFields();
		fields.unpack((byte[])rslt);
		assertEquals("123456789012346", fields.getString(EmvFields._9F26_APPLICATION_CRYPTOGRAM));

	}
}
