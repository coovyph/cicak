package com.cck.util;

import org.jpos.util.Log;
import org.jpos.util.LogEvent;
import org.jpos.util.Logger;

public class GeckoLogger {
  private static void log(String level, Throwable t, Log log, String format, Object... args) {
    log(level, t, log, String.format(format, args));
  }
  
  private static void log(String level, Throwable t, Log log, String message) {
    LogEvent event = null;
    if (level == null || level.isEmpty()) {
      event = log.createLogEvent("info");
    } else {
      event = log.createLogEvent(level);
    } 
    event.addMessage(message);
    if (t != null)
      event.addMessage(t); 
    Logger.log(event);
  }
  
  public static void debug(Throwable t, Log log, String format, Object... args) {
    log("debug", t, log, format, args);
  }
  
  public static void debug(Log log, String format, Object... args) {
    log("debug", null, log, format, args);
  }
  
  public static void debug(Throwable t, Log log, String message) {
    log("debug", t, log, message);
  }
  
  public static void info(Throwable t, Log log, String format, Object... args) {
    log("info", t, log, format, args);
  }
  
  public static void info(Log log, String format, Object... args) {
    log("info", null, log, format, args);
  }
  
  public static void info(Throwable t, Log log, String message) {
    log("info", t, log, message);
  }
  
  public static void error(Throwable t, Log log, String format, Object... args) {
    log("error", t, log, format, args);
  }
  
  public static void error(Log log, String format, Object... args) {
    log("error", null, log, format, args);
  }
  
  public static void error(Throwable t, Log log, String message) {
    log("error", t, log, message);
  }
  
  public static void error(Log log, String message) {
    log("error", null, log, message);
  }
}
