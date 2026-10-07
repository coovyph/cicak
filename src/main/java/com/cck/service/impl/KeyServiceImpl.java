package com.cck.service.impl;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import org.jpos.iso.ISOUtil;
import org.jpos.security.SMAdapter;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.springframework.stereotype.Service;

import com.cck.component.KeyStoreManager;
import com.cck.dto.CckHeaders;
import com.cck.dto.CckKey;
import com.cck.dto.CckKeyRequest;
import com.cck.dto.CckKeyResponse;
import com.cck.service.KeyService;
import com.cck.util.KeyUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeyServiceImpl implements KeyService {

	private final KeyStoreManager keyManager;
	private final GeckoSecurityModule gsm;

	public CckKeyResponse reloadKey() {
		keyManager.reloadKey();
		return new CckKeyResponse(true, "success");
	}




	public boolean getKey(String name,CckKeyResponse cckKeyResponse) {

		SecureDESKey secureDesKey = keyManager.getKey(name);

		cckKeyResponse.setSuccess(false);
		if(secureDesKey==null) {
			cckKeyResponse.setMessage("Key is not exists");
			return false;	
		}


		Key key = null;
		try{
			key = gsm.decryptFromLMK(secureDesKey);
		}catch(SMException se) {
			log.error("Cannot decrypt lmk key {}",name,se);
			cckKeyResponse.setMessage("Cannot decrypt lmk key " + name);
			return false;
		}

		CckKey cckKey = new CckKey();
		cckKey.setName(name);
		cckKey.setKey(ISOUtil.hexString(key.getEncoded()));
		cckKey.setKcv(ISOUtil.hexString(secureDesKey.getKeyCheckValue()));
		cckKey.setKeyType(secureDesKey.getKeyType());

		cckKeyResponse.setSuccess(true);
		cckKeyResponse.setMessage("success");
		cckKeyResponse.setKey(cckKey);

		return true;

	}



	public boolean setKey(CckKey cckKey,CckKeyResponse cckKeyResponse) {


		short keyLength = KeyUtil.calculateKeyLength(cckKey.getKey());
		String keyType = KeyUtil.constructKeyType(cckKey.getKeyType(), "1", keyLength);

		Key key = null;

		byte[] clearKeyBytes = ISOUtil.hex2byte(cckKey.getKey());
		String algorithm = "DES";
		if (keyLength > SMAdapter.LENGTH_DES){
			algorithm += "ede";

			if(keyLength< SMAdapter.LENGTH_DES3_3KEY){
				byte[] formatedBytes = new byte[24];
				System.arraycopy(clearKeyBytes,0,formatedBytes,0,clearKeyBytes.length);
				System.arraycopy(clearKeyBytes, 0, formatedBytes, clearKeyBytes.length,8);

				key = new SecretKeySpec(formatedBytes, algorithm);
			}
		}

		if(key==null){
			key = new SecretKeySpec(clearKeyBytes, algorithm);
		}


		SecureDESKey sk = null;
		try {
			sk = gsm.encryptToLMK(keyLength, keyType, key);
		}catch(SMException sme) {
			log.info("Cannot encrypt key to lmk ");
			cckKeyResponse.setSuccess(false);
			cckKeyResponse.setMessage("Cannot encrypt key to LMK");
			return false;
		}

		keyManager.setKey(cckKey.getName(), sk);
		keyManager.persist();

		cckKeyResponse.setSuccess(true);
		cckKeyResponse.setMessage("success");

		return true;
	}


}
