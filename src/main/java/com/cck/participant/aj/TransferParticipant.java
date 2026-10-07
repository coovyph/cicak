package com.cck.participant.aj;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;

import com.cck.participant.BaseResponseParticipant;

public class TransferParticipant extends BaseResponseParticipant {
  private String rawDe48;
  
  public void setConfiguration(Configuration cfg) throws ConfigurationException {
    this.rawDe48 = cfg.get("de48");
  }
  
  protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
    rspMsg.set(48, this.rawDe48);
    rspMsg.set(39, "00");
    String pan = msg.getString(2);
    rspMsg.set(102, pan.substring(pan.length() - 10));
    String apprvCode = String.valueOf(System.currentTimeMillis());
    apprvCode = apprvCode.substring(apprvCode.length() - 6);
    rspMsg.set(38, apprvCode);
    return 1;
  }
}
