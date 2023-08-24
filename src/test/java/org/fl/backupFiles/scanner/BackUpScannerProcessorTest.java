/*
 * MIT License

Copyright (c) 2017, 2023 Frederic Lefevre

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/

package org.fl.backupFiles.scanner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpCounters;
import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpTask;
import org.fl.backupFiles.Config;
import org.fl.backupFiles.TestUtils;
import org.fl.util.file.FilesUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BackUpScannerProcessorTest {

	private static final String DEFAULT_PROP_FILE = "file:///ForTests/BackUpFiles/backupFiles.properties";
	
	private final static String SOURCE_DATA_DIR1 = "file:///C:/FredericPersonnel/tmp" ;
	private final static String SOURCE_DATA_DIR2 = "file:///C:/FredericPersonnel/Loisirs/sports" ;
	
	private final static String BUFFER_DATA_DIR  = "file:///C:/ForTests/BackUpFiles/FP_Test_Buffer/";
	private final static String BUFFER_DATA_DIR1 = BUFFER_DATA_DIR + "dir1/" ;
	private final static String BUFFER_DATA_DIR2 = BUFFER_DATA_DIR + "dir2/" ;
	private final static String TARGET_DATA_DIR  = "file:///C:/ForTests/BackUpFiles/FP_Test_Target/";
	
	private static Logger log ;
	
	@BeforeAll
	static void generateTestData() {
		
		try {
			Config.initConfig(DEFAULT_PROP_FILE);
			log = Config.getLogger();

			// Copy test data
			Path srcPath1 = Paths.get(new URI(SOURCE_DATA_DIR1));
			Path srcPath2 = Paths.get(new URI(SOURCE_DATA_DIR2));
			Path testDataDir1 = Paths.get(new URI(BUFFER_DATA_DIR1));
			Path testDataDir2 = Paths.get(new URI(BUFFER_DATA_DIR2));
			boolean b1 = FilesUtils.copyDirectoryTree(srcPath1, testDataDir1, log);
			boolean b2 = FilesUtils.copyDirectoryTree(srcPath2, testDataDir2, log);
			if (! (b1 && b2)) {
				fail("Errors writing test data (BeforeAll method)");
			}
			Path targetDataDir = Paths.get(new URI(TARGET_DATA_DIR));
			Files.createDirectory(targetDataDir);
			
		} catch (URISyntaxException e) {
			Logger.getGlobal().log(Level.SEVERE, "URI exception writing test data", e);
			fail("URI exception writing test data (BeforeAll method)");
		} catch (IOException e) {
			Logger.getGlobal().log(Level.SEVERE, "IO exception writing test data", e);
			fail("IO exception writing test data (BeforeAll method)");
		}
	}
	
	@Test
	void nominalTest() {
		
		try {
								
			ExecutorService scannerExecutor = Config.getScanExecutorService();
			
			Path src = TestUtils.getPathFromUriString(BUFFER_DATA_DIR);
			Path tgt = TestUtils.getPathFromUriString(TARGET_DATA_DIR);
			
			BackUpTask backUpTask = new BackUpTask(src, tgt);
			
			BackUpScannerThread backUpScannerThread = new BackUpScannerThread(backUpTask);
			CompletableFuture<ScannerThreadResponse> backUpRes = CompletableFuture.supplyAsync(backUpScannerThread::scan, scannerExecutor);
			
			ScannerThreadResponse scannerResp = backUpRes.get();
			BackUpCounters backUpCounters = scannerResp.getBackUpCounters();
			assertThat(backUpCounters.ambiguousNb).isZero();
			assertThat(backUpCounters.copyNewNb).isZero();
			assertThat(backUpCounters.copyReplaceNb).isZero();
			assertThat(backUpCounters.copyTreeNb).isEqualTo(2);
			assertThat(backUpCounters.deleteDirNb).isZero();
			assertThat(backUpCounters.deleteNb).isZero();
			assertThat(backUpCounters.backupWithSizeAboveThreshold).isEqualTo(2);
			assertThat(backUpCounters.contentDifferentNb).isZero();
			assertThat(backUpCounters.nbHighPermanencePath).isZero();
			assertThat(backUpCounters.nbMediumPermanencePath).isEqualTo(2);
			assertThat(backUpCounters.nbSourceFilesFailed).isZero();
			assertThat(backUpCounters.nbSourceFilesProcessed).isEqualTo(3);
			assertThat(backUpCounters.nbTargetFilesProcessed).isZero();
			assertThat(backUpCounters.nbTargetFilesFailed).isZero();
			assertThat(backUpCounters.copyTargetNb).isZero();
			
			BackUpItemList backUpItemList = scannerResp.getBackUpItemList() ;
			assertThat(backUpItemList)
				.isNotNull()
				.hasSize(2);

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
			backUpScannerThread = new BackUpScannerThread(backUpTask) ;
			backUpRes = CompletableFuture.supplyAsync(backUpScannerThread::scan, scannerExecutor) ;
			
			scannerResp = backUpRes.get() ;
			backUpItemList = scannerResp.getBackUpItemList() ;
			
			assertNotNull(backUpItemList) ;
			assertEquals(0, backUpItemList.size()) ;
			
			// Delete target dir to recover initial state
			FilesUtils.deleteDirectoryTree(tgt, false, log) ;
			Files.createDirectory(tgt) ;
			
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Exception in BackUpScannerProcessor test", e);
			fail("Exception " + e.getMessage());
		}	
	}
	
	@Test
	void scanWithUnexistingTargetDir() {
		
		try {
								
			ExecutorService scannerExecutor = Config.getScanExecutorService() ;
			
			Path src  = TestUtils.getPathFromUriString(BUFFER_DATA_DIR) ;
			Path tgt  = TestUtils.getPathFromUriString(TARGET_DATA_DIR  + "doesNotExists/") ;
			
			BackUpTask backUpTask = new BackUpTask(src, tgt) ;
			
			BackUpScannerThread backUpScannerThread = new BackUpScannerThread(backUpTask) ;
			CompletableFuture<ScannerThreadResponse> backUpRes = CompletableFuture.supplyAsync(backUpScannerThread::scan, scannerExecutor) ;
			
			ScannerThreadResponse scannerResp = backUpRes.get() ;
			BackUpCounters backUpCounters = scannerResp.getBackUpCounters() ;			
			assertEquals(0, backUpCounters.ambiguousNb) ;
			assertEquals(0, backUpCounters.contentDifferentNb) ;
			assertEquals(0, backUpCounters.copyNewNb) ;
			assertEquals(0, backUpCounters.copyReplaceNb) ;
			assertEquals(1, backUpCounters.copyTreeNb) ;
			assertEquals(0, backUpCounters.deleteDirNb) ;
			assertEquals(0, backUpCounters.deleteNb) ;
			assertEquals(0, backUpCounters.nbSourceFilesFailed) ;
			assertEquals(1, backUpCounters.nbSourceFilesProcessed) ;
			assertEquals(0, backUpCounters.nbTargetFilesFailed) ;
			assertEquals(0, backUpCounters.nbTargetFilesProcessed) ;
			
			BackUpItemList backUpItemList = scannerResp.getBackUpItemList() ;
			assertNotNull(backUpItemList) ;
			assertEquals(1, backUpItemList.size()) ;
			BackUpItem backUpItem = backUpItemList.get(0) ;
			assertNotNull(backUpItem) ;
			assertEquals(BackUpItem.BackupAction.COPY_TREE, backUpItem.getBackupAction()) ;

		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Exception in BackUpScannerProcessor test", e);
			fail("Exception " + e.getMessage());
		}	
	}
	
	@Test
	void testSingleFile() {
		
		try {
			
			final String TGT_FILE1 =  TARGET_DATA_DIR + "singleFile" ;
			Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;
			Files.write(tgt, new ArrayList<String>(Arrays.asList("autre chose sur une ligne"))) ;
			assertTrue(Files.exists(tgt)) ;
			
			ExecutorService scannerExecutor = Config.getScanExecutorService() ;
			
			final String SRC_FILE1 =  BUFFER_DATA_DIR + "singleFile" ;
			Path src  = TestUtils.getPathFromUriString(SRC_FILE1) ;
			Files.write(src, new ArrayList<String>(Arrays.asList("quelque chose sur une ligne"))) ;
			assertTrue(Files.exists(src)) ;
			
			
			BackUpTask backUpTask = new BackUpTask(src, tgt) ;
			
			BackUpScannerThread backUpScannerThread = new BackUpScannerThread(backUpTask) ;
			CompletableFuture<ScannerThreadResponse> backUpRes = CompletableFuture.supplyAsync(backUpScannerThread::scan, scannerExecutor) ;
		
			ScannerThreadResponse scannerResp = backUpRes.get() ;
			BackUpItemList backUpItemList = scannerResp.getBackUpItemList() ;
			assertNotNull(backUpItemList) ;
			assertEquals(1, backUpItemList.size()) ;
			BackUpItem backUpItem = backUpItemList.get(0) ;
			assertNotNull(backUpItem) ;
			assertEquals(BackUpItem.BackupAction.COPY_REPLACE, backUpItem.getBackupAction()) ;

		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Exception in BackUpScannerProcessor test", e);
			fail("Exception " + e.getMessage());
		}	
	}
	
	@Test
	void testSingleFileUnexistingTarget() {
		
		try {
			
			ExecutorService scannerExecutor = Config.getScanExecutorService() ;
			
			final String SRC_FILE1 =  BUFFER_DATA_DIR + "singleFile" ;
			final String TGT_FILE1 =  TARGET_DATA_DIR + "doesNotExists" ;
			
			Path src  = TestUtils.getPathFromUriString(SRC_FILE1) ;
			Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;

			Files.write(src, new ArrayList<String>(Arrays.asList("quelque chose sur une ligne"))) ;
			
			assertTrue(Files.exists(src)) ;
			assertFalse(Files.exists(tgt)) ;
			
			BackUpTask backUpTask = new BackUpTask(src, tgt) ;
			
			BackUpScannerThread backUpScannerThread = new BackUpScannerThread(backUpTask) ;
			CompletableFuture<ScannerThreadResponse> backUpRes = CompletableFuture.supplyAsync(backUpScannerThread::scan, scannerExecutor) ;
		
			ScannerThreadResponse scannerResp = backUpRes.get() ;
			BackUpItemList backUpItemList = scannerResp.getBackUpItemList() ;
			assertNotNull(backUpItemList) ;
			assertEquals(1, backUpItemList.size()) ;
			BackUpItem backUpItem = backUpItemList.get(0) ;
			assertNotNull(backUpItem) ;
			assertEquals(BackUpItem.BackupAction.COPY_NEW, backUpItem.getBackupAction()) ;

		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Exception in BackUpScannerProcessor test", e);
			fail("Exception " + e.getMessage());
		}	
	}
	
	@Test
	void testSingleFileUnexistingSource() {
		
		try {
			
			ExecutorService scannerExecutor = Config.getScanExecutorService() ;
			
			final String SRC_FILE1 =  BUFFER_DATA_DIR + "doesNotExists" ;
			final String TGT_FILE1 =  TARGET_DATA_DIR + "singleFile" ;
			
			Path src  = TestUtils.getPathFromUriString(SRC_FILE1) ;
			Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;

			Files.write(tgt, new ArrayList<String>(Arrays.asList("quelque chose sur une ligne"))) ;
			
			assertFalse(Files.exists(src)) ;
			assertTrue(Files.exists(tgt)) ;
			
			BackUpTask backUpTask = new BackUpTask(src, tgt) ;
			
			BackUpScannerThread backUpScannerThread = new BackUpScannerThread(backUpTask) ;
			CompletableFuture<ScannerThreadResponse> backUpRes = CompletableFuture.supplyAsync(backUpScannerThread::scan, scannerExecutor) ;
		
			ScannerThreadResponse scannerResp = backUpRes.get() ;
			BackUpItemList backUpItemList = scannerResp.getBackUpItemList() ;
			assertNotNull(backUpItemList) ;
			assertEquals(1, backUpItemList.size()) ;
			BackUpItem backUpItem = backUpItemList.get(0) ;
			assertNotNull(backUpItem) ;
			assertEquals(BackUpItem.BackupAction.DELETE, backUpItem.getBackupAction()) ;

		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Exception in BackUpScannerProcessor test", e);
			fail("Exception " + e.getMessage());
		}	
	}
	
	@Test
	void testTargetDirectoryAndUnexistingSource() {
		
		try {
			
			ExecutorService scannerExecutor = Config.getScanExecutorService() ;
			
			final String SRC_FILE1 =  BUFFER_DATA_DIR + "doesNotExists" ;
			final String TGT_FILE1 =  TARGET_DATA_DIR + "targetDir/" ;
			
			Path src  = TestUtils.getPathFromUriString(SRC_FILE1) ;
			Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;

			Files.createDirectory(tgt) ;
			
			assertFalse(Files.exists(src)) ;
			assertTrue(Files.exists(tgt)) ;
			assertTrue(Files.isDirectory(tgt)) ;
			
			BackUpTask backUpTask = new BackUpTask(src, tgt) ;
			
			BackUpScannerThread backUpScannerThread = new BackUpScannerThread(backUpTask) ;
			CompletableFuture<ScannerThreadResponse> backUpRes = CompletableFuture.supplyAsync(backUpScannerThread::scan, scannerExecutor) ;
		
			ScannerThreadResponse scannerResp = backUpRes.get() ;
			BackUpItemList backUpItemList = scannerResp.getBackUpItemList() ;
			assertNotNull(backUpItemList) ;
			assertEquals(1, backUpItemList.size()) ;
			BackUpItem backUpItem = backUpItemList.get(0) ;
			assertNotNull(backUpItem) ;
			assertEquals(BackUpItem.BackupAction.DELETE_DIR, backUpItem.getBackupAction()) ;
			
			Files.delete(tgt) ;
			assertFalse(Files.exists(tgt)) ;

		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Exception in BackUpScannerProcessor test", e);
			fail("Exception " + e.getMessage());
		}	
	}
	
	@Test
	void testUnexistingSourceAndTarget() {
		
		try {
			
			ExecutorService scannerExecutor = Config.getScanExecutorService() ;
			
			final String SRC_FILE1 =  BUFFER_DATA_DIR + "doesNotExists" ;
			final String TGT_FILE1 =  TARGET_DATA_DIR + "doesNotExists" ;
			
			Path src  = TestUtils.getPathFromUriString(SRC_FILE1) ;
			Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;

			BackUpTask backUpTask = new BackUpTask(src, tgt) ;
			
			BackUpScannerThread backUpScannerThread = new BackUpScannerThread(backUpTask) ;
			CompletableFuture<ScannerThreadResponse> backUpRes = CompletableFuture.supplyAsync(backUpScannerThread::scan, scannerExecutor) ;
			
			ScannerThreadResponse scannerResp = backUpRes.get() ;
			BackUpItemList backUpItemList = scannerResp.getBackUpItemList() ;
			assertNotNull(backUpItemList) ;
			assertEquals(0, backUpItemList.size());

		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Exception in BackUpScannerProcessor test", e);
			fail("Exception " + e.getMessage());
		}	
	}
	
	@AfterAll
	static void deleteTestData() {
		
		try {
			Path bufferDataDir = Paths.get(new URI(BUFFER_DATA_DIR)) ;
			Path targetDataDir = Paths.get(new URI(TARGET_DATA_DIR)) ;
		
			boolean b1 = FilesUtils.deleteDirectoryTree(bufferDataDir, true, log) ;
			boolean b2 = FilesUtils.deleteDirectoryTree(targetDataDir, true, log) ;
			if (! (b1 && b2)) {
				fail("Errors deleting test data (AfterAll method)") ;
			} 
		} catch (URISyntaxException e) {
			log.log(Level.SEVERE, "URI exception deleting test data", e) ;
			fail("URI exception deleting test data (AfterAll method)") ;
		} catch (IOException e) {
			log.log(Level.SEVERE, "IO exception writing test data", e) ;
			fail("IO exception writing test data (BeforeAll method)") ;
		}

	}
}
