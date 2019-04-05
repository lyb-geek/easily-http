package com.github.linyb.core.proxy;

import java.lang.reflect.Method;

import com.github.linyb.core.enu.HttpMethod;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

public class HttpClientCglibProxy extends BaseHttpClientProxy implements MethodInterceptor {

	public HttpClientCglibProxy(HttpMethod httpMethod, String url) {
		this.httpMethod = httpMethod;
		this.url = url;
	}

	public HttpClientCglibProxy() {
	}

	public static Object newInstance(Class targetClz, HttpMethod httpMethod, String url) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(targetClz);
		enhancer.setCallback(new HttpClientCglibProxy(httpMethod, url));
		return enhancer.create();
	}

	public static Object newInstance(Class targetClz) {
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(targetClz);
		enhancer.setCallback(new HttpClientCglibProxy());
		return enhancer.create();
	}

	@Override
	public Object intercept(Object proxy, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
		return doService(proxy, method, args);
	}

}
