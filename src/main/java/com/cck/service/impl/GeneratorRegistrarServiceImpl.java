package com.cck.service.impl;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import com.cck.component.field.IFieldValueGenerator;
import com.cck.service.GeneratorRegistrarService;

@Service
public class GeneratorRegistrarServiceImpl implements ApplicationContextAware,GeneratorRegistrarService{

	private ApplicationContext ctx;

	@Override
	public IFieldValueGenerator getFieldValueGenerator(String name) {
		// TODO Auto-generated method stub
		return this.ctx.getBean("field" + name , IFieldValueGenerator.class);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.ctx = applicationContext;
	}

}
