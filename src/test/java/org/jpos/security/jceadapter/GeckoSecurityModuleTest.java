package org.jpos.security.jceadapter;

import org.jpos.iso.ISOUtil;
import org.jpos.security.MKDMethod;
import org.jpos.security.SKDMethod;
import org.jpos.security.SMAdapter;
import org.jpos.security.SecureDESKey;
import org.junit.jupiter.api.Test;

import com.cck.util.KeyUtil;

public class GeckoSecurityModuleTest {
	
	//@Test
	public void testARQC()throws Exception {
		GeckoSecurityModule module = new GeckoSecurityModule("lmk");

		String key = "08BFD9A98CDC56EBADA755F8223DA4F9";



		SecureDESKey imkAcKey =KeyUtil.constructKey(key, SMAdapter.TYPE_MK_AC,"0");
		byte[] atc = ISOUtil.hex2byte("00FF");
		byte[] upn = ISOUtil.hex2byte("9BADBCAB");
		byte[] transData = ISOUtil.hex2byte("000000012300000000000000084000000400000840010101019BADBCAB000000FF03A00000");
		byte [] arqc =	module.calculateARQC(MKDMethod.OPTION_A,SKDMethod.VSDC,imkAcKey,"4240965310160219","01",atc,upn,transData);

		System.out.println(ISOUtil.hexString(arqc));
	}
	
	@Test
	public void testARQC2()throws Exception {
		GeckoSecurityModule module = new GeckoSecurityModule("lmk");

		String key = "08BFD9A98CDC56EBADA755F8223DA4F9";



		SecureDESKey imkAcKey =KeyUtil.constructKey(key, SMAdapter.TYPE_MK_AC,"0");
		byte[] atc = ISOUtil.hex2byte("001D");
		byte[] upn = ISOUtil.hex2byte("9BADBCAB");
		byte[] transData = ISOUtil.hex2byte("000000034400000000000000034480000000000344260727009BADBCAB1800001D06011203A0A800");
		byte [] arqc =	module.calculateARQC(MKDMethod.OPTION_A,SKDMethod.VSDC,imkAcKey,"4240965320029156","01",atc,upn,transData);

		System.out.println(ISOUtil.hexString(arqc));
	}
}
