// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.cck.util;

import java.util.HexFormat;

public class StringUtils {
	public static final int CHAR_A = 'A';
	public static final int CHAR_Z = 'Z';
	public static final int CHAR_a = 'a';
	public static final int CHAR_z = 'z';

	public static final int CHAR_0 = '0';
	public static final int CHAR_9 = '9';

	public static final boolean isEmpty(String data) {
		if(data==null)return true;
		return data.isEmpty();
	}

	public static final boolean isAlpha(int c) {
		return (c>=CHAR_A && c<=CHAR_Z) || (c>=CHAR_a && c<=CHAR_z);
	}

	public static final boolean isNumeric(int c) {
		return (c>=CHAR_0 && c<=CHAR_9);
	} 

	public static final byte[] parseHex(String value) {
		return HexFormat.of().parseHex(value);
	}

	public static final String toHex(byte[] value) {
		return HexFormat.of().formatHex(value).toUpperCase();
	}

	public static final String padleft(String s, int len, char c)
			throws StringUtilsException 
	{
		s = s.trim();
		if (s.length() > len)
			throw new StringUtilsException("invalid len " +s.length() + "/" +len);
		StringBuilder d = new StringBuilder (len);
		int fill = len - s.length();
		while (fill-- > 0)
			d.append (c);
		d.append(s);
		return d.toString();
	}

	public static final byte[] xor (byte[] op1, byte[] op2) {
		byte[] result;
		// Use the smallest array
		if (op2.length > op1.length) {
			result = new byte[op1.length];
		}
		else {
			result = new byte[op2.length];
		}
		for (int i = 0; i < result.length; i++) {
			result[i] = (byte)(op1[i] ^ op2[i]);
		}
		return  result;
	}

	public static final byte[] concatBytes (byte[] array1, byte[] array2) {
		byte[] concatArray = new byte[array1.length + array2.length];
		System.arraycopy(array1, 0, concatArray, 0, array1.length);
		System.arraycopy(array2, 0, concatArray, array1.length, array2.length);
		return  concatArray;
	}
}
