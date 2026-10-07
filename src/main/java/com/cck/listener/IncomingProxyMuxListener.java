package com.cck.listener;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.transaction.Context;

import com.cck.util.GeckoLogger;
import com.cck.util.ISOContextUtil;

public class IncomingProxyMuxListener extends IncomingListener {
	public static final String P_CHANNEL = "endpoint";

	public static final String P_SOCKET_NAME = "socket-name";

	public static final String P_INTERCHANGE = "interchange";

	public static final String P_IDENTIFIER = "identifier";

	private String channelName;

	private String socketName;

	private String interchangeName;

	private String identifier;

	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		super.setConfiguration(cfg);
		this.channelName = cfg.get("endpoint");
		this.socketName = cfg.get("socket-name", "");
		this.interchangeName = cfg.get("interchange", "");
		this.identifier = cfg.get("identifier", "");
	}

	protected Context constructIncomingMsgContext(ISOSource source, ISOMsg m) {
		Context ctx = super.constructIncomingMsgContext(source, m);
		ISOContextUtil.setEndpoint(ctx, this.channelName);
		ISOContextUtil.setSocketName(ctx, this.socketName);
		GeckoLogger.info(getLog(), "Using interchange %s", new Object[] { this.interchangeName });
		ISOContextUtil.putSession(ctx, "interchange", this.interchangeName);
		ISOContextUtil.putSession(ctx, "identifier", this.identifier);
		try {
			if (m.isResponse())
				ISOContextUtil.markAsLateResponse(ctx); 
		} catch (Exception e) {
			GeckoLogger.error(e, getLog(), "Canoot extract isomsg informations");
		} 
		return ctx;
	}
}
