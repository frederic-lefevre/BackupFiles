package org.fl.backupFiles.scanner;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpCounters;
import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpTask;
import org.fl.backupFiles.Config;
import org.fl.backupFiles.TestUtils;
import org.junit.jupiter.api.Test;

import com.ibm.lge.fl.util.RunningContext;
import com.ibm.lge.fl.util.file.FilesUtils;

class BackUpScannerProcessorTest {

	private static final String DEFAULT_PROP_FILE = "file:///ForTests/BackUpFiles/backupFiles.properties";
	
	@Test
	void test() {
		
		Logger log = Logger.getGlobal() ;
		try {
					
			RunningContext runningContext = new RunningContext("BackupFilesTest", null, new URI(DEFAULT_PROP_FILE));
			Config.initConfig(runningContext.getProps());
			ExecutorService scannerExecutor = Config.getScanExecutorService() ;
			
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
					log.log(Level.SEVERE, "Interruption exception in BackUpScannerThread test", e);
					fail("Interrupted Exception");
				}
			}
		
			ScannerThreadResponse scannerResp = backUpRes.get() ;
						
			BackUpCounters backUpCounters = scannerResp.getBackUpCounters() ;			
			assertEquals(0, backUpCounters.ambiguousNb) ;
			assertEquals(0, backUpCounters.contentDifferentNb) ;
			assertEquals(0, backUpCounters.copyNewNb) ;
			assertEquals(0, backUpCounters.copyReplaceNb) ;
			assertEquals(2, backUpCounters.copyTreeNb) ;
			assertEquals(0, backUpCounters.deleteDirNb) ;
			assertEquals(0, backUpCounters.deleteNb) ;
			assertEquals(0, backUpCounters.nbSourceFilesFailed) ;
			assertEquals(3, backUpCounters.nbSourceFilesProcessed) ;
			assertEquals(0, backUpCounters.nbTargetFilesFailed) ;
			assertEquals(0, backUpCounters.nbTargetFilesProcessed) ;
			
			BackUpItemList backUpItemList = scannerResp.getBackUpItemList() ;
			assertNotNull(backUpItemList) ;
			assertEquals(2, backUpItemList.size()) ;
			
			// Execute backup
			backUpCounters.reset() ;
			for (BackUpItem backUpItem : backUpItemList) {
				backUpItem.execute(backUpCounters);
			}
			assertEquals(0, backUpCounters.ambiguousNb) ;
			assertEquals(0, backUpCounters.contentDifferentNb) ;
			assertEquals(0, backUpCounters.copyNewNb) ;
			assertEquals(0, backUpCounters.copyReplaceNb) ;
			assertEquals(2, backUpCounters.copyTreeNb) ;
			assertEquals(0, backUpCounters.deleteDirNb) ;
			assertEquals(0, backUpCounters.deleteNb) ;
			assertEquals(0, backUpCounters.nbSourceFilesFailed) ;
			assertEquals(2, backUpCounters.nbSourceFilesProcessed) ;
			assertEquals(0, backUpCounters.nbTargetFilesFailed) ;
			assertEquals(0, backUpCounters.nbTargetFilesProcessed) ;
			
			// Recompare directory
			backUpTask.setCompareContent(true) ;
			backUpScannerThread = new BackUpScannerThread(backUpTask, log) ;
			backUpRes = scannerExecutor.submit(backUpScannerThread) ;
			
			while (! backUpRes.isDone()) {
				try {
					Thread.sleep(300);
				} catch (InterruptedException e) {
					log.log(Level.SEVERE, "Interruption exception in BackUpScannerThread test", e);
					fail("Interrupted Exception");
				}
			}
			
			scannerResp = backUpRes.get() ;
			backUpItemList = scannerResp.getBackUpItemList() ;
			
			assertNotNull(backUpItemList) ;
			assertEquals(0, backUpItemList.size()) ;
			
			// Delete target dir to recover initial state
			FilesUtils.deleteDirectoryTree(tgt, false, log) ;
			Files.createDirectory(tgt) ;
			
		} catch (Exception e) {
			log.log(Level.SEVERE, "Exception in BackUpScannerProcessor test", e);
			fail("Exception");
		}
		
	}

}
