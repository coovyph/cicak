package com.cck.participant;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.space.SpaceFactory;
import org.jpos.transaction.Context;

import com.cck.bean.AInterchange;
import com.cck.util.ISOContextUtil;

public class KeyRequestParticipant extends BaseResponseParticipant{
	private String queue;
	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		this.queue  = cfg.get("queue");
	}

	@Override
	protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
		String interchangeName = ISOContextUtil.getEndpoint(ctx);
		if(interchangeName==null) {
			info("Interchange name is not set");
			rspMsg.set(39,"96");
			return ABORTED;
		}

		AInterchange interchange =	AInterchange.getInterchange(interchangeName);
		if(interchange==null) {
			info("Cannot find interchange " + interchangeName);
			rspMsg.set(39,"96");
			return ABORTED;
		}

		ISOMsg reqMsg = (ISOMsg)msg.clone();
		reqMsg.set(70,"163");
		
		Context nCtx = new Context();
		
		ISOContextUtil.setInIsoMsg(nCtx, reqMsg);
		ISOContextUtil.setEndpoint(nCtx, interchangeName);

		SpaceFactory.getSpace().out(this.queue,nCtx,30000);

		interchange.doKeyChange(interchange.retrieveMUX());

		rspMsg.set(39,"00");
		return PREPARED;
	}

}
