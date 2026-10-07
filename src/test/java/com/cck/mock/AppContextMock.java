package com.cck.mock;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.springframework.core.io.Resource;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;


public class AppContextMock implements ApplicationContext {

	private String lastServiceName;
	private Map<String,Object> map = new HashMap<String, Object>();
	private Map<String,String> shortNameMap = new HashMap<String, String>();

	public String getLastServiceName() {
		return lastServiceName;
	}

	public void register(Object obj) {
		if(obj==null)return;
		this.map.put(obj.getClass().getCanonicalName(), obj);
	}

	public void register(String name,Object obj) {
		if(obj == null)return;
		this.shortNameMap.put(name, obj.getClass().getCanonicalName());
		this.map.put(name, obj);
	}

	public void unregister(Class<?> clazz) {
		this.map.remove(clazz.getCanonicalName());
	}
	@Override
	public Environment getEnvironment() {
		throw new UnsupportedOperationException("Unimplemented method 'getEnvironment'");
	}

	@Override
	public boolean containsBeanDefinition(String beanName) {
		throw new UnsupportedOperationException("Unimplemented method 'containsBeanDefinition'");
	}

	@Override
	public int getBeanDefinitionCount() {
		throw new UnsupportedOperationException("Unimplemented method 'getBeanDefinitionCount'");
	}

	@Override
	public String[] getBeanDefinitionNames() {
		throw new UnsupportedOperationException("Unimplemented method 'getBeanDefinitionNames'");
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType, boolean allowEagerInit) {
		throw new UnsupportedOperationException("Unimplemented method 'getBeanProvider'");
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType, boolean allowEagerInit) {
		throw new UnsupportedOperationException("Unimplemented method 'getBeanProvider'");
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type) {
		throw new UnsupportedOperationException("Unimplemented method 'getBeanNamesForType'");
	}

	@Override
	public String[] getBeanNamesForType(ResolvableType type, boolean includeNonSingletons, boolean allowEagerInit) {
		throw new UnsupportedOperationException("Unimplemented method 'getBeanNamesForType'");
	}

	@Override
	public String[] getBeanNamesForType(Class<?> type) {
		throw new UnsupportedOperationException("Unimplemented method 'getBeanNamesForType'");
	}

	@Override
	public String[] getBeanNamesForType(Class<?> type, boolean includeNonSingletons, boolean allowEagerInit) {
		throw new UnsupportedOperationException("Unimplemented method 'getBeanNamesForType'");
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type) throws BeansException {
		throw new UnsupportedOperationException("Unimplemented method 'getBeansOfType'");
	}

	@Override
	public <T> Map<String, T> getBeansOfType(Class<T> type, boolean includeNonSingletons, boolean allowEagerInit)
			throws BeansException {
		throw new UnsupportedOperationException("Unimplemented method 'getBeansOfType'");
	}

	@Override
	public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
		throw new UnsupportedOperationException("Unimplemented method 'getBeanNamesForAnnotation'");
	}

	@Override
	public Map<String, Object> getBeansWithAnnotation(Class<? extends Annotation> annotationType)
			throws BeansException {
		throw new UnsupportedOperationException("Unimplemented method 'getBeansWithAnnotation'");
	}

	@Override
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType)
			throws NoSuchBeanDefinitionException {
		throw new UnsupportedOperationException("Unimplemented method 'findAnnotationOnBean'");
	}

	@Override
	public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType,
			boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
		throw new UnsupportedOperationException("Unimplemented method 'findAnnotationOnBean'");
	}

	@Override
	public Object getBean(String name) throws BeansException {
		Object obj = map.get(name);
		if(obj == null) {
			name = shortNameMap.get(name);
			if(name!=null) {
				obj = map.get(name);
			}
		}
		return obj;
	}

	@Override
	public <T> T getBean(String name, Class<T> requiredType) throws BeansException {
		Object obj = getBean(name);
		return (T)obj;
	}

	@Override
	public Object getBean(String name, Object... args) throws BeansException {
		throw new UnsupportedOperationException("Unimplemented method 'getBean'");
	}

	@Override
	public <T> T getBean(Class<T> requiredType) throws BeansException {
		return getBean(requiredType.getCanonicalName(),requiredType);
	}

	@Override
	public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
		throw new UnsupportedOperationException("Unimplemented method 'getBean'");
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(Class<T> requiredType) {
		throw new UnsupportedOperationException("Unimplemented method 'getBeanProvider'");
	}

	@Override
	public <T> ObjectProvider<T> getBeanProvider(ResolvableType requiredType) {
		throw new UnsupportedOperationException("Unimplemented method 'getBeanProvider'");
	}

	@Override
	public boolean containsBean(String name) {
		throw new UnsupportedOperationException("Unimplemented method 'containsBean'");
	}

	@Override
	public boolean isSingleton(String name) throws NoSuchBeanDefinitionException {
		throw new UnsupportedOperationException("Unimplemented method 'isSingleton'");
	}

	@Override
	public boolean isPrototype(String name) throws NoSuchBeanDefinitionException {
		throw new UnsupportedOperationException("Unimplemented method 'isPrototype'");
	}

	@Override
	public boolean isTypeMatch(String name, ResolvableType typeToMatch) throws NoSuchBeanDefinitionException {
		throw new UnsupportedOperationException("Unimplemented method 'isTypeMatch'");
	}

	@Override
	public boolean isTypeMatch(String name, Class<?> typeToMatch) throws NoSuchBeanDefinitionException {
		throw new UnsupportedOperationException("Unimplemented method 'isTypeMatch'");
	}

	@Override
	public Class<?> getType(String name) throws NoSuchBeanDefinitionException {
		throw new UnsupportedOperationException("Unimplemented method 'getType'");
	}

	@Override
	public Class<?> getType(String name, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
		throw new UnsupportedOperationException("Unimplemented method 'getType'");
	}

	@Override
	public String[] getAliases(String name) {
		throw new UnsupportedOperationException("Unimplemented method 'getAliases'");
	}

	@Override
	public BeanFactory getParentBeanFactory() {
		throw new UnsupportedOperationException("Unimplemented method 'getParentBeanFactory'");
	}

	@Override
	public boolean containsLocalBean(String name) {
		throw new UnsupportedOperationException("Unimplemented method 'containsLocalBean'");
	}

	@Override
	public String getMessage(String code, Object[] args, String defaultMessage, Locale locale) {
		throw new UnsupportedOperationException("Unimplemented method 'getMessage'");
	}

	@Override
	public String getMessage(String code, Object[] args, Locale locale) throws NoSuchMessageException {
		throw new UnsupportedOperationException("Unimplemented method 'getMessage'");
	}

	@Override
	public String getMessage(MessageSourceResolvable resolvable, Locale locale) throws NoSuchMessageException {
		throw new UnsupportedOperationException("Unimplemented method 'getMessage'");
	}

	@Override
	public void publishEvent(Object event) {
		throw new UnsupportedOperationException("Unimplemented method 'publishEvent'");
	}

	@Override
	public Resource[] getResources(String locationPattern) throws IOException {
		throw new UnsupportedOperationException("Unimplemented method 'getResources'");
	}

	@Override
	public Resource getResource(String location) {
		throw new UnsupportedOperationException("Unimplemented method 'getResource'");
	}

	@Override
	public ClassLoader getClassLoader() {
		throw new UnsupportedOperationException("Unimplemented method 'getClassLoader'");
	}

	@Override
	public String getId() {
		throw new UnsupportedOperationException("Unimplemented method 'getId'");
	}

	@Override
	public String getApplicationName() {
		throw new UnsupportedOperationException("Unimplemented method 'getApplicationName'");
	}

	@Override
	public String getDisplayName() {
		throw new UnsupportedOperationException("Unimplemented method 'getDisplayName'");
	}

	@Override
	public long getStartupDate() {
		throw new UnsupportedOperationException("Unimplemented method 'getStartupDate'");
	}

	@Override
	public ApplicationContext getParent() {
		throw new UnsupportedOperationException("Unimplemented method 'getParent'");
	}

	@Override
	public AutowireCapableBeanFactory getAutowireCapableBeanFactory() throws IllegalStateException {
		throw new UnsupportedOperationException("Unimplemented method 'getAutowireCapableBeanFactory'");
	}

}
