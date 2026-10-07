// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.cck.listener;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOSource;
import org.jpos.space.SpaceFactory;
import org.jpos.transaction.Context;
import org.jpos.util.Log;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

import com.cck.util.ISOContextUtil;

/**
 * 
 * @author coovy
 * 2020-02-22 handling incoming message from mux, check for late response
 */
public class IcomingIso8583MuxListener implements ISORequestListener,LogSource,Configurable{

	public static final  String P_QUEUE = "queue";
	public static final  String P_TIMEOUT = "timeout";

	public static final  long DEFAULT_TIMEOUT = 5000L;

	private Logger logger;
	private String realm;
	private String queue;
	private long timeout;
	
	private String interchange;

	@Override
	public boolean process(ISOSource source, ISOMsg m) {
		Context ctx = constructIncomingMsgContext(source, m);

		LogEvent evt = new LogEvent(this,Log.ERROR);
		evt.addMessage("send out message to " + this.queue + " with timeout " + this.timeout);
		evt.addMessage(m);

		Logger.log(evt);
		SpaceFactory.getSpace().push(this.queue, ctx, this.timeout);

		return true;
	}


	protected Context constructIncomingMsgContext(ISOSource source, ISOMsg m) {
		Context ctx = new Context();


		ISOContextUtil.setInIsoMsg(ctx, m);

		try {
			if(m.isResponse())
				ISOContextUtil.markAsLateResponse(ctx);
		}catch(Exception e) {
			LogEvent evt = new LogEvent(this,Log.ERROR);
			evt.addMessage("cannot extract iso msg");
			evt.addMessage(evt);

			Logger.log(evt);
		}
		
		ISOContextUtil.setEndpoint(ctx, this.interchange);

		return ctx;		
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


	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		this.queue = cfg.get("queue");
		this.interchange = cfg.get("interchange");
		this.timeout = cfg.getLong("timeout",DEFAULT_TIMEOUT);
	}
}
