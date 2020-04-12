package org.fl.backupFiles.gui.workers;

public class BackUpProcessorResult {

	private final long 	duration ;

	public BackUpProcessorResult(long d) {
		super();
		duration = d;
	}

	public long getDuration() { return duration; }
}
