package com.cck;

import org.jpos.iso.ISOUtil;
import org.jpos.security.EncryptedPIN;
import org.jpos.security.SMAdapter;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.GeckoSecurityModule;

public class TestGeneratePinBlock {
	public TestGeneratePinBlock() throws Exception{
		GeckoSecurityModule jce = new GeckoSecurityModule("resources/lmk");
		// Get variables
		String pan = "4889501001619181";
		String pin = "123456";
		String zpk = "C8188FF5ECA4E8867550AFFA9B5AAD40";

		// Create key object
		SecureDESKey zpkKey = new SecureDESKey();
		zpkKey.setKeyName("ZPK");
		zpkKey.setKeyType("ZPK:0U");
		zpkKey.setVariant((byte)0);
		zpkKey.setKeyBytes(ISOUtil.hex2byte(zpk));
		zpkKey.setKeyLength(SMAdapter.LENGTH_DES3_2KEY);

		//SecureDESKey zpkKey = new SecureDESKey(SecureDESKey.DES, "ZPK", ISOUtil.hex2byte(zpk))

	    EncryptedPIN encryptedPin = jce.encryptPIN(pin,pan);
		encryptedPin = jce.exportPIN(encryptedPin, zpkKey,(byte)0 );

		// Encrypt PIN block under ZPK
		byte[] encryptedPinBlock = encryptedPin.getPINBlock();

		System.out.println(ISOUtil.hexString(encryptedPinBlock));

	}
	
	public static void main(String[] args)throws Exception {
		new TestGeneratePinBlock();
	}
}
