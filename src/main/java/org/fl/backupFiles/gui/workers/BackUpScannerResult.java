package org.fl.backupFiles.gui.workers;

import java.util.List;

import org.fl.backupFiles.scanner.BackUpScannerTask;

public class BackUpScannerResult {

	private final List<BackUpScannerTask> taskResults ;
	private final long 					  duration ;
	
	public BackUpScannerResult(List<BackUpScannerTask> tr, long d) {
		
		super();
		taskResults = tr;
		duration = d;
	}

	public List<BackUpScannerTask> getTaskResults() {
		return taskResults;
	}

	public long getDuration() {
		return duration;
	}
	
}
