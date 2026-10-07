package com.cck.participant.jg;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;

import com.cck.util.JagoIbftAdditionalData;

public class JagoTransferInquiryParticipant extends BaseJagoResponseParticipant{
	private String destName;
	private String accountNo;

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		// TODO Auto-generated method stub
		this.accountNo = cfg.get("account-no");
		this.destName = cfg.get("dest-name","No name");
	}

	@Override
	protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
		// TODO Auto-generated method stub
		rspMsg.set(39,"00");
		rspMsg.set(102,this.accountNo);

		String f48 = msg.getString(48);

		JagoIbftAdditionalData jagoAdditionalData = new JagoIbftAdditionalData();
		if(f48!=null) {
			try {
				jagoAdditionalData.unpack(f48);
			}catch(ISOException ioe) {
				//ignore the error
				error("Cannot unpack f48 " + f48);
			}
		}
		jagoAdditionalData.put(JagoIbftAdditionalData.DEST_NAME,this.destName);

		rspMsg.set(48,jagoAdditionalData.pack());

		setCoreResponseSuccess(rspMsg);

		return PREPARED;
	}


}
