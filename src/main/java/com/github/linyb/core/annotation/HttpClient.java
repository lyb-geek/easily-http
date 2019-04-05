package com.github.linyb.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.linyb.core.enu.HttpMethod;

/**
 * 
 * <p>
 * Title:HttpClient
 * </p>
 * <p>
 * Description: 通过该注解配置远程http调用地址，GET或者POST请求
 * </p>
 * 
 * @author linyb
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface HttpClient {

	String url();

	HttpMethod HTTP_METHOD() default HttpMethod.GET;

}
