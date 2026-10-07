// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.cck.util;

import org.jpos.iso.ISOUtil;
import org.jpos.security.KeyScheme;
import org.jpos.security.SMAdapter;
import org.jpos.security.SecureDESKey;

public class KeyUtil {

	private KeyUtil() {

	}
	public static final SecureDESKey constructKey(String hexKey,String mainKeyType,String variant) {
		byte[] keyBytes = ISOUtil.hex2byte(hexKey);
		SecureDESKey result = new SecureDESKey();

		short keyLength = calculateKeyLength(keyBytes);
		String keyType = constructKeyType(mainKeyType, variant, keyLength);

		result.setKeyBytes(keyBytes);
		result.setKeyLength(keyLength);
		result.setKeyType(keyType);

		return result;
	}
	public static final String constructKeyType(String mainKeyType,String variant,short keyLength) {
		KeyScheme scheme = KeyScheme.U;
		if(keyLength==SMAdapter.LENGTH_DES) {
			scheme = KeyScheme.Z;
		}else if(keyLength==SMAdapter.LENGTH_DES3_3KEY) {
			scheme = KeyScheme.T;
		}

		StringBuilder sbKeyType = new StringBuilder();
		sbKeyType.append(mainKeyType);
		sbKeyType.append(":");
		sbKeyType.append(variant);//variant
		sbKeyType.append(scheme);//scheme

		return sbKeyType.toString();
	}

	public static final boolean isSameBytes(byte[] dataBytes1,byte[] dataBytes2) {
		if(dataBytes1.length==dataBytes2.length) {
			for(int i=0;i<dataBytes1.length;i++) {
				if(dataBytes1[i]!=dataBytes2[i])return false;
			}
			return true;
		}else {
			return false;
		}
	}

	public static final short calculateKeyLength(byte[] raw) {
		if(raw==null)return 0;
		return  Integer.valueOf((raw.length/8) * SMAdapter.LENGTH_DES).shortValue();
	}

	public static final short calculateKeyLength(String raw) {
		if(raw==null)return 0;
		return  Integer.valueOf((raw.length()/16) * SMAdapter.LENGTH_DES).shortValue();

	}
}
