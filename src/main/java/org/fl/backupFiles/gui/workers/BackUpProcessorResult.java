package org.fl.backupFiles.gui.workers;

public class BackUpProcessorResult {

	private final long 	  duration ;
	private final boolean success ;

	public BackUpProcessorResult(boolean s, long d) {
		super();
		success  = s ;
		duration = d ;
	}

	public boolean isSuccessfull() { return success;  } 
	public long    getDuration()   { return duration; }
}
