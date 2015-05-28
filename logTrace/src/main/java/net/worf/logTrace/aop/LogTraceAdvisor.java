package net.worf.logTrace.aop;

import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import net.worf.logTrace.annotation.LogTraceAnnotation;
import net.worf.logTrace.enums.LogTraceType;
import net.worf.logTrace.writter.ITraceWritter;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
public class LogTraceAdvisor {


	@Before("logTraceAnnotation()")
	public void beforeMethodAdvice(JoinPoint joinPoint){
		   MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		   String methodName = methodSignature.getName();
		   LogTraceAnnotation logTraceAnnotation = getLogTraceAnnotation(joinPoint);
		   String traceLable = createTraceLabel(logTraceAnnotation); 
		   ITraceWritter loggerWritter = logTraceAnnotation.logTraceType().getTraceWritter();
		   loggerWritter.writeMessage(traceLable+" Before method  "+methodName);
		   StringBuilder sessionAttributeStringBuilder = new StringBuilder("log "+methodName+" sessionAttribute ");
		   String[] sessionAttributes = logTraceAnnotation.sessionAttribute();
		   if(sessionAttributes.length >0){
			   for(String sessionAttribute:sessionAttributes){
				   logSessionAttributeInfo(sessionAttribute,sessionAttributeStringBuilder);
			   }
		   }
		   loggerWritter.writeMessage(traceLable+" "+sessionAttributeStringBuilder.toString());
		   String[] logMethodArgumentNames = logTraceAnnotation.logMethodArgumentNames();
		   StringBuilder argumentStringBuilder = new StringBuilder("log "+methodName+" argument ");
		   boolean isLogAllmethodArgs = logTraceAnnotation.logAllmethodArgs();
		   String[] argumentNames = methodSignature.getParameterNames();
		   Object[] argumentValue = joinPoint.getArgs();
		   int argLength = argumentNames.length;
		   if(isLogAllmethodArgs){
				   for(int i=0;i<argLength;i++){
						   logArgumentInfo(argumentNames[i],argumentValue[i],argumentStringBuilder);
				   }   
		   }else{
			   if(logMethodArgumentNames.length >0){
				   boolean includeArgument = logTraceAnnotation.includeArgument(); 
				   if(includeArgument){
					   logArgumentUseIncludeStrategy(logMethodArgumentNames, argumentNames, argumentValue, argumentStringBuilder);
				   }else{
					   logArgumentUseExcludeStrategy(logMethodArgumentNames, argumentNames, argumentValue, argumentStringBuilder);
				   }
			   }
		   }
		   loggerWritter.writeMessage(traceLable+" "+argumentStringBuilder.toString());
	}
	
	private void logArgumentUseIncludeStrategy(String[] logMethodArgumentNames,String[] argumentNames,Object[] argumentValue,StringBuilder argumentStringBuilder){
		int logMethodArgumentsLength = logMethodArgumentNames.length;
		int argLength = argumentNames.length;
		 for(int i=0;i<logMethodArgumentsLength;i++){
			   for(int j=0;j<argLength;j++){
				   if(logMethodArgumentNames[i].equals(argumentNames[j])){
					   logArgumentInfo(argumentNames[j],argumentValue[j],argumentStringBuilder);
					   break;
				   }
			   }   
		   }   
	}
	
    private void  logArgumentUseExcludeStrategy(String[] logMethodArgumentNames,String[] argumentNames,Object[] argumentValue,StringBuilder argumentStringBuilder){
	    int logMethodArgumentsLength = logMethodArgumentNames.length;
	    int argLength = argumentNames.length;
	     for(int i=0;i<argLength;i++){
		   boolean needLog = true;
		   for(int j=0;j<logMethodArgumentsLength;j++){
			   if(logMethodArgumentNames[j].equals(argumentNames[i])){
				   needLog = false;
			   }
		   }
		   if(needLog){
			   logArgumentInfo(argumentNames[i],argumentValue[i],argumentStringBuilder);
		   }
	   }   
	}
	
	@AfterReturning(value="logTraceAnnotation()",returning="retVal")
	public void afterReturnAdvice(JoinPoint joinPoint,Object retVal){
		LogTraceAnnotation logTraceAnnotation = getLogTraceAnnotation(joinPoint);
		String traceLabel = getTraceLabel(logTraceAnnotation);
		ITraceWritter loggerWritter = logTraceAnnotation.logTraceType().getTraceWritter();
		if(retVal != null){
			loggerWritter.writeMessage(traceLabel+" AfterReturning method  "+joinPoint.getSignature().getName() +" return  "+retVal);	
		}else{
			loggerWritter.writeMessage(traceLabel+" AfterReturning method  "+joinPoint.getSignature().getName() +" return is null or void ! ");
		}
	}

	@After("logTraceAnnotation()")
	public void afterMethodAdvice(JoinPoint joinPoint) {
		LogTraceAnnotation logTraceAnnotation = getLogTraceAnnotation(joinPoint);
		ITraceWritter loggerWritter = logTraceAnnotation.logTraceType().getTraceWritter();
		loggerWritter.writeMessage(getTraceLabel(logTraceAnnotation)+" After method  "+joinPoint.getSignature().getName());

	}
	
	@Pointcut("@annotation(com.feec.ec.aop.log.LogTraceAnnotation) || @within(com.feec.ec.aop.log.LogTraceAnnotation)")
	public void logTraceAnnotation(){};
	
	private LogTraceAnnotation getLogTraceAnnotation(JoinPoint joinPoint){
		MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
		LogTraceAnnotation  logTraceAnnotation= methodSignature.getMethod().getAnnotation(LogTraceAnnotation.class);
		if(logTraceAnnotation == null){
			logTraceAnnotation = joinPoint.getTarget().getClass().getAnnotation(LogTraceAnnotation.class);
		}
		return logTraceAnnotation;
	}
	
	
	
	private String getTraceLabel(LogTraceAnnotation logTraceAnnotation){
		String processName = StringUtils.isEmpty(logTraceAnnotation.processName())?"":logTraceAnnotation.processName();
		String traceName = logTraceAnnotation.logTraceType().getTraceName();
		String transKey = ObjectUtils.defaultIfNull(getAttributeValue(traceName),"").toString();
		return String.format("%s %s %s ",traceName,transKey,processName);
	}
	
	private String  createTraceLabel(LogTraceAnnotation logTraceAnnotation){
		String processName = StringUtils.isEmpty(logTraceAnnotation.processName())?"":logTraceAnnotation.processName();
		String traceLable = logTraceAnnotation.logTraceType().getTraceName();
		String transKey = "";
		if(logTraceAnnotation.logTraceType() != LogTraceType.NORMAL && logTraceAnnotation.logTraceType().isUsingTransKey()){
			  transKey = getTraceKey(traceLable,logTraceAnnotation.isTransBegin());   
		 }
		return String.format("%s %s %s ",traceLable,transKey,processName);
	}
	
	private String getTraceKey(String traceName,boolean isTransBegin){
		String transKey = "";
		if(isTransBegin){
			removeAttribute(traceName);
		   transKey = generatetUniTransKey();
		   setAttribute(traceName,transKey); 
		}else{
			Object keyInSesssion = getAttributeValue(traceName);
			if(keyInSesssion != null){
				transKey = keyInSesssion.toString();
			}
		}
		return transKey;
	}
	
	private String generatetUniTransKey(){
		return UUID.randomUUID().toString().replace("-","");
	}
	
	private StringBuilder logArgumentInfo(String argumentName,Object argumentValue,StringBuilder sb){
		  sb.append(" ").append(argumentName).append(" ")
		    .append(ObjectUtils.defaultIfNull(argumentValue,"").toString()).append(" ");
		  return sb;
	}
	
	
	private StringBuilder logSessionAttributeInfo(String sessionAttribute,StringBuilder sb){
			  sb.append(" ").append(sessionAttribute).append(" ")
			    .append(ObjectUtils.defaultIfNull(getAttributeValue(sessionAttribute),"")).append(" ");
		  return sb;
	}
	
	private Object getAttributeValue(String name){
		if(getSession()!= null){
			return getSession().getAttribute(name);	
		}else{
			return null;
		}
	}
	
	private void removeAttribute(String name){
		if(getSession()!= null){
		    getSession().removeAttribute(name);
		}
	}
	
	private void setAttribute(String name,Object value){
		if(getSession()!= null){
			getSession().setAttribute(name, value);	
		}
	}
	
	private HttpSession getSession(){
		RequestAttributes requestAttrs=RequestContextHolder.currentRequestAttributes();
		if(requestAttrs == null)return null;
		HttpServletRequest request=((ServletRequestAttributes)requestAttrs).getRequest();
		return request.getSession();
	}
	

}
