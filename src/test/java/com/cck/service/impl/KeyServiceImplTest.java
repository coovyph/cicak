package com.cck.service.impl;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.jpos.security.SMAdapter;
import org.jpos.security.SMException;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cck.component.KeyStoreManager;
import com.cck.dto.CckHeaders;
import com.cck.dto.CckKey;
import com.cck.dto.CckKeyRequest;
import com.cck.dto.CckKeyResponse;

@ExtendWith(MockitoExtension.class)
public class KeyServiceImplTest {
	@InjectMocks
	private static KeyServiceImpl keyServiceImpl;

	@BeforeAll
	public static void init() throws SMException{
		GeckoSecurityModule gsm = new GeckoSecurityModule("resources/lmk");
		KeyStoreManager ksm = new KeyStoreManager("resources/key.prop");
		ksm.reloadKey();

		keyServiceImpl = new KeyServiceImpl(ksm, gsm);


	}

	@Test
	public void testSave() {

		CckKey cckKey = new CckKey();
		cckKey.setKey("404043434545464649494A4A4C4C4F4F");
		cckKey.setKeyType(SMAdapter.TYPE_MK_AC);
	
		cckKey.setName("MKAC-TEST");


		CckKeyResponse cckKeyResponse = new CckKeyResponse();

		boolean result = keyServiceImpl.setKey(cckKey, cckKeyResponse);

		assertTrue("Expected key to stored",result);
	}

	@Test
	public void testRetrieve() {

		new CckKey();


		CckKeyResponse cckKeyResponse = new CckKeyResponse();

		boolean result = keyServiceImpl.getKey("ZPK-TEST", cckKeyResponse);


		assertTrue("Expected key to be retrieve",result);
		CckKey cckKey = cckKeyResponse.getKey();

		assertNotNull(cckKey);

		assertEquals("1694ECA4D5232910DCBC5EB3F2A2A2921694ECA4D5232910",cckKey.getKey());

	}
}
