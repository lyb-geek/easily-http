package com.github.linyb.core.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.context.annotation.Import;

import com.github.linyb.core.spring.registrar.HttpClientScannerRegistrar;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(HttpClientScannerRegistrar.class)
public @interface EnableHttpClients {

	String[] basePackages() default {};

	Class<?>[] basePackageClasses() default {};

}
