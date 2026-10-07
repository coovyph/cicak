package com.cck.service.impl;

import java.util.Iterator;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOHeader;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;
import org.jpos.iso.MUX;
import org.jpos.q2.iso.QMUX;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.stereotype.Service;

import com.cck.component.field.FieldDefinition;
import com.cck.component.field.FieldDefinitionException;
import com.cck.component.field.FieldGeneratorException;
import com.cck.component.field.IFieldValueGenerator;
import com.cck.dto.CckBody;
import com.cck.dto.CckHeaders;
import com.cck.dto.CckMsg;
import com.cck.service.CallService;
import com.cck.service.GeneratorRegistrarService;
import com.cck.util.ErrorCode;
import com.cck.util.ServiceConstant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class CallServiceImpl implements CallService{

	private final GeneratorRegistrarService generatorRegistrarService;


	private MUX getMux(CckMsg msg) {
		String interchangeName = msg.getInterchange();
		try{
			if(interchangeName!=null) {
				return QMUX.getMUX(interchangeName + "_mux");
			}
			return QMUX.getMUX(ServiceConstant.MUX_NAME);
		}catch(Exception e) {
			log.info("Cannot retrieve mux {}{}",ServiceConstant.MUX_NAME, e.getMessage());
			return null;
		}
	}


	private void setMsgValue(ISOMsg msg, String interchange,CckBody cckBody,String key)throws ISOException {
		IFieldValueGenerator fieldGenerator =null;
		try{
			FieldDefinition fd = new FieldDefinition();
			fd.parse(key);
			fieldGenerator = generatorRegistrarService.getFieldValueGenerator(fd.getSuffix());

			Object value = fieldGenerator.constructMsgValue(msg,interchange,cckBody, fd);

			if(value instanceof byte[]) {
				msg.set(fd.getFieldId(),(byte[])value);
			}else if(value instanceof ISOHeader){
				msg.setHeader((ISOHeader)value);
			}else {
				msg.set(fd.getFieldId(),value.toString());
			}
		}catch(FieldGeneratorException e) {
			throw new ISOException("Cannot generate msg field with key " + key + ":" + e.getMessage());
		}catch(FieldDefinitionException e) {
			throw new ISOException("Invalid field definition for key " + key);
		}catch(NoSuchBeanDefinitionException nfde) {
			throw new ISOException("Cannot field " + key + ":" + nfde.getMessage());
		}


	}



	private void constructReqMsg(CckMsg cckMsg,ISOMsg rqMsg)throws ISOException{

			rqMsg.setMTI(cckMsg.getMti());

			Iterator<String> iter = cckMsg.iter();

			String key = null;
			CckHeaders cckHeaders = cckMsg.getHeaders();
			String interchange = null;
			if(cckHeaders!=null) {
				interchange = cckHeaders.getInterchange();
			}
			if(interchange==null)interchange = "default";

			while(iter.hasNext()) {
				key = iter.next();
				setMsgValue(rqMsg,interchange,cckMsg.getBody(), key);
			}
		
	}

	public CckMsg call(CckMsg msg) {
		ISOMsg reqMsg = new ISOMsg();
		CckMsg cckRspMsg = new CckMsg();


		cckRspMsg.setMti(msg.getMti());
		cckRspMsg.setResponseMti();

		try {
			constructReqMsg(msg,reqMsg);
		}catch(ISOException e) {

			cckRspMsg.setErrorCode(ErrorCode.FORMAT_ERROR);
			cckRspMsg.setMessage(e.getMessage());
			return cckRspMsg;
		}

		cckRspMsg.setRequestIso(constructCckBody(reqMsg));
		MUX mux = getMux(msg);
		if(mux==null) {
			log.error("mux is not found...");
			cckRspMsg.setErrorCode(ErrorCode.SYSTEM_ERROR);
			cckRspMsg.setMessage("Mux is not found");
			return cckRspMsg;
		}

		if(!mux.isConnected()) {
			cckRspMsg.setErrorCode(ErrorCode.NOT_CONNECTED);
			cckRspMsg.setMessage("Host is not connected");
			return cckRspMsg;
			//send response error over here	
		}
		ISOMsg rspMsg = null;
		try{
			rspMsg = mux.request(reqMsg, 60000);
		}catch(ISOException e) {
			log.error("Exception when request message",  e);
			cckRspMsg.setErrorCode(ErrorCode.ERROR_ON_PROCESS);
			cckRspMsg.setMessage("Error on sending message");
			return cckRspMsg;
			//return something
		}
		if(rspMsg==null) {
			cckRspMsg.setErrorCode(ErrorCode.NO_RESPONSE);
			cckRspMsg.setMessage("Timeout no response");
		}else {

			cckRspMsg.setResponseIso(constructCckBody(rspMsg));
			cckRspMsg.setErrorCode(rspMsg.getString(39));
			cckRspMsg.setMessage("Receive response from host");
		}
		
		
		return cckRspMsg;
	}

	private CckBody constructCckBody(ISOMsg msg) {
		CckBody cckBody = new CckBody();
		try {
			cckBody.put("F0", msg.getMTI());
		}catch(ISOException ioe) {

		}
		Object value = null;
		for(int i=2;i<128;i++) {
			if(msg.hasField(i)) {
				value = msg.getValue(i);
				if(value instanceof String) {
					cckBody.put("F" + i,(String)value);
				}else if(value instanceof byte[]){
					cckBody.put("F" + i,ISOUtil.hexString((byte[])value));
				}
			}
		}
		return cckBody;
	}




}
