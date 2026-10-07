package com.cck.qconfig;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Constructor;
import java.util.Properties;

public  class ConfigFactory {

	public static final String P_CLASS_NAME = "class";
	private QConfig createConfig(Properties p)throws ConfigException {
		String className = p.getProperty(P_CLASS_NAME);
		if(className==null)throw new ConfigException("Class name is null or not set");
		Class c = null;
		try {
			c = Class.forName(className);
		}catch(ClassNotFoundException ce) {
			throw new ConfigException("Cannot find class " + className );
		}

		Constructor<QConfig> cr = null;
		try{
			cr = c.getConstructor();
		}catch(NoSuchMethodException ce) {
			throw new ConfigException("Default constructor with no parameter is not exists for class " + className);
		}

		QConfig config = null;
		try{
			cr.newInstance();
		}catch(Exception e) {
			throw new ConfigException("Exception when ini the QConfig class",e);
		}
		config.loadProperties(p);
		return config;
	}

	public  QConfig parse(File file) throws ConfigException{
		Properties p = new Properties();
		FileInputStream fis = null;
		try {
			fis = new FileInputStream(file);
			p.load(fis);
		}catch(Exception e) {
			throw new ConfigException("Cannot load config file '" + file.getName() + "' :" + e.getMessage());
		}finally {
			try {fis.close();}catch(Exception e) {}
		}
		return createConfig(p);
	}

}
