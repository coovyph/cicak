package com.cck.participant;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;

import com.cck.bean.AInterchange;
import com.cck.util.ISOContextUtil;

public class KeychangeParticipant extends BaseResponseParticipant {
  public void setConfiguration(Configuration cfg) throws ConfigurationException {}
  
  protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
    AInterchange interchange = AInterchange.getInterchange(ISOContextUtil.getEndpoint(ctx));
    if (interchange == null) {
      info(new Object[] { "Unexpected , interchange is not found" });
      rspMsg.set(39, "96");
    } else {
      interchange.manualLoadKey(msg, rspMsg);
    } 
    return 1;
  }
}
