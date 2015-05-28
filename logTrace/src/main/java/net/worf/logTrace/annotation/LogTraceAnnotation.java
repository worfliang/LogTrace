package net.worf.logTrace.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.worf.logTrace.enums.LogTraceType;

@Target({ElementType.METHOD,ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface LogTraceAnnotation {
	
	String[] sessionAttribute() default {};
	
	String[] logMethodArgumentNames() default {};
	
	boolean  logAllmethodArgs() default false;
	
	LogTraceType logTraceType() default LogTraceType.NORMAL;
	
	boolean  isTransBegin() default false;
	
	boolean  includeArgument() default true;
	
	String   processName() default "";
}
