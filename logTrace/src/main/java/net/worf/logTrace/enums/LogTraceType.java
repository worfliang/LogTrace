package net.worf.logTrace.enums;

import net.worf.logTrace.writter.ITraceWritter;
import net.worf.logTrace.writter.LoggerWritter;
import net.worf.logTrace.writter.SystemOutWritter;

public enum LogTraceType {

	NORMAL("",false,new SystemOutWritter()),
	TRANSACTION_TRACE("transaction",true,new LoggerWritter(""));
	
	private String traceName;
	private boolean usingTransKey;
	private ITraceWritter traceWritter;
	
	private LogTraceType(String traceName,boolean usingTransKey,ITraceWritter traceWritter){
		this.traceName = traceName;
		this.usingTransKey = usingTransKey;
		this.traceWritter = traceWritter;
	}

	public String getTraceName() {
		return traceName;
	}

	public boolean isUsingTransKey() {
		return usingTransKey;
	}
	
	public ITraceWritter getTraceWritter() {
		return traceWritter;
	}
	
}
