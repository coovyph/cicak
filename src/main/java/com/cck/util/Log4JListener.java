package com.cck.util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jpos.util.LogEvent;
import org.jpos.util.LogListener;
import org.jpos.util.LogSource;
import org.jpos.util.Loggeable;

public class Log4JListener implements LogListener{

	private Logger getLog4jLogger(LogEvent ev) {
		Logger logger = LogManager.getLogger(ev.getRealm());
		if(logger==null) {
			LogSource source =	ev.getSource();

			return LogManager.getLogger(source.getClass());
		}else {
			return logger;
		}
	}

	private Level getLogLevel(LogEvent ev) {
		try {
			return Level.valueOf(ev.getTag());
		}catch(Exception e) {
			return Level.INFO;
		}
	}

	private void doLog(Logger logger,LogEvent ev) {
		Level level = getLogLevel(ev);
		if(level==null)return;

		List<Object> payload =	ev.getPayLoad();
		if(payload==null) {
			logger.log(level, ev.getTag());
		}else {
			for(Object o:payload) {
				if(o instanceof Exception) {
					logger.log(level, o);
				}else if(o instanceof Loggeable) {
					String flog = dumpLoggeable((Loggeable)o);
					logger.log(level, flog);
				}else {
					logger.log(level, o);
				}
			}
		}

	}

	private String dumpLoggeable(Loggeable loggeable) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			PrintStream ps = new PrintStream(baos, true);
			loggeable.dump(ps,"");

			return new String(baos.toByteArray());
		}finally {
			try {baos.close();}catch(Exception e) {}
		}
	}
	public LogEvent log(LogEvent ev) {
		Logger logger = getLog4jLogger(ev);
		if(logger!=null) {
			doLog(logger, ev);
		}
		return ev;
	}

}
