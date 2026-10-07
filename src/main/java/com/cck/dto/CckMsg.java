package com.cck.dto;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;

import com.cck.util.ErrorCode;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class CckMsg implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String mti;
	private long timeout;
	private String errorCode;
	private String message;
	@JsonInclude(Include.NON_EMPTY)
	private CckHeaders headers;
	@JsonInclude(Include.NON_EMPTY)
	private CckBody body;
	@JsonInclude(Include.NON_EMPTY)
	private CckBody requestIso;
	@JsonInclude(Include.NON_EMPTY)
	private CckBody responseIso;

	public CckMsg() {
		super();
		this.body = new CckBody();
		this.headers = new CckHeaders();
		this.errorCode = ErrorCode.APPROVED;
	}

	public void setResponseMti() {
		if (!isRequest())return;

		char c1 = this.mti.charAt(3);
		char c2 = '0';
		switch (c1)
		{
		case '0' :
		case '1' : c2='0';break;
		case '2' :
		case '3' : c2='2';break;
		case '4' :
		case '5' : c2='4';break;

		}
		this.mti = this.mti.substring(0,2) + (Character.getNumericValue(this.mti.charAt (2))+1) + c2;

	}

	@JsonIgnore
	public boolean isRequest()  {
		return Character.getNumericValue(this.mti.charAt (2))%2 == 0;
	}

	public void setRequestIso(CckBody requestIso) {
		this.requestIso = requestIso;
	}
	
	public CckBody getRequestIso() {
		return requestIso;
	}
	
	public void setResponseIso(CckBody responseIso) {
		this.responseIso = responseIso;
	}
	
	public CckBody getResponseIso() {
		return responseIso;
	}

	public String getMti() {
		return mti;
	}
	public void setMti(String mti) {
		this.mti = mti;
	}
	public long getTimeout() {
		return timeout;
	}
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}

	public void setInterchange(String inerchange) {
		this.headers.setInterchange(inerchange);
	}

	public String getInterchange() {
		return this.headers.getInterchange();
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public CckBody getBody() {
		return body;
	}
	public void setBody(Map<String, Object> extBody) {
		if(extBody==null)return;
		extBody.forEach((k,v)->{
			this.body.put(k.toUpperCase(), v);
		});
	}

	public void putBodyValue(String key,String value) {
		this.body.put(key, value);
	}
	public void putBodyValue(String key,Object value) {

		this.body.put(key, value);
	}

	public Object getBodyValue(String key) {
		return this.body.get(key);
	}

	public String getBodyValueAsString(String key) {
		Object value = getBodyValue(key);
		if(value==null)return null;
		return value.toString();
	}


	public CckHeaders getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, String> headers) {
		this.headers.putAll(headers);
	}

	public void putHeaderValue(String key,String value) {
		this.headers.put(key, value);
	}

	public String getHeaderValue(String key) {
		return this.headers.get(key);
	}


	public Iterator<String> iter(){
		return this.body.keySet().iterator();
	}

	
}
