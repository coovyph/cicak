package com.cck.qconfig;

import java.io.File;

import org.jdom2.input.SAXBuilder;
import org.jpos.q2.ConfigDecorationProvider;

public class CicakQ2Decorator implements ConfigDecorationProvider{
	private ConfigFactory factory;
	public CicakQ2Decorator() {
		this.factory= new ConfigFactory();
	}

	public void initialize(File deployDir) throws Exception {

	}

	public void uninitialize() {

	}

	public String decorateFile(File f) throws Exception {
		QConfig cf = this.factory.parse(f);
		return cf.toElementString();
	}

	private SAXBuilder createSAXBuilder () {
		SAXBuilder builder = new SAXBuilder ();
		builder.setFeature("http://xml.org/sax/features/namespaces", true);
		builder.setFeature("http://apache.org/xml/features/xinclude", true);
		return builder;
	}

}
