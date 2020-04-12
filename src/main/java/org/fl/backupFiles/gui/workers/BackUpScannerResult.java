package org.fl.backupFiles.gui.workers;

import java.util.ArrayList;

import org.fl.backupFiles.scanner.BackUpScannerTask;

public class BackUpScannerResult {

	private final ArrayList<BackUpScannerTask> taskResults ;
	private final long 						   duration ;
	
	public BackUpScannerResult(ArrayList<BackUpScannerTask> tr, long d) {
		
		super();
		taskResults = tr;
		duration = d;
	}

	public ArrayList<BackUpScannerTask> getTaskResults() {
		return taskResults;
	}

	public long getDuration() {
		return duration;
	}
	
}
