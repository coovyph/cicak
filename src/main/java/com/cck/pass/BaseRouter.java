package com.cck.pass;

import org.jpos.iso.ISOMsg;
import org.jpos.q2.QBean;
import org.jpos.q2.QBeanSupport;
import org.jpos.space.SpaceFactory;
import org.jpos.util.Log;
import org.jpos.util.NameRegistrar;

import com.cck.util.GeckoLogger;

public abstract class BaseRouter extends QBeanSupport implements IRouter {
  public static final int S_ORIGIN = 1;
  
  public static final int D_ORIGIN = 2;
  
  public static final int BOTH_ORIGIN = 0;
  
  private boolean isConnected(String destination) {
    return (SpaceFactory.getSpace().rdp(destination + ".ready") != null);
  }
  
  public boolean drop(Log log, String name) {
    try {
      QBean bean = getSocketBean(name);
      if (bean != null) {
        bean.stop();
        GeckoLogger.info(log, "Socket adaptor %s is stopped", new Object[] { name });
        return true;
      } 
      GeckoLogger.info(log, "Cannot find socket adaptor %s", new Object[] { name });
      return false;
    } catch (Exception e) {
      return false;
    } 
  }
  
  public boolean dropDest(Log log, String name, ISOMsg msg) {
    String destination = retrieveDestination(name, msg);
    if (destination == null) {
      GeckoLogger.info(log, "Cannot drop destination for source %s", new Object[] { name });
      return false;
    } 
    return drop(log, destination);
  }
  
  public boolean route(Log log, ISOMsg msg, String destination) {
    if (isConnected(destination)) {
      String key = destination + "-send";
      SpaceFactory.getSpace().out(key, msg, 30000L);
      return true;
    } 
    GeckoLogger.info(log, "Route destination %s is not connected", new Object[] { destination });
    return false;
  }
  
  public boolean routeToDest(Log log, ISOMsg msg, String source) {
    String destination = retrieveDestination(source, msg);
    if (destination == null) {
      GeckoLogger.info(log, "Cannot find destination for source %s", new Object[] { source });
      return false;
    } 
    if (!isConnected(destination)) {
      QBean qbean = getSocketBean(destination);
      if (qbean == null) {
        GeckoLogger.info(log, "No active socket for destination %s", new Object[] { destination });
        return false;
      } 
      if (qbean.getState() != 3 && qbean.getState() != 2) {
        GeckoLogger.info(log, "Starting socket for destination %s", new Object[] { destination });
        try {
          qbean.start();
        } catch (Exception e) {
          GeckoLogger.error(e, log, "Cannot start socket for destination %s", new Object[] { destination });
        } 
      } 
      GeckoLogger.info(log, "Destination %s is not connected", new Object[] { destination });
      return false;
    } 
    return route(log, msg, destination);
  }
  
  private QBean getSocketBean(String name) {
    Object objBean = null;
    try {
      objBean = NameRegistrar.get(name);
    } catch (Exception e) {
      return null;
    } 
    if (objBean instanceof QBean)
      return (QBean)objBean; 
    return null;
  }
  
  protected void initService() throws Exception {
    initServiceImpl();
    NameRegistrar.register(getName(), this);
    super.initService();
  }
  
  protected void destroyService() throws Exception {
    destroyServiceImpl();
    NameRegistrar.unregister(getName());
    super.destroyService();
  }
  
  protected abstract void initServiceImpl() throws Exception;
  
  protected abstract void destroyServiceImpl() throws Exception;
  
  protected abstract String retrieveDestination(String paramString, ISOMsg paramISOMsg);
}
