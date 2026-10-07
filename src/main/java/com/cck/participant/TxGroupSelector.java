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

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;
import org.jpos.transaction.GroupSelector;
import org.jpos.util.Log;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

import com.cck.util.ISOContextUtil;

public class TxGroupSelector implements GroupSelector,Configurable,LogSource{
	public static final String REQUEST_TIME = "incoming-time";

	private Configuration cfg;

	private Logger logger;
	private String realm;

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		this.cfg = cfg;
	}

	@Override
	public int prepare(long id, Serializable context) {

		return PREPARED|ABORTED|NO_JOIN;
	}

	private void log(String level,Object... messages) {
		LogEvent event = new LogEvent(this,level);

		for(Object message:messages) {
			event.addMessage(message);
		}

		Logger.log(event);
	}

	protected void info(Object... messages) {
		log(Log.INFO,messages);
	}

	protected void error(Object... messages) {
		log(Log.ERROR,messages);
	}
	
	protected void debug(Object... messages) {
		log(Log.DEBUG,messages);
	}

	@Override
	public String select(long id, Serializable context) {

		String group = "unknown";
		if(!(context instanceof Context)){
			info("Cannot process incoming message, is not instance of Context");
			return group;
		}
		Context ctx = (Context)context;

		ISOContextUtil.putSession(ctx,REQUEST_TIME, System.currentTimeMillis());
		ISOMsg inMsg = ISOContextUtil.getInIsomsg(ctx);
		
		if(inMsg==null) {
			info("No incoming iso message..");
			return group;
		}

		try {
			group = constructSelector(inMsg,ISOContextUtil.isMarkedAsLateResponse(ctx));
			if(group==null)group = "unknown";
		}catch(Exception e) {
			error("Cannot construct selector",e);
		}

		return group;
	}

	protected String constructSelector(ISOMsg inMsg,boolean lateResponse)throws ISOException {
		StringBuilder sb = new StringBuilder();
		String mti = inMsg.getMTI();

		sb.append(mti.substring(1,2));
		if(inMsg.hasField(3)) {
			String processingCode = inMsg.getString(3);
			sb.append(processingCode.substring(0,2));
		}

		if(inMsg.hasField(70)) {
			sb.append(inMsg.getString(70));
		}
		if(lateResponse)sb.append("late");
		String code = sb.toString();

		debug("Using code " + code);
		return this.cfg.get(code, code);
	}

	protected Configuration getConfig(){
		return this.cfg;
	}

	@Override
	public void setLogger(Logger logger, String realm) {
		this.logger = logger;
		this.realm = realm;
	}

	@Override
	public String getRealm() {
		return this.realm;
	}

	@Override
	public Logger getLogger() {
		return this.logger;
	}





}
