package org.fl.backupFiles.gui.workers;

import java.util.List;

import org.fl.backupFiles.scanner.ScannerThreadResponse;

public class BackUpScannerResult {

	private final List<ScannerThreadResponse> taskResults ;
	private final long 					  	  duration ;
	
	public BackUpScannerResult(List<ScannerThreadResponse> tr, long d) {	
		super();
		taskResults = tr;
		duration = d;
	}

	public List<ScannerThreadResponse> getTaskResults() {
		return taskResults;
	}

	public long getDuration() {
		return duration;
	}
	
}
