// Copyright 2021 PT ALTO NETWORK, All Rights Reserved
// This source code is protected by Indonesian and International copyright laws.
// Any reproduction, modification, disclosure and/or distribution of the source
// code in any form is strictly prohibited and may be unlawful without
// PT ALTO Network's written consent.
// All other copyright or ALTO trademark, including but not limited to this
// source code, is PT ALTO NETWORK's property.
// ============================================================================

package com.cck.bean;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jdom2.Element;
import org.jpos.core.Configurable;
import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.core.XmlConfigurable;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.NameRegistrar;

import com.cck.util.StringUtils;

import groovy.lang.GroovyObject;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

/**
 * 
 * @author hendra
 * 
 * Load and manage process script
 * 
  <script-manager name="" class="com.gecko.bean.ScriptManager" logger="Q2" realm="script-manager">
     <property name="groovyEngineUrl" value=""/>
     <scripts>
         <script name="echo" path="script/echo.groovy"/>
         <script name="balinq" path="script/balinq.groovy"/>
         <script name="withdrawal" path="script/withdrawal.groovy"/>
     </scripts>


  </script-manager>
 *
 * 2025-05-12
 * Using Groovy with JDK21 , the library has different 
 * Use GroovyObject instead of CompiledScript, the GroovyObject is not thread safe do not use global property
 * only method "void exec" will executed
 *
 */
public class ScriptManager extends QBeanSupport implements XmlConfigurable,Configurable{
	private Map<String, GroovyObject> groovyObjects = new HashMap();
	private Map<String,String> compilePath = new HashMap<String, String>();

	private Element e;
	private String groovyEngineUrl;
	private GroovyScriptEngine groovyScriptEngine;

	private String constructName() {
		return ScriptManager.constructName(getName());
	}

	private static String constructName(String name) {
		return name + ".script";
	}

	public static final  ScriptManager getScriptManager(String name) {
		Object result = NameRegistrar.getIfExists(constructName(name));
		if(result==null) {
			return null;
		}
		if(result instanceof ScriptManager) {
			return (ScriptManager)result;
		}
		return null;
	}


	@Override
	protected void initService() throws Exception {
		NameRegistrar.register(constructName(),this);
	}


	@Override
	protected void startService() throws Exception {
		this.compilePath.clear();
		this.groovyObjects.clear();

		this.groovyScriptEngine = new GroovyScriptEngine(this.groovyEngineUrl);

		Element scriptsElement = this.e.getChild("scripts");
		if(scriptsElement==null)return;
		List<Element> scripts =	scriptsElement.getChildren("script");
		scripts.forEach(e->compileScriptElement(e));

	}

	private void compileScriptElement(Element scriptElement) {
		String name = scriptElement.getAttributeValue("name");
		if(StringUtils.isEmpty(name)) {
			return;
		}
		String path = scriptElement.getAttributeValue("path");
		if(StringUtils.isEmpty(path)) {
			log.info("Cannot compile script '" + name + "' the path is empty");
			return;
		}
		this.compilePath.put(name, path);
		compileScript(name, path);
	}

	private boolean compileScript(String name,String path) {
		try {
			Class<GroovyObject> groovyObjectClass =  this.groovyScriptEngine.loadScriptByName(path);

			Constructor<GroovyObject> cr = groovyObjectClass.getConstructor();

			GroovyObject groovyObject =	cr.newInstance();

			this.groovyObjects.put(name, groovyObject);
			return true;
		}catch(ScriptException|ResourceException se) {
			log.info("Cannot load script " + path ,se);
		}catch(NoSuchMethodException nme) {
			log.info("Unexpected script error, no constructor " + path ,nme);
		}catch(InvocationTargetException|IllegalAccessException|InstantiationException nve) {
			log.info("Unexpected script error, illegal access " + path ,nve);
		}
		return false;
	}

	@Override
	protected void stopService() throws Exception {
		this.groovyObjects.clear();
		this.groovyObjects = null;
	}

	@Override
	protected void destroyService() throws Exception {
		NameRegistrar.unregister(constructName());
	}

	@Override
	public void setConfiguration(Element prE) throws ConfigurationException {
		this.e = prE;
	}

	@Override
	public void setConfiguration(Configuration cfg) throws ConfigurationException {
		this.groovyEngineUrl = cfg.get("groovyEngineUrl",".");
	}


	public boolean recompile(String name) {
		String path = this.compilePath.get(name);
		return compileScript(name,path);

	}


	public GroovyObject getGroovyObject(String name) {
		return this.groovyObjects.get(name);
	}
}
