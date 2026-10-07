package com.cck.participant;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;

public class SuccessParticipant extends BaseResponseParticipant{

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		
	}

	@Override
	protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
		rspMsg.set(39,"00");
		return PREPARED;
	}

}
