package org.fl.backupFiles.scanner;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class BackUpScannerTask {

	private final BackUpScannerThread 			backUpScannerThread ;
	private final Future<ScannerThreadResponse> futureResponse ;
	private boolean 							resultRecorded ;
	
	public BackUpScannerTask(BackUpScannerThread bst, CompletableFuture<ScannerThreadResponse> fr) {
		backUpScannerThread = bst ;
		futureResponse		= fr ;
	}

	public BackUpScannerThread getBackUpScannerThread() {
		return backUpScannerThread;
	}

	public Future<ScannerThreadResponse> getFutureResponse() {
		return futureResponse;
	}

	public boolean isResultRecorded() {
		return resultRecorded;
	}

	public void setResultRecorded(boolean resultRecorded) {
		this.resultRecorded = resultRecorded;
	}
}
