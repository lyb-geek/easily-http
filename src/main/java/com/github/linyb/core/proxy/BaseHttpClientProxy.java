package com.github.linyb.core.proxy;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.linyb.common.util.MapUtil;
import com.github.linyb.common.util.StringUtil;
import com.github.linyb.core.annotation.HttpClient;
import com.github.linyb.core.annotation.HttpReqBean;
import com.github.linyb.core.annotation.HttpReqListParam;
import com.github.linyb.core.annotation.HttpReqMethod;
import com.github.linyb.core.annotation.HttpReqParam;
import com.github.linyb.core.enu.HttpMethod;
import com.github.linyb.core.enu.MimeType;
import com.github.linyb.core.util.HttpClientUtil;
import com.github.linyb.exception.EasyHttpException;

public class BaseHttpClientProxy {

	private static final String JSON_OBJ_PREFIX = "{";

	private static final String JSON_ARRAY_PREFIX = "[{";

	private static final String HTTP_LIST_REQUEST_PARAMS = "listParams";

	private static Logger logger = LoggerFactory.getLogger(BaseHttpClientProxy.class);

	protected HttpMethod httpMethod;

	protected String url;

	public HttpMethod getHttpMethod() {
		return httpMethod;
	}

	public void setHttpMethod(HttpMethod httpMethod) {
		this.httpMethod = httpMethod;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Object doService(Object proxy, Method method, Object[] args) throws Throwable {
		setHttpInfoByHttpClient(proxy);

		boolean capwordsRequired = setHttpInfoByHttpReqMethodReturnCapwordsRequired(method);

		if (StringUtil.isBlank(url)) {
			throw new EasyHttpException("URL is required");
		}

		if (httpMethod == null) {
			throw new EasyHttpException("httpMethod is required");
		}

		Map<String, String> params = new HashMap<>();
		boolean isHttpListBeanParams = false;
		if (args != null && args.length > 0) {
			int index = 0;
			boolean isHttpReqBean = false;
			boolean isHttpReqParam = false;
			for (Annotation[] parameterAnnotation : method.getParameterAnnotations()) {

				for (Annotation annotation : parameterAnnotation) {
					if (annotation instanceof HttpReqBean) {
						Map<String, String> httpReqBeanMap;
						isHttpReqBean = true;
						if (capwordsRequired) {
							httpReqBeanMap = MapUtil.getCapwordsKeyMap(args[index]);
						} else {
							httpReqBeanMap = MapUtil.convertBean2Map(args[index]);
						}
						params.putAll(httpReqBeanMap);
					} else if (annotation instanceof HttpReqParam) {
						HttpReqParam httpReqParam = (HttpReqParam) annotation;
						isHttpReqParam = true;
						String paramKey = httpReqParam.value();
						if (capwordsRequired) {
							paramKey = StringUtil.capitalize(paramKey.toLowerCase());
						}
						params.put(paramKey, args[index].toString());
					} else if (annotation instanceof HttpReqListParam) {
						isHttpListBeanParams = true;
						String listParamsJSONValue = JSON.toJSONString(args[index],
								SerializerFeature.DisableCircularReferenceDetect);
						params.put(HTTP_LIST_REQUEST_PARAMS, listParamsJSONValue);
					}
				}
				index++;
			}

			validateHttpMethodAnnation(isHttpListBeanParams, isHttpReqBean, isHttpReqParam);

		}

		if (params.isEmpty() && (args != null && args.length > 0)) {
			throw new EasyHttpException("HttpReqBean or HttpReqParam  is required");
		}

		if (isHttpListBeanParams) {
			return doPostJSON(method, params);
		}

		Object result = null;
		switch (httpMethod) {
		case GET:
			result = doGet(method, params);
			break;
		case POST:
			String contentType = method.getAnnotation(HttpReqMethod.class).contentType().name();
			if (MimeType.APPLICATION_JSON.name().equalsIgnoreCase(contentType)) {
				result = doPostJSON(method, params);
			} else {
				result = doPost(method, params);
			}
			break;

		default:
			result = doGet(method, params);
			break;
		}

		return result;
	}

	/**
	 *
	 * HttpReqListParam 和 HttpReqBean，HttpReqParam不能同时使用
	 * 
	 * @param isHttpListParams
	 * @param isHttpReqBean
	 * @param isHttpReqParam
	 */
	private void validateHttpMethodAnnation(boolean isHttpListParams, boolean isHttpReqBean, boolean isHttpReqParam) {

		if (isHttpReqBean && isHttpListParams) {
			throw new EasyHttpException("HttpReqBean ,HttpReqListParam can not be used at the same time !");
		}

		if (isHttpReqParam && isHttpListParams) {
			throw new EasyHttpException("HttpReqParam ,HttpReqListParam can not be used at the same time !");
		}

	}

	/**
	 * 根据HttpReqMethod注解再次封装url和httpMethod信息,并返回是否首字母大小写
	 * 
	 * @param method
	 */
	private boolean setHttpInfoByHttpReqMethodReturnCapwordsRequired(Method method) {
		HttpReqMethod httpReqMethod = method.getAnnotation(HttpReqMethod.class);

		if (httpReqMethod != null) {
			String path = httpReqMethod.path();
			if (StringUtil.isNotBlank(path)) {
				url = url + path;
			}

			httpMethod = httpReqMethod.HTTP_METHOD();

			return httpReqMethod.capwordsRequired();
		}
		return false;
	}

	/**
	 * 根据httpclient注解获取url和httpmethod
	 * 
	 * @param proxy
	 * @throws ClassNotFoundException
	 */
	private void setHttpInfoByHttpClient(Object proxy) throws ClassNotFoundException {
		String className = proxy.getClass().getGenericInterfaces()[0].getTypeName();
		Class targetClz = Class.forName(className);
		HttpClient httpClient = null;
		boolean hasHttpclientAnnotation = targetClz.isAnnotationPresent(HttpClient.class);
		if (hasHttpclientAnnotation) {
			httpClient = (HttpClient) targetClz.getAnnotation(HttpClient.class);
		}

		if (httpClient != null) {
			String httpUrl = httpClient.url();
			if (StringUtil.isNotBlank(httpUrl)) {
				url = httpUrl;
			}

			httpMethod = httpClient.HTTP_METHOD();
		}
	}

	private Object doGet(Method method, Map<String, String> param) {
		try {
			String jsonResult = HttpClientUtil.doGet(url, param);
			Object result = getResult(method, jsonResult);

			return result;
		} catch (Exception e) {
			logger.error(method.getName() + " error:" + e.getMessage(), e);
		}

		return null;

	}

	private Object getResult(Method method, String jsonResult) {
		Object result = null;
		HttpReqMethod httpReqMethod = method.getAnnotation(HttpReqMethod.class);
		Class[] expectReturnTypes = httpReqMethod.expectReturnType();
		Class returnType = method.getReturnType();
		if (expectReturnTypes != null && expectReturnTypes.length > 0) {
			returnType = expectReturnTypes[0];
		}
		if (jsonResult.startsWith(JSON_OBJ_PREFIX)) {
			result = JSON.parseObject(jsonResult, returnType);
		} else if (jsonResult.startsWith(JSON_ARRAY_PREFIX)) {
			try {
				result = JSON.parseArray(jsonResult, returnType);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			result = getNotJsonResult(method, jsonResult);
		}
		return result;
	}

	private Object doPost(Method method, Map<String, String> param) {
		try {
			String jsonResult = HttpClientUtil.doPost(url, param);
			Object result = getResult(method, jsonResult);
			return result;
		} catch (Exception e) {
			logger.error(method.getName() + " error:" + e.getMessage(), e);
		}

		return null;

	}

	private Object doPostJSON(Method method, Map<String, String> param) {
		try {
			String listParamsJSONValue = param.get(HTTP_LIST_REQUEST_PARAMS);
			String jsonReqParam;
			if (StringUtil.isNotBlank(listParamsJSONValue)) {
				jsonReqParam = listParamsJSONValue;
			} else {
				jsonReqParam = JSON.toJSONString(param);
			}

			String jsonResult = HttpClientUtil.doPostJson(url, jsonReqParam);
			Object result = getResult(method, jsonResult);
			return result;
		} catch (Exception e) {
			logger.error(method.getName() + " error:" + e.getMessage(), e);
		}

		return null;

	}

	private Object doPostList(Method method, Map<String, Object> param) {
		try {
			String jsonReqParam = JSON.toJSONString(param);
			String jsonResult = HttpClientUtil.doPostJson(url, jsonReqParam);
			Object result = getResult(method, jsonResult);
			return result;
		} catch (Exception e) {
			logger.error(method.getName() + " error:" + e.getMessage(), e);
		}

		return null;

	}

	private Object getNotJsonResult(Method method, String jsonResult) {
		if (StringUtil.isBlank(jsonResult)) {
			return jsonResult;
		}

		Object returnType = method.getReturnType();
		String returnTypeStr = ((Class) returnType).getSimpleName();
		if (returnTypeStr.equalsIgnoreCase("Boolean")) {
			return Boolean.valueOf(jsonResult);
		} else if (returnTypeStr.equalsIgnoreCase("Integer") || returnTypeStr.equalsIgnoreCase("int")) {
			return Integer.valueOf(jsonResult);
		} else if (returnTypeStr.equalsIgnoreCase("Byte")) {
			return new Byte(jsonResult);
		} else if (returnTypeStr.equalsIgnoreCase("Short")) {
			return Short.valueOf(jsonResult);
		} else if (returnTypeStr.equalsIgnoreCase("Long")) {
			return Long.valueOf(jsonResult);
		} else if (returnTypeStr.equalsIgnoreCase("Float")) {
			return Float.valueOf(jsonResult);
		} else if (returnTypeStr.equalsIgnoreCase("Double")) {
			return Double.valueOf(jsonResult);
		} else if (returnTypeStr.equalsIgnoreCase("Boolean[]")) {
			String[] returnArr = covertString2Arrays(jsonResult);
			Boolean[] results = new Boolean[returnArr.length];
			for (int i = 0; i < returnArr.length; i++) {
				results[i] = Boolean.valueOf(returnArr[i]);
			}

			return results;

		} else if (returnTypeStr.equalsIgnoreCase("Integer[]") || returnTypeStr.equalsIgnoreCase("int[]")) {
			String[] returnArr = covertString2Arrays(jsonResult);
			Integer[] results = new Integer[returnArr.length];
			for (int i = 0; i < returnArr.length; i++) {
				results[i] = Integer.valueOf(returnArr[i]);
			}
			return results;
		} else if (returnTypeStr.equalsIgnoreCase("Byte[]")) {
			String[] returnArr = covertString2Arrays(jsonResult);
			Byte[] results = new Byte[returnArr.length];
			for (int i = 0; i < returnArr.length; i++) {
				results[i] = new Byte(returnArr[i]);
			}
			return results;
		} else if (returnTypeStr.equalsIgnoreCase("Short[]")) {
			String[] returnArr = covertString2Arrays(jsonResult);
			Short[] results = new Short[returnArr.length];
			for (int i = 0; i < returnArr.length; i++) {
				results[i] = Short.valueOf(returnArr[i]);
			}
			return results;
		} else if (returnTypeStr.equalsIgnoreCase("Long[]")) {
			String[] returnArr = covertString2Arrays(jsonResult);
			Long[] results = new Long[returnArr.length];
			for (int i = 0; i < returnArr.length; i++) {
				results[i] = Long.valueOf(returnArr[i]);
			}
			return results;
		} else if (returnTypeStr.equalsIgnoreCase("Float[]")) {
			String[] returnArr = covertString2Arrays(jsonResult);
			Float[] results = new Float[returnArr.length];
			for (int i = 0; i < returnArr.length; i++) {
				results[i] = Float.valueOf(returnArr[i]);
			}
			return results;
		} else if (returnTypeStr.equalsIgnoreCase("Double[]")) {
			String[] returnArr = covertString2Arrays(jsonResult);
			Double[] results = new Double[returnArr.length];
			for (int i = 0; i < returnArr.length; i++) {
				results[i] = Double.valueOf(returnArr[i]);
			}
			return results;
		} else if (returnTypeStr.equalsIgnoreCase("String[]")) {
			String[] returnArr = covertString2Arrays(jsonResult);

			return returnArr;
		}
		return jsonResult;
	}

	/**
	 * 字符串装数组
	 * 
	 * @return
	 */
	private static String[] covertString2Arrays(String value) {
		value = StringUtil.substring(value, StringUtil.indexOf(value, "[") + 1, StringUtil.lastIndexOf(value, "]"));
		return StringUtil.split(value, ",");
	}

}
