package com.cck.participant;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.space.SpaceFactory;
import org.jpos.transaction.Context;
import org.jpos.transaction.TransactionParticipant;
import org.jpos.util.Log;
import org.jpos.util.LogSource;
import org.jpos.util.Logger;

import com.cck.util.ISOContextUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CoreApiRemoveAcqAmtParticipant implements TransactionParticipant,LogSource,Configurable{
	private Log log;
	private List<String> panList;
	private long amount=-1;
	private String outQueue;

	private static ObjectMapper objectMapper = new ObjectMapper();

	@Override
	public int prepare(long id, Serializable context) {
		if (!(context instanceof Context)) {
			this.log.info("Cannot process incoming message, incoming message is not instance of Context");
			return ABORTED;
		}
		Context ctx = (Context) context;

		ISOMsg msg = ISOContextUtil.getInIsomsg(ctx);

		this.log.info("Got message : ");
		this.log.info(msg);
		
		if(msg==null) {
			this.log.info("Cannot process null incoming iso msg");
			return ABORTED;
		}

		removeAcqAmt(msg);
		SpaceFactory.getSpace().out(this.outQueue, msg,5000);
		return PREPARED;
	}


	private void removeAcqAmt(ISOMsg msg) {
		String raw47 = msg.getString(47);
		if(raw47==null || raw47.isEmpty())return;


		if(this.panList.isEmpty())return;

		if(! this.panList.contains(msg.getString(2)))return;
		if(this.amount>-1) {
			long amtLong = Long.parseLong(msg.getString(4));
			if(amtLong != this.amount)return;
		}

		String mti = null;
		try{
			mti = msg.getMTI();
		}catch(ISOException isoe) {
			return;
		}
		if(mti.charAt(1)!='4')return;//only reversal required handling



		Map map =null;
		try{
			map = objectMapper.readValue(raw47.getBytes(),Map.class);
		}catch(IOException de) {
			log.error("Cannot parse de 47", de);
			return;
		}

		log.info("Remove acq amount for pan " + msg.getString(2) + " , amount : " + msg.getString(4));


		map.remove("acq_amt");
		map.remove("acq_amt_crncy");
		try {
			msg.set(47,objectMapper.writeValueAsString(map));
		}catch(JsonProcessingException jpe) {
			log.error("Cannot remap de 47",jpe);
		}

	}

	@Override
	public void setLogger(Logger logger, String realm) {
		this.log = new Log(logger,realm);
	}

	@Override
	public String getRealm() {
		return this.getRealm();
	}

	@Override
	public Logger getLogger() {
		return this.getLogger();
	}

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		this.panList = new ArrayList<>();
		String rawPan = cfg.get("pan","");
		if(!rawPan.isEmpty()) {
			StringTokenizer tokenz = new StringTokenizer(rawPan,",");
			while(tokenz.hasMoreTokens()) {
				this.panList.add(tokenz.nextToken());
			}
		}

		String rawAmount = cfg.get("amount","");
		if(!rawAmount.isEmpty()) {
			try {
				this.amount = Long.parseLong(rawAmount);
			}catch(NumberFormatException ne) {
				this.amount = -1;
			}
		}
		this.outQueue = cfg.get("out");


	}

}
