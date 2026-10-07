package com.cck.component;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Coovy(Hendra Marzuki)
 * 2025-05-12 
 * Bin Manager will store bin and IMK-AC,CVK
 */
@Component
@ConditionalOnProperty(prefix = "cck.card",name="enabled",havingValue = "true")
@Slf4j
public class BinManager {
	private String fileName;
	private Map<String, String> imkAcMap;
	private Map<String, String> cvkMap;

	public BinManager(@Value("${cck.card.binStore}")String binFileName) {
		this.fileName = binFileName;

		this.imkAcMap = new HashMap();
		this.cvkMap = new HashMap();
	}

	@PostConstruct
	public synchronized void reload() {
		Map<String, String> tempImkAcMap = new HashMap();
		Map<String, String> tempCvkMap = new HashMap();

		Properties p = new Properties();
		try(FileInputStream fis = new FileInputStream(this.fileName)){
			p.load(fis);	
		}catch(FileNotFoundException fne) {
			log.error("File {} is not found, System will use old map if exists",this.fileName);
			return;
		}catch(IOException ioe) {
			log.error("Cannot open file {}, System will use old map if exists",this.fileName);
			return;
		}

		p.forEach((k,v)->{
			loadKey(k.toString(), v.toString(), tempImkAcMap, tempCvkMap);
		});

		this.imkAcMap.clear();
		this.imkAcMap.putAll(tempImkAcMap);

		this.cvkMap.clear();
		this.cvkMap.putAll(tempCvkMap);

	}

	private void loadKey(String key,String value,Map<String,String> tempImkAcMap,Map<String,String> tempCvkMap) {
		if(value.isEmpty())return;
		String[] keyNames = value.split(";");
		tempImkAcMap.put(key, keyNames[0]);
		if(keyNames.length>1) {
			tempCvkMap.put(key, keyNames[1]);
		}
	}

	public String getImkAcKeyName(String cardNo) {
		if(cardNo==null || cardNo.isEmpty())return null;
		String bin = cardNo.length()>6?cardNo.substring(0,6):cardNo;
		return this.imkAcMap.get(bin);
	}

	public String getCvkKeyName(String cardNo) {
		if(cardNo==null || cardNo.isEmpty())return null;
		String bin = cardNo.length()>6?cardNo.substring(0,6):cardNo;
		return this.cvkMap.get(bin);
	}

}
