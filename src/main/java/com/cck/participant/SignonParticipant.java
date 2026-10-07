package com.cck.participant;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;

import com.cck.bean.AInterchange;
import com.cck.util.ISOContextUtil;

public class SignonParticipant extends BaseResponseParticipant{

	private boolean signOnMode;
	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		this.signOnMode = cfg.getBoolean("sign-on-mode",true);
	}

	@Override
	protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
		AInterchange interchange = AInterchange.getInterchange(ISOContextUtil.getEndpoint(ctx));
		if(interchange==null) {
			info("Unexpected , interchange is not found");
			rspMsg.set(39,"96");
		}else {
			interchange.setSignedOn(this.signOnMode);
			rspMsg.set(39,"00");
		}
		return PREPARED;
	}

}
