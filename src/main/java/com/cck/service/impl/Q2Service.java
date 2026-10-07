package com.cck.service.impl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.jpos.util.NameRegistrar;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

@Service
public class Q2Service implements ApplicationContextAware {
	private CckQ2 q2;
	public static final String Q2_CONTEXT = "q2ctx";

	@Value("${cck.q2.deployDir:deploy}")
	private String deploy;
	
	@PostConstruct
	public void init() {
		this.q2 = new CckQ2(new String[]{"-d" +this.deploy,"-r"});	
		this.q2.start();
	}

	@PreDestroy
	public void stop() {
		this.q2.stop();
	}

	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		NameRegistrar.register(Q2_CONTEXT, applicationContext);
	}

	private static ApplicationContext getContext() {
		Object obj = NameRegistrar.getIfExists(Q2_CONTEXT);
		if(obj instanceof ApplicationContext) {
			return (ApplicationContext)obj;
		}
		return null;
	}

	public static final <T> T getBean(String name,Class<T> clazz) {
		ApplicationContext ctx = getContext();
		if(ctx!=null)return ctx.getBean(name, clazz);
		return null;
	}

	public static final <T> T getBean(Class<T> clazz) {
		ApplicationContext ctx = getContext();
		if(ctx!=null)return ctx.getBean(clazz);
		return null;
	}


}
