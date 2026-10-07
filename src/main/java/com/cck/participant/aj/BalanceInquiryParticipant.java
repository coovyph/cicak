package com.cck.participant.aj;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.transaction.Context;

import com.cck.participant.BaseResponseParticipant;

public class BalanceInquiryParticipant extends BaseResponseParticipant {
	private Configuration cfg = null;

	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		this.cfg = cfg;
	}

	protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
		rspMsg.set(39, "00");
		String processingCode = msg.getString(3);
		String fromAccountType = processingCode.substring(2, 4);
		StringBuilder sb = new StringBuilder();
		sb.append(fromAccountType);
		sb.append("02");
		sb.append("360");
		sb.append("C");
		String pan = msg.getString(2);
		String prefix = pan.substring(0, 6);
		long balance = this.cfg.getLong(prefix, 450000L);
		sb.append(ISOUtil.zeropad(balance, 12));
		rspMsg.set(102, pan.substring(pan.length() - 10));
		String apprvCode = String.valueOf(System.currentTimeMillis());
		apprvCode = apprvCode.substring(apprvCode.length() - 6);
		rspMsg.set(38, apprvCode);
		rspMsg.set(54, sb.toString());
		return 1;
	}
}
