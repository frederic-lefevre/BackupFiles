package org.fl.backupFiles.scanner;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpTask;
import org.fl.backupFiles.TestUtils;
import org.junit.jupiter.api.Test;

class BackUpScannerThreadTest {

	@Test
	void test() {
		
		Logger log = Logger.getGlobal() ;
		ExecutorService scannerExecutor = Executors.newFixedThreadPool(5) ;
		
		final String SRC_FILE1 =  "file:///ForTests/BackUpFiles/FP_Test_Buffer" ;
		final String TGT_FILE1 =  "file:///ForTests/BackUpFiles/FP_Test_Target" ;
		
		Path src  = TestUtils.getPathFromUriString(SRC_FILE1) ;
		Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;
		
		BackUpTask backUpTask = new BackUpTask(src, tgt, log) ;
		
		BackUpScannerThread backUpScannerThread = new BackUpScannerThread(backUpTask, log) ;
		Future<ScannerThreadResponse> backUpRes = scannerExecutor.submit(backUpScannerThread) ;
		
		while (! backUpRes.isDone()) {
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				log.log(Level.SEVERE, "Interruption exception in BackUpScannerThread test", e);
				fail("Interrupted Exception");
			}
		}
		try {
			ScannerThreadResponse scannerResp = backUpRes.get() ;
			BackUpItemList backUpItemList = scannerResp.getBackUpItemList() ;
			
			assertNotNull(backUpItemList) ;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Exception in BackUpScannerThread test", e);
			fail("Exception");
		}
		
	}

}
