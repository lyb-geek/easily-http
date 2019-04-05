package com.github.linyb.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.linyb.core.proxy.HttpClientCglibProxy;
import com.github.linyb.core.proxy.HttpClientProxy;

public class HttpClientServiceUtil {
	private static Logger logger = LoggerFactory.getLogger(HttpClientServiceUtil.class);

	private HttpClientServiceUtil() {

	}

	public static HttpClientServiceUtil getInstance() {

		return Singleton.INSTANCE.getInstance();
	}

	private static enum Singleton {
		INSTANCE;
		private HttpClientServiceUtil client;

		private Singleton() {
			client = new HttpClientServiceUtil();
		}

		public HttpClientServiceUtil getInstance() {
			return client;
		}

	}

	public <T> T getService(Class<T> clz) {
		return (T) HttpClientCglibProxy.newInstance(clz);
	}

	public <T> T getService(Class<T>[] interfaces) {
		return (T) HttpClientProxy.newInstance(interfaces);
	}

}