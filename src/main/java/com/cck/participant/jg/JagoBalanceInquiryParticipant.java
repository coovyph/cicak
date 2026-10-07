package com.cck.participant.jg;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;

public class JagoBalanceInquiryParticipant extends BaseJagoResponseParticipant{
	private String balanceInformation;
	private String accountNo;

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		// TODO Auto-generated method stub
		this.balanceInformation = cfg.get("balance-info");	
		this.accountNo = cfg.get("account-no");
	}

	@Override
	protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
		// TODO Auto-generated method stub
		rspMsg.set(39,"00");
		rspMsg.set(54,this.balanceInformation);
		rspMsg.set(102,this.accountNo);
		setCoreResponseSuccess(rspMsg);
		
		return PREPARED;
	}


}
