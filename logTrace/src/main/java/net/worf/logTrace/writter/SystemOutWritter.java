package net.worf.logTrace.writter;


public class SystemOutWritter implements ITraceWritter {

	@Override
	public void writeMessage(String message) {
		 System.out.println(message);
	}

}
