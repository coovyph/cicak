package com.cck.participant;

import java.io.Serializable;

import org.jpos.core.Configurable;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.Log;
import org.jpos.util.LogEvent;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

import com.cck.util.ISOContextUtil;
public abstract class BaseResponseParticipant implements TransactionParticipant,LogSource,Configurable{

	private Logger logger;
	private String realm;

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

	@Override
	public int prepare(long id, Serializable context) {
		if(!(context instanceof Context)) {
			info("Cannot process incoming message, incoming message is not instance of Context");
			return PREPARED;
		}
		Context ctx = (Context)context;
		
		ISOMsg msg = ISOContextUtil.getInIsomsg(ctx);
		if(msg!=null) {

			try {
				if(!msg.isRequest()) {
					info("Cannot process invalid incoming request message");
					return ABORTED;
				}
				ISOMsg rspMsg = ISOContextUtil.getOutIsoMsg(ctx);
				if(rspMsg==null) {

					rspMsg = (ISOMsg)msg.clone();
					rspMsg.setResponseMTI();

					rspMsg.set(39,"00");
					ISOContextUtil.setOutIsoMsg(ctx, rspMsg);
				}
				return prepareImpl(id,ctx,msg,rspMsg);
			}catch(Exception e) {
				error("Exception when set response-mti " + id ,e);
			}
		}else {
			info("Cannot process null incoming message");
			
		}

		return ABORTED;
	}


	protected abstract int prepareImpl(long id,Context ctx,ISOMsg msg,ISOMsg rspMsg);


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