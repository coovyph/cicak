package com.cck.participant.jg;

import java.util.UUID;

import org.jpos.iso.ISOMsg;

import com.cck.participant.BaseResponseParticipant;

public abstract class BaseJagoResponseParticipant extends BaseResponseParticipant{

	
	protected void setCoreResponseSuccess(ISOMsg rspMsg) {
		StringBuilder sb = new StringBuilder();
		sb.append("CE008APPROVEDCI036");
		sb.append(UUID.randomUUID().toString());
		
		rspMsg.set(126,sb.toString());
	}

}
