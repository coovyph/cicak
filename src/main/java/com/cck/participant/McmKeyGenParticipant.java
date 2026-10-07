package com.cck.participant;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.security.SecureDESKey;
import org.jpos.security.jceadapter.GeckoSecurityModule;
import org.jpos.transaction.Context;

import com.cck.bean.AInterchange;
import com.cck.service.impl.Q2Service;
import com.cck.util.CicakMsgUtility;
import com.cck.util.ISOContextUtil;

public class McmKeyGenParticipant extends BaseResponseParticipant{
	@Override
	protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
		AInterchange interchange = AInterchange.getInterchange(ISOContextUtil.getEndpoint(ctx));
		if(interchange!=null) {
			SecureDESKey masterKey = interchange.getMasterKey();

			SecureDESKey workingKey = null;
			try{
				workingKey = CicakMsgUtility.generateNetworkKey(masterKey, Q2Service.getBean(GeckoSecurityModule.class));
			}catch(Exception e) {
				error("Cannot generate network key",e);
				rspMsg.set(39,"96");
				return PREPARED;
			}

			StringBuilder sb = new StringBuilder();
			sb.append(ISOUtil.hexString(workingKey.getKeyBytes()));
			sb.append("|");
			sb.append(ISOUtil.hexString(workingKey.getKeyCheckValue()));

			rspMsg.set(39,"00");
			rspMsg.set(48,sb.toString());
		}
		return PREPARED;
	}

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		// no configuration over here
	}
}
