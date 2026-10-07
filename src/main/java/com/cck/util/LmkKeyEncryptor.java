package com.cck.util;

import java.security.Key;

import javax.crypto.spec.SecretKeySpec;

import org.jpos.iso.ISOUtil;
import org.jpos.security.SMAdapter;
import org.jpos.security.SMException;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.GeckoSecurityModule;

/**
 * 2024/03/25,
 * Use this class to encrypt to MKAC (variant 1 )under LMK
 */
public class LmkKeyEncryptor {
    private GeckoSecurityModule securityModule = null;

    public LmkKeyEncryptor(String lmk) throws SMException {
        this.securityModule = new GeckoSecurityModule(lmk);
    }

    public SecureDESKey encryptToLmk(String plain, String majorKeyType, String variant) throws SMException {
        short keyLength = KeyUtil.calculateKeyLength(plain);
        String keyType = KeyUtil.constructKeyType(majorKeyType, variant, keyLength);

        Key key = null;
        
        byte[] clearKeyBytes = ISOUtil.hex2byte(plain);
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


        return securityModule.encryptToLMK(keyLength, keyType, key);
    }

    public static void main(String[] args) throws Exception {

        LmkKeyEncryptor encryptor = new LmkKeyEncryptor("resources/lmk");
        SecureDESKey secureDESKey = encryptor.encryptToLmk("2315208C9110AD402315208C9110AD40", SMAdapter.TYPE_MK_AC,
                "1");

        System.out.println(ISOUtil.hexString(secureDESKey.getKeyBytes()));
        System.out.println(ISOUtil.hexString(secureDESKey.getKeyCheckValue()));
    }
}
