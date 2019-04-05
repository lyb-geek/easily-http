package com.github.linyb.core.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.linyb.core.enu.HttpMethod;

public class HttpClientProxy extends BaseHttpClientProxy implements InvocationHandler {
	private static Logger logger = LoggerFactory.getLogger(HttpClientProxy.class);

	public HttpClientProxy(HttpMethod httpMethod, String url) {
		this.httpMethod = httpMethod;
		this.url = url;
	}

	public HttpClientProxy() {
	}

	public static Object newInstance(Class[] interfaces, HttpMethod httpMethod, String url) {
		return Proxy.newProxyInstance(HttpClientProxy.class.getClassLoader(), interfaces,
				new HttpClientProxy(httpMethod, url));
	}

	public static Object newInstance(Class[] interfaces) {
		Object obj = Proxy.newProxyInstance(HttpClientProxy.class.getClassLoader(), interfaces, new HttpClientProxy());
		return obj;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		return doService(proxy, method, args);
	}

}
