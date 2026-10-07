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
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

import com.cck.util.ISOContextUtil;

public class IncomingListener implements ISORequestListener, LogSource, Configurable {
	public static final String P_QUEUE = "queue";

	public static final String P_TIMEOUT = "timeout";

	public static final long DEFAULT_TIMEOUT = 60000L;

	private Log log;

	private String queue;

	private long timeout;

	public boolean process(ISOSource source, ISOMsg m) {
		Context ctx = constructIncomingMsgContext(source, m);
		SpaceFactory.getSpace().push(this.queue, ctx, this.timeout);
		return true;
	}

	protected Context constructIncomingMsgContext(ISOSource source, ISOMsg m) {
		Context ctx = new Context();
		ISOContextUtil.setInIsoMsg(ctx, m);
		ISOContextUtil.setSource(ctx, m.getSource());
		return ctx;
	}

	protected Log getLog() {
		return this.log;
	}

	public void setLogger(Logger logger, String realm) {
		this.log = new Log(logger, realm);
	}

	public String getRealm() {
		return this.log.getRealm();
	}

	public Logger getLogger() {
		return this.log.getLogger();
	}

	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		this.queue = cfg.get("queue");
		this.timeout = cfg.getLong("timeout", 60000L);
	}
}