package com.cck.service.impl;

import org.jpos.iso.MUX;
import org.jpos.q2.QBeanSupport;
import org.jpos.q2.iso.QMUX;
import org.jpos.util.NameRegistrar;
import org.springframework.stereotype.Service;

import com.cck.service.ConnectionService;
import com.cck.util.ServiceConstant;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ConnectionServiceImpl implements ConnectionService{


	public boolean isConnected() {
		MUX mux = null;
		try{
			mux = QMUX.getMUX(ServiceConstant.MUX_NAME);
		}catch(Exception e) {
			log.info("Cannot retrieve mux {}{}",ServiceConstant.MUX_NAME, e.getMessage());
			return false;
		}
		return mux.isConnected();
	}

	public boolean stopConnection() {
		Object obj = NameRegistrar.getIfExists(ServiceConstant.SOCKET_NAME);
		if(!(obj instanceof QBeanSupport)) {
			log.info("Cannot find socket {}",ServiceConstant.SOCKET_NAME);
			return false;
		}

		QBeanSupport qBeanSupport = (QBeanSupport)obj;
		qBeanSupport.stop();
		return true;
	}

	public boolean startConnection(boolean restart) {
		Object obj = NameRegistrar.getIfExists(ServiceConstant.SOCKET_NAME);
		if(!(obj instanceof QBeanSupport)) {
			log.info("Cannot find socket {}",ServiceConstant.SOCKET_NAME);
			return false;
		}

		QBeanSupport qBeanSupport = (QBeanSupport)obj;

		if(restart) {
			qBeanSupport.stop();
		}else {
			if(qBeanSupport.running()) {
				return true;
			}
		}
		qBeanSupport.start();
		return true;
	}
}
