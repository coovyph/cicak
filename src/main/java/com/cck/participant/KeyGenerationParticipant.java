package com.cck.participant;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;

import com.cck.bean.AInterchange;
import com.cck.util.ISOContextUtil;

public class KeyGenerationParticipant extends BaseResponseParticipant{

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {

	}

	@Override
	protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
				
		AInterchange interchange = AInterchange.getInterchange(ISOContextUtil.getEndpoint(ctx));
		if(interchange!=null) {
			interchange.doKeyChange(interchange.retrieveMUX());
		}
		return PREPARED;
	}

}
