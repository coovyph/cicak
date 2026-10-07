package com.cck.participant.aj;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;

import com.cck.bean.AInterchange;
import com.cck.participant.BaseResponseParticipant;
import com.cck.util.ISOContextUtil;

public class SignOffParticipant extends BaseResponseParticipant {
  public void setConfiguration(Configuration cfg) throws ConfigurationException {}
  
  protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
    String interchangeName = ISOContextUtil.getEndpoint(ctx);
    AInterchange interchange = AInterchange.getInterchange(interchangeName);
    if (interchange != null)
      interchange.setSignedOn(false); 
    rspMsg.set(39, "00");
    return 1;
  }
}
