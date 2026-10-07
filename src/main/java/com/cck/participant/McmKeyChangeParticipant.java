package com.cck.participant;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOMsg;
import org.jpos.transaction.Context;

public class McmKeyChangeParticipant extends BaseResponseParticipant{

	private List<String> keys;
	private int counter=0;

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {

		String keyFile = cfg.get("key-file");
		File file = new File(keyFile);
		FileReader fr = null;
		BufferedReader br = null;
		this.keys = new ArrayList();
		try {
			fr = new FileReader(file);
			br = new BufferedReader(fr);

			String line = null;
			while((line=br.readLine())!=null) {
				if(!line.trim().equals(""))
					this.keys.add(line);
			}

		}catch(Exception e) {
			throw new ConfigurationException("Cannot load key " + e.getMessage());
		}finally {
			try {br.close();}catch(Exception e) {}
			try {fr.close();}catch(Exception e) {}
		}
	}


	@Override
	protected int prepareImpl(long id, Context ctx, ISOMsg msg, ISOMsg rspMsg) {
		rspMsg.set(48,this.keys.get(counter));
		this.counter++;
		if(this.counter>=keys.size()) {
			this.counter=0;
		}

		rspMsg.set(39, "00");
		return PREPARED;
	}


}