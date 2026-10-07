// ============================================================================
// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.cck.listener;

import java.util.EventObject;
import java.util.StringTokenizer;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOChannel;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ISOServerEventListener;
import org.jpos.space.Space;
import org.jpos.space.SpaceFactory;

/**
 * 
 * @author coovy
 * 
 * handle incoming connection

 */
public class ServerEventListener implements ISOServerEventListener,Configurable{
	public static final String P_READY = "ready";

	private String ready;

	@Override
	public void handleISOServerEvent(EventObject event) {
		Object source = event.getSource();
		if(source instanceof ISOServer) {
			ISOServer server = (ISOServer)source;
			Space sp = SpaceFactory.getSpace();
			if(isServerConnected(server)) {
				sp.out(this.ready, System.currentTimeMillis());
			}else {
				sp.out(this.ready, 0L);
			}
		}

	}

	protected boolean isServerConnected(ISOServer server) {
		String names = server.getISOChannelNames();
		StringTokenizer tokenz = new StringTokenizer(names);
		while(tokenz.hasMoreElements()) {
			ISOChannel c = server.getISOChannel(tokenz.nextToken());
			if(c.isConnected()) {
				return true;
			}
		}
		return false;
	}


	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		this.ready = cfg.get(P_READY);
	}

}
