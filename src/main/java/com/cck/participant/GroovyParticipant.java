// ============================================================================
// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.cck.participant;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jdom2.Element;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.SimpleConfiguration;
import org.jpos.core.XmlConfigurable;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.Context;
import org.jpos.util.Log;

import com.cck.bean.ScriptManager;
import com.cck.util.ErrorCode;
import com.cck.util.ISOContextUtil;

import groovy.lang.GroovyObject;

/**
 * 
 * @author coovy
 * 
 * 2019-01-15, handle process based on groovy script
 * 
 <participant class="com.gecko.participant.transaction.GroovyParticipant" logger="Q2">
    <property name="script-manager value=""/>
    <script id="">
       <property name="" value=""/>
    </script>
 </participant>
 */
public class GroovyParticipant extends BaseResponseParticipant implements XmlConfigurable,AbortParticipant{

	public static final String E_SCRIPT = "script";
	public static final String A_ID = "id";
	public static final String E_PROPERTY = "property";
	public static final String A_NAME = "name";
	public static final String A_VALUE = "value";


	public static final String P_SCRIPT_MANAGER = "script-manager";
	public static final String P_ABORTABLE = "abortable";

	private Configuration scriptCfg;

	private String scriptManager;
	private String scriptId;
	private boolean abortable;

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		this.scriptManager = cfg.get(P_SCRIPT_MANAGER);
		this.abortable =  cfg.getBoolean(P_ABORTABLE,false);
	}

	@Override
	public int prepareForAbort(long id, Serializable context) {
		if(this.abortable) {
			prepare (id, context);
		}
		return ABORTED;
	}

	@Override
	public void setConfiguration(Element e) throws ConfigurationException {
		Element element = e.getChild(E_SCRIPT);
		if(element==null) {
			throw new ConfigurationException("element 'script' is not defined");
		}
		this.scriptId = element.getAttributeValue(A_ID);

		if(this.scriptId==null || this.scriptId.isEmpty()) {
			throw new ConfigurationException("attribute 'id' is not defined in element 'script'");
		}
		constructScriptCfg(element);

	}

	protected Configuration getScriptCfg() {
		return this.scriptCfg;
	}

	private void constructScriptCfg(Element e) {
		List<Element> properties = e.getChildren(E_PROPERTY);
		this.scriptCfg = new SimpleConfiguration();
		for(Element p : properties) {
			String name = p.getAttributeValue(A_NAME);
			if(name!=null && !name.isEmpty()) {
				String value = p.getAttributeValue(A_VALUE);
				if(value!=null && !value.isEmpty()) {
					this.scriptCfg.put(name, value);
				}
			}
		}
	}

	private GroovyObject getGroovyObject(Log log) {
		ScriptManager scriptMgrInstance = ScriptManager.getScriptManager(this.scriptManager);
		if(scriptMgrInstance!=null) {
			log.debug("Script mgr " + this.scriptManager + " exists");
			return scriptMgrInstance.getGroovyObject(this.scriptId);
		}else {
			log.debug("Script mgr" + this.scriptManager + " does not exists");
		}
		return null;
	}

	private void mapOut(Context ctx,Object obj) {
		if(obj==null)return;

		Map<String, Object> map = (Map<String,Object>)obj;
		Set<String> set = map.keySet();
		Iterator<String> iter = set.iterator();
		while(iter.hasNext()) {
			String key = iter.next();
			Object val = map.get(key);
			if(val!=null) {
				ISOContextUtil.putSession(ctx, key, val);
			}
		}
	}

	@Override
	protected int prepareImpl(long id, Context ctx,  ISOMsg msg, ISOMsg rspMsg) {
		Log log = new Log(getLogger(),getRealm());

		GroovyObject groovyObject = getGroovyObject(log);
		if(groovyObject==null) {
			log.info(log,"Cannot process incoming message " + id + " , script " + this.scriptId + " is not defined");
			rspMsg.set(39,ErrorCode.SYSTEM_ERROR);
			return PREPARED;
		}
		Map<String,Object> out = new HashMap();
		try {
			groovyObject.invokeMethod("exec", new Object[] {log,out,this.scriptCfg, ctx,msg,rspMsg});
			mapOut(ctx,out);
		}catch(Exception e) {
			log.error(e, "Script error " + id);
			return ABORTED;
		}
		Object objResult = out.get("result");
		boolean result = true;
		if(objResult instanceof Boolean) {
			result = (Boolean)objResult;
		}
		if(result) {
			return PREPARED;
		}
		return ABORTED;
	}

}
