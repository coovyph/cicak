package com.cck.component;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.jpos.iso.ISOUtil;
import org.jpos.security.SMAdapter;
import org.jpos.security.SecureDESKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import com.cck.service.impl.Q2Service;
import com.cck.util.KeyUtil;

import lombok.extern.slf4j.Slf4j;

@Component
@ConditionalOnProperty(prefix = "cck.sm",name="enabled",havingValue = "true")
@Slf4j
public class KeyStoreManager {
	private Map<String, SecureDESKey> keyMap;
	private Map<String, String> parentKeyMap;
	private String fileName;

	public static final String DEFAULT_IMK_AC = "DEFAULT-MK-AC";

	public KeyStoreManager(@Value("${cck.sm.keyStore}")String keyStoreFileName) {
		this.fileName = keyStoreFileName;
	}

	@PostConstruct
	public void reloadKey() {
		this.keyMap = new HashMap();
		this.parentKeyMap = new HashMap();

		try(InputStream is = new FileInputStream(this.fileName)){
			Properties p = new Properties();
			p.load(is);

			Set<Object> set =  p.keySet();

			set.forEach(k->{
				prepareDesKey(k,p.get(k));
			});
		}catch(IOException ioe) {
			log.error("Cannot load key ",ioe);
		}
	}

	public static final KeyStoreManager getInstance() {
		return Q2Service.getBean(KeyStoreManager.class);
	}

	public synchronized void persist() {
		Properties p = new Properties();
		this.keyMap.forEach((k,v)->{
			prepareStoredKey(p,k, v);
		});

		try(OutputStream os = new FileOutputStream(new File(this.fileName)) ){
			p.store(os, "");
		}catch(IOException e) {
			log.error("Cannot store key",e);
		}
	}

	private void prepareStoredKey(Properties p, String name,SecureDESKey key) {
		KeyValue keyValue = new KeyValue(name);
		keyValue.setName(name);
		keyValue.setLength(key.getKeyLength());

		String keyType = key.getKeyType();
		int idx = keyType.indexOf(':');
		if(idx>0) {
			keyType = keyType.substring(0,idx);
		}


		keyValue.setVariant(String.valueOf(key.getVariant()));

		keyValue.setType(keyType);

		byte[] bytes = key.getKeyBytes();
		if(bytes!=null)
			keyValue.setValue(ISOUtil.hexString(bytes));

		byte[] kcv = key.getKeyCheckValue();
		if(kcv!=null)
			keyValue.setKeyCheck(ISOUtil.hexString(kcv));

		String parent = this.parentKeyMap.get(name);
		keyValue.setParent(parent);

		p.setProperty(name, keyValue.pack());
	}


	public void setAcqKwkKey(String name, SecureDESKey value) {
		setKey("ZPK", String.valueOf(name) + "-acq", value);
	}

	public void setIssKwkKey(String name, SecureDESKey value) {
		setKey("ZPK", String.valueOf(name) + "-iss", value);
	}

	public SecureDESKey getAcqKwkKey(String name) {
		return getKey("ZPK", String.valueOf(name) + "-acq");
	}

	public SecureDESKey getIssKwkKey(String name) {
		return getKey("ZPK", String.valueOf(name) + "-iss");
	}

	public void setKey(String type,String name,SecureDESKey value) {
		setKey(type + "-" + name, value);
	}

	public void setKey(String key,SecureDESKey value) {
		this.keyMap.put(key, value);
	}


	public SecureDESKey getKey(String type,String name) {
		return getKey(type + "-" + name);
	}
	public SecureDESKey getKey(String key) {
		return this.keyMap.get(key);
	}

	private void prepareDesKey(Object objName,Object objValue) {
		String value = objValue.toString();
		String name = objName.toString();
		KeyValue keyValue = createKeyValue(name,value);
		if(keyValue==null) {
			return;
		}


		String keyType = KeyUtil.constructKeyType(keyValue.getType(), keyValue.getVariant(),keyValue.getLength());
		SecureDESKey secureDesKey = new SecureDESKey();
		secureDesKey.setKeyName(name);
		secureDesKey.setKeyType(keyType);
		secureDesKey.setVariant((byte)Integer.parseInt(keyValue.getVariant()));

		byte[] bytes = null;
		try {
			bytes = ISOUtil.hex2byte(keyValue.getValue());
		}catch(Exception e) {

		}

		byte[] kcv = null;
		try {
			kcv = ISOUtil.hex2byte(keyValue.getKeyCheck());
		}catch(Exception e) {

		}


		secureDesKey.setKeyBytes(bytes);
		secureDesKey.setKeyCheckValue(kcv);
		secureDesKey.setKeyLength(keyValue.getLength());

		this.keyMap.put(name, secureDesKey);
		if(keyValue.getParent()!=null) {
			this.parentKeyMap.put(name, keyValue.getParent());
		}

	}
	private KeyValue createKeyValue(String name,String value) {
		KeyValue keyValue = new KeyValue(name);
		try {
			keyValue.unpack(value);
			return keyValue;
		}catch(KeyException e) {
			log.info("Ignore invalid key {}:{}" , value , e.getMessage());
			return null;
		}
	}

	public static final class KeyValue{
		public static final String SEPARATOR = ";";
		private String name;
		private String type;
		private String variant;
		private String parent;
		private short length;
		private String value;
		private String keyCheck;

		KeyValue(String name){
			setName(name);
		}

		public void setVariant(String variant) {
			this.variant = variant;
		}

		public String getVariant() {
			return variant;
		}

		public void setLength(short length) {
			this.length = length;
		}

		public short getLength() {
			return length;
		}

		public void setParent(String parent) {
			this.parent = parent;
		}

		public String getParent() {
			return parent;
		}

		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		public String getValue() {
			return value;
		}
		public void setValue(String value) {
			this.value = value;
		}
		public String getKeyCheck() {
			return keyCheck;
		}
		public void setKeyCheck(String keyCheck) {
			this.keyCheck = keyCheck;
		}


		public String pack() {
			StringBuilder sb = new StringBuilder();

			if(this.type!=null)
				sb.append(this.type);
			sb.append(SEPARATOR);

			if(this.length==SMAdapter.LENGTH_DES3_2KEY) {
				sb.append("2");
			}else if(this.length==SMAdapter.LENGTH_DES3_3KEY) {
				sb.append("3");
			}else {
				sb.append("1");
			}
			sb.append(SEPARATOR);

			if(this.variant!=null && !this.variant.isEmpty()) {
				sb.append(this.variant);
			}
			sb.append(SEPARATOR);

			if(this.parent!=null)
				sb.append(parent);
			sb.append(SEPARATOR);
			if(this.value!=null)
				sb.append(this.value);
			sb.append(SEPARATOR);

			if(this.keyCheck!=null)
				sb.append(this.keyCheck);

			return sb.toString();
		}

		private boolean isValidType(String type) {
			return (SMAdapter.TYPE_ZMK.equals(type) ||SMAdapter.TYPE_ZPK.equals(type) ||
					SMAdapter.TYPE_TMK.equals(type) ||SMAdapter.TYPE_TPK.equals(type) ||
					SMAdapter.TYPE_TAK.equals(type) ||SMAdapter.TYPE_PVK.equals(type) ||
					SMAdapter.TYPE_CVK.equals(type) ||SMAdapter.TYPE_BDK.equals(type) ||
					SMAdapter.TYPE_ZAK.equals(type) ||SMAdapter.TYPE_MK_AC.equals(type) ||
					SMAdapter.TYPE_MK_SMI.equals(type) ||SMAdapter.TYPE_MK_SMC.equals(type) ||
					SMAdapter.TYPE_MK_CVC3.equals(type) ||SMAdapter.TYPE_MK_DAC.equals(type) ||
					SMAdapter.TYPE_MK_DN.equals(type) ||SMAdapter.TYPE_ZEK.equals(type) ||
					SMAdapter.TYPE_DEK.equals(type) ||SMAdapter.TYPE_RSA_SK.equals(type) ||
					SMAdapter.TYPE_HMAC.equals(type) ||SMAdapter.TYPE_RSA_PK.equals(type));
		}

		private boolean prepareKeyLength(String length) {
			int l = 0;
			try {
				l = Integer.parseInt(length);

				if(l==1) {
					setLength(SMAdapter.LENGTH_DES);
				}else if(l==2) {
					setLength(SMAdapter.LENGTH_DES3_2KEY);
				}else if(l==3) {
					setLength(SMAdapter.LENGTH_DES3_3KEY);
				}else {
					return false;
				}

				return true;
			}catch(Exception e) {

			}
			return false;
		}

		public void unpack(String value) throws KeyException{
			if(value==null)throw new KeyException("Cannot unpack null value");

			String[] fields = value.split(SEPARATOR);
			if(fields.length<5)throw new KeyException("Field value should be 5");
			String 	temp = fields[0];
			if(!isValidType(temp)) {
				throw new KeyException("2nd field type '" + temp + "' is not valid");
			}
			setType(temp);

			temp = fields[1];
			if(!prepareKeyLength(temp)) {
				throw new KeyException("3rd field key length '" + temp + "' is not valid");
			}


			temp = fields[2];

			if(temp.isEmpty()) {
				setVariant("0");
			}else {
				setVariant(temp);
			}



			setParent(fields[3]);
			setValue(fields[4]);
			setKeyCheck(fields[5]);

		}

	}

}
