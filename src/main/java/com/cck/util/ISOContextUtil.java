// ============================================================================
// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.cck.util;

import java.util.HashMap;
import java.util.Map;

import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.transaction.Context;

/**
 * 
 * @author coovy
 * 2010-02-20, helper class to get or set context
 * 
 */
public class ISOContextUtil {
	public static final String IN_MTI = "$inmti";
	public static final String OUT_MTI = "$outmti";
	public static final String DE_PREFIX = "$de";
	public static final String IN_ISOMSG = "$msg";
	public static final String OUT_ISOMSG = "$omsg";
	public static final String SESSION = "$session";
	public static final String SOURCE = "$source";
	public static final String IS_LATE_RESPONSE = "$lastrsp";
	public static final String END_POINT = "$endpoint";
	public static final String SOCKET_NAME = "$socketName";
	public static final String REQUEST = "$request";
	
	public static final String OVERRIDE_FIELDS = "$ovrdfld";
	
	public static void putSessionMap(Context ctx,Map<String, Object> inMap) {
		Object obj = ctx.get(SESSION);
		Map<String, Object> map = null;
		if(obj==null) {
			map = new HashMap<String, Object>();
			ctx.put(SESSION,map);
		}else {
			map = (Map<String,Object>)obj;
		}
		map.putAll(inMap);
	}
	
	public static void putOverrideFields(Context ctx,Map<Integer, Object> inMap) {
		Object obj = ctx.get(OVERRIDE_FIELDS);
		Map<Integer, Object> map = null;
		if(obj==null) {
			map = new HashMap<Integer, Object>();
			ctx.put(OVERRIDE_FIELDS,map);
		}else {
			map = (Map<Integer,Object>)obj;
		}
		map.putAll(inMap);
	}
	
	
	public static Map<Integer,Object> getOverrideFields(Context ctx) {
		Object obj = ctx.get(OVERRIDE_FIELDS);
		if(obj==null)return null;
		if(obj instanceof Map<?, ?>) {
			return (Map<Integer, Object>)obj;
		}
		return null;
	}
	
	
	public static void putSession(Context ctx,String key,Object value) {
		Object obj = ctx.get(SESSION);
		Map<String, Object> map = null;
		if(obj==null) {
			map = new HashMap<String, Object>();
			ctx.put(SESSION,map);
		}else {
			map = (Map<String,Object>)obj;
		}
		map.put(key,value);
	}
	
	public static String getSessionString(Context ctx,String key,String defaultValue) {
	   Object result = getSession(ctx,key);
	   if(result==null)return defaultValue;
	   return result.toString();
	}

	public static Long getSessionLong(Context ctx,String key,Long defaultValue) {
		   Object result = getSession(ctx,key);
		   if(result instanceof Long) {
			   return (Long)result;
		   }
		   return defaultValue; 
		}
	public static boolean getSessionBoolean(Context ctx,String key,boolean defaultValue) {
		   Object result = getSession(ctx,key);
		   if(result==null)return defaultValue;
		   return (Boolean)result;
		}
	
	public static Object getSession(Context ctx,String key) {
		Object obj = ctx.get(SESSION);
		if(obj==null)return null;
		if(obj instanceof Map<?, ?>) {
			Map<String,Object> map = (Map<String, Object>)obj;
			return map.get(key);
		}
		return null;
	}
	
	public static void setInMti(Context ctx,String mti) {
		ctx.put(IN_MTI, mti);
	}
	
	public static String getInMti(Context ctx) {
		return ctx.getString(IN_MTI);
	}
	
	public static void setOutMti(Context ctx,String mti) {
		ctx.put(OUT_MTI, mti);
	}
	
	public static String getOutMti(Context ctx) {
		return ctx.getString(OUT_MTI);
	}
	
	public static void setRequest(Context ctx,boolean request) {
		ctx.put(REQUEST, request);
	}
	
	public static boolean isRequest(Context ctx) {
		return getBoolean(ctx, REQUEST);
	}
	
	public static void putDE(Context ctx,int no,Object value) {
		ctx.put(constructDE(no), value);
	}
	
	public static String getDEString(Context ctx,int no) {
		return ctx.getString(constructDE(no));
	}
	
	public static byte[] getDEBytes(Context ctx,int no) {
		Object obj = getDE(ctx,no);
		if(obj==null)return null;
		if(obj instanceof byte[]) {
			return (byte[])obj;
		}
		return null;
	}
	
	public static Object getDE(Context ctx,int no) {
		return ctx.get(constructDE(no));
	}
	
	private static String constructDE(int no) {
		return DE_PREFIX + no;
	}
	
	public static boolean isReservedKey(String key) {
		if(key==null)return false;
		return IN_ISOMSG.equals(key) || 
			   SOURCE.equals(key);
	}
	
	public static void setEndpoint(Context ctx,String endpoint) {
		ctx.put(END_POINT, endpoint);
	}
	
	public static String getEndpoint(Context ctx) {
		return getString(ctx, END_POINT);
	}
	
	
	public static void setSocketName(Context ctx,String socketName) {
		ctx.put(SOCKET_NAME, socketName);
	}
	
	public static String getSocketName(Context ctx) {
		return getString(ctx, SOCKET_NAME);
	}
	
	public static void markAsLateResponse(Context ctx) {
		ctx.put(IS_LATE_RESPONSE, true);
	}
	
	public static boolean isMarkedAsLateResponse(Context ctx) {
		return getBoolean(ctx, IS_LATE_RESPONSE);
	}
	
	public static String getString(Context ctx,String key) {
		Object obj = ctx.get(key);
		if(obj==null)return null;
		return obj.toString();
	}
	private static boolean getBoolean(Context ctx,String key) {
		Object obj = ctx.get(key);
		if(obj==null)return false;
		try {
			return (Boolean)obj;
		}catch(Exception e) {
			return false;
		}
	}
	
	public static ISOMsg getInIsomsg(Context ctx) {
		return getIsoMsg(ctx, IN_ISOMSG);
	}

	public static void setInIsoMsg(Context ctx,ISOMsg msg) {
		ctx.put(IN_ISOMSG, msg);
	}

	public static ISOMsg getOutIsoMsg(Context ctx) {
		return getIsoMsg(ctx, OUT_ISOMSG);
	}

	public static void setOutIsoMsg(Context ctx,ISOMsg msg) {
		ctx.put(OUT_ISOMSG, msg);
	}
	

	
	public static void setSource(Context ctx,ISOSource source) {
		ctx.put(SOURCE, source);
	}
	
	public static ISOSource getSource(Context ctx) {
		Object obj = ctx.get(SOURCE);
		if(obj==null)return null;
		if(obj instanceof ISOSource) {
			return (ISOSource)obj;
		}
		return null;
	}

	private static ISOMsg getIsoMsg(Context ctx,String key) {
		Object obj = ctx.get(key);
		if(obj==null)return null;
		if(obj instanceof ISOMsg) {
			return (ISOMsg)obj;
		}
		return null;
	}
}
