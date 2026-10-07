// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.cck.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.jpos.iso.ISOUtil;

public class GeckoSequencer {
   private static AtomicInteger stan = new AtomicInteger(0);
   private static AtomicLong rrn = new AtomicLong(0);

   public static String constructStan() {
	  
	   int stanNumber = stan.incrementAndGet();
	   if(stanNumber>999999) {
		   stan = new AtomicInteger(0);
		   stanNumber = stan.incrementAndGet();
	   }
	   return ISOUtil.zeropad(stanNumber, 6);
   }
   
   public static String constructRRN() {
		  
	   Long rrnNumber = rrn.incrementAndGet();
	   if(rrnNumber>999999999999L) {
		   rrn = new AtomicLong(0);
		   rrnNumber = rrn.incrementAndGet();
	   }
	   return ISOUtil.zeropad(rrnNumber, 12);
   }
   
   

}
