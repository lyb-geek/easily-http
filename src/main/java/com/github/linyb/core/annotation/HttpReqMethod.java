package com.github.linyb.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.github.linyb.core.enu.HttpMethod;
import com.github.linyb.core.enu.MimeType;

@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface HttpReqMethod {

	String path();

	HttpMethod HTTP_METHOD() default HttpMethod.GET;

	boolean capwordsRequired() default false;

	MimeType contentType() default MimeType.APPLICATION_FORM;

	Class<?>[] expectReturnType() default {};

}
