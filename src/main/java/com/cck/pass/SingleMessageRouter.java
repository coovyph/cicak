package com.cck.pass;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;

public class SingleMessageRouter extends BaseRouter {
  public static final String P_SOURCE = "source";
  
  public static final String P_DEST = "dest";
  
  private String source;
  
  private String dest;
  
  public void setConfiguration(Configuration cfg) throws ConfigurationException {
    super.setConfiguration(cfg);
    this.source = cfg.get("source");
    this.dest = cfg.get("dest");
  }
  
  public int getSocketOrigin(String socketSource, ISOMsg msg) {
    if (this.source.equals(socketSource))
      return 1; 
    if (this.dest.equals(socketSource))
      return 2; 
    return 0;
  }
  
  protected String retrieveDestination(String source, ISOMsg msg) {
    if (this.source.equals(source))
      return this.dest; 
    if (this.dest.equals(source))
      return this.source; 
    return "unknown";
  }
  
  protected void initServiceImpl() throws Exception {}
  
  protected void destroyServiceImpl() throws Exception {}
}
