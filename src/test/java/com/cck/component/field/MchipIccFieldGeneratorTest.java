package com.cck.component.field;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.Key;
import java.util.HashMap;
import java.util.Map;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.security.SMAdapter;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.jpos.tlv.TLVList;
import org.junit.jupiter.api.Test;

import com.cck.component.BinManager;
import com.cck.component.KeyStoreManager;
import com.cck.util.KeyUtil;
import com.cck.util.MastercardUtility;
import com.cck.util.EMVTag;

public class MchipIccFieldGeneratorTest {
	@Test
	public void testGenerateArqc()throws SMException,FieldDefinitionException,FieldGeneratorException {
		GeckoSecurityModule gsm = new GeckoSecurityModule("resources/lmk");
		KeyStoreManager ksm = new KeyStoreManager("resources/key.prop");
		BinManager bm = new BinManager("resources/bin.prop");

		ksm.reloadKey();
		bm.reload();
		
		MchipIccFieldGenerator mif = new MchipIccFieldGenerator(gsm, ksm, bm);

		ISOMsg msg = new ISOMsg();

		msg.set(2,"5393710500968746");
		msg.set(3,"000000");
		msg.set(4, "000000012600");
		msg.set(23,"001");
		msg.set(49,"840");

		String interchangeName = "BCAD";
		Map<String,Object> msgBody = new HashMap<String, Object>();
		FieldDefinition fd = new FieldDefinition();
		fd.parse("m55");
		


	
		Map<String,Object> chipBody = new HashMap<String, Object>();
		chipBody.put("mkac", "MK-AC-BCAD");
		chipBody.put("v82","5C00");
		chipBody.put("v9F36", "05AE");
		chipBody.put("v9F37", "25054363");
		chipBody.put("v9A", "251121");
		chipBody.put("v9F10","0110A04301A400000000FFFFFFFFFFFFFFFF");
		msgBody.put("m55", chipBody);


		Key mkacKey =	gsm.decryptFromLMK(ksm.getKey("MK-AC-BCAD"));
		//System.out.println(ISOUtil.hexString(mkacKey.getEncoded()));


		Object value = mif.constructMsgValue(msg, interchangeName, msgBody, fd);

		//System.out.println(value);

		TLVList tlvList = new TLVList();
		try {
			tlvList.unpack(ISOUtil.hex2byte(value.toString()));
		}catch(ISOException isoe) {

		}
		tlvList.dump(System.out, "");
		
		assertEquals("4DE6FE923054ACA9", tlvList.getString(EMVTag._9F26_APPLICATION_CRYPTOGRAM));

		//tlvList.dump(System.out, "");

	}
}
