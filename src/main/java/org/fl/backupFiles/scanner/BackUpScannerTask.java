package org.fl.backupFiles.scanner;

import java.util.concurrent.CompletableFuture;

public class BackUpScannerTask {

	private final BackUpScannerThread 					   backUpScannerThread ;
	private final CompletableFuture<ScannerThreadResponse> futureResponse ;
	private boolean 									   resultRecorded ;
	
	public BackUpScannerTask(BackUpScannerThread bst, CompletableFuture<ScannerThreadResponse> fr) {
		backUpScannerThread = bst ;
		futureResponse		= fr ;
	}

	public BackUpScannerThread getBackUpScannerThread() {
		return backUpScannerThread;
	}

	public CompletableFuture<ScannerThreadResponse> getFutureResponse() {
		return futureResponse;
	}

	public boolean isResultRecorded() {
		return resultRecorded;
	}

	public void setResultRecorded(boolean resultRecorded) {
		this.resultRecorded = resultRecorded;
	}
}
