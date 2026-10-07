package com.cck.participant.aj;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOSource;
import org.jpos.iso.MUX;
import org.jpos.transaction.Context;

import com.cck.bean.AInterchange;
import com.cck.participant.BaseResponseParticipant;
import com.cck.util.ISOContextUtil;

public class SignOnParticipant extends BaseResponseParticipant {
	public void setConfiguration(Configuration cfg) throws ConfigurationException {}

	private boolean reply(ISOMsg msg, ISOMsg rspMsg) {
		ISOSource source = msg.getSource();
		if (source == null) {
			info(new Object[] { "Cannot send response, source is not set" });
			return false;
		} 
		if (!source.isConnected()) {
			info(new Object[] { "Cannot send response, source is disconnected" });
			return false;
		} 
		try {
			source.send(rspMsg);
		} catch (Exception e) {
			error(new Object[] { "Exception occurs when send response", e });
			return false;
		} 
		return true;
	}

	protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
		String interchangeName = ISOContextUtil.getEndpoint(ctx);
		AInterchange interchange = AInterchange.getInterchange(interchangeName);
		boolean signedOn = true;
		if (interchange != null) {
			interchange.setSignedOn(true);
			rspMsg.set(39, "00");
		} else {
			rspMsg.set(39, "05");
			signedOn = false;
		} 
		if (reply(msg, rspMsg) && signedOn) {
			MUX mux = interchange.retrieveMUX();
			if (mux != null) {
				interchange.doKeyChange(mux);
			} else {
				error(new Object[] { "Cannot trigger keychange" });
			} 
		} 
		return 1;
	}
}