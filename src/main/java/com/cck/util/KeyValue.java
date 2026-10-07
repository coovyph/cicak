package com.cck.util;

import org.jpos.security.SMAdapter;

public  class KeyValue{
	public static final String SEPARATOR = ";";
	private String name;
	private String type;
	private String variant;
	private String parent;
	private short length;
	private String value;
	private String keyCheck;

	public KeyValue(String name){
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