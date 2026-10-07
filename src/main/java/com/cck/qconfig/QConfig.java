package com.cck.qconfig;

import java.util.Properties;

import org.jdom2.Element;

public interface QConfig {
	public void loadProperties(Properties p)throws ConfigException;
    public Element getElement();
    public String toElementString();
    
}
