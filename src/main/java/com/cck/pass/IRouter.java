package com.cck.pass;

import org.jpos.iso.ISOMsg;
import org.jpos.util.Log;

public interface IRouter {
  boolean drop(Log paramLog, String paramString);
  
  boolean dropDest(Log paramLog, String paramString, ISOMsg paramISOMsg);
  
  boolean route(Log paramLog, ISOMsg paramISOMsg, String paramString);
  
  int getSocketOrigin(String paramString, ISOMsg paramISOMsg);
  
  boolean routeToDest(Log paramLog, ISOMsg paramISOMsg, String paramString);
}
