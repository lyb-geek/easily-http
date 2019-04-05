package com.github.linyb.core.spring.bean;

import org.springframework.beans.factory.FactoryBean;

import com.github.linyb.core.util.HttpClientServiceUtil;

public class HttpClientFactroyBean<T> implements FactoryBean<T> {

	public static final String field_httpClientServiceBeanClz = "httpClientServiceBeanClz";

	private Class<T> httpClientServiceBeanClz;

	public Class<T> getHttpClientServiceBeanClz() {
		return httpClientServiceBeanClz;
	}

	public void setHttpClientServiceBeanClz(Class<T> httpClientServiceBeanClz) {
		this.httpClientServiceBeanClz = httpClientServiceBeanClz;
	}

	@Override
	public T getObject() throws Exception {

		T t = HttpClientServiceUtil.getInstance().getService(httpClientServiceBeanClz);

		return t;
	}

	@Override
	public Class<?> getObjectType() {
		return httpClientServiceBeanClz;
	}

}
