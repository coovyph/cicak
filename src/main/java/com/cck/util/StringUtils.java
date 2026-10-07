// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.cck.util;

public class StringUtils {
  public static final int CHAR_A = 'A';
  public static final int CHAR_Z = 'Z';
  public static final int CHAR_a = 'a';
  public static final int CHAR_z = 'z';
  
  public static final int CHAR_0 = '0';
  public static final int CHAR_9 = '9';
  
	public static boolean isEmpty(String data) {
	  if(data==null)return true;
	  return data.isEmpty();
  }
  
  public static boolean isAlpha(int c) {
	  return (c>=CHAR_A && c<=CHAR_Z) || (c>=CHAR_a && c<=CHAR_z);
  }
  
  public static boolean isNumeric(int c) {
	  return (c>=CHAR_0 && c<=CHAR_9);
  } 
}
