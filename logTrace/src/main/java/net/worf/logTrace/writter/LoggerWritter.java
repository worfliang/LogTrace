package net.worf.logTrace.writter;

import org.apache.log4j.Logger;


public class LoggerWritter implements ITraceWritter {
	
	private String loggerName;
	
	public LoggerWritter(String loggerName) {
		this.loggerName = loggerName;
	}

	public String getLoggerName() {
		return loggerName;
	}

	public void setLoggerName(String loggerName) {
		this.loggerName = loggerName;
	}

	@Override
	public void writeMessage(String message) {
		Logger.getLogger(getLoggerName()).info(message);
	}
	
	

}
