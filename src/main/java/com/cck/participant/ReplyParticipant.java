package com.cck.participant;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.transaction.AbortParticipant;
import org.jpos.transaction.Context;

public class ReplyParticipant extends BaseResponseParticipant implements AbortParticipant{

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		//nothing
	}

	@Override
	protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {

		ISOSource source = msg.getSource();
		if(source==null) {
			info("Cannot send response, source is not set");
			return ABORTED;
		}

		if(!source.isConnected()) {
			info("Cannot send response, source is disconnected");
			return ABORTED;
		}
		try {
			source.send(rspMsg);
		}catch(Exception e) {
			error("Exception occurs when send response",e);
			return ABORTED;
		}
		return PREPARED;
	}


}
