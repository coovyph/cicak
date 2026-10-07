package com.cck.participant.jg;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;

public class JagoSuccessParticipant extends BaseJagoResponseParticipant{
	private String accountNo;

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		this.accountNo = cfg.get("account-no");
	}

	@Override
	protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
		rspMsg.set(39,"00");
		rspMsg.set(102,this.accountNo);
		setCoreResponseSuccess(rspMsg);

		return PREPARED;
	}

}
