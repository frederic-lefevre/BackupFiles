package org.fl.backupFiles;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpItem.BackupAction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import com.ibm.lge.fl.util.RunningContext;
import com.ibm.lge.fl.util.file.FileComparator;

public class BackUpItemTest {

	private static final String DEFAULT_PROP_FILE = "file:///ForTests/BackUpFiles/backupFiles.properties";

	private static Logger log ;
	
	@BeforeAll
	static void initConfig() {
		
		try {
			RunningContext runningContext = new RunningContext("BackupFilesTest", null, new URI(DEFAULT_PROP_FILE));
			log = runningContext.getpLog() ;
			Config.initConfig(runningContext.getProps(), log);
			
		} catch (URISyntaxException e) {
			log.log(Level.SEVERE, "URI exception writing test data", e);
			fail("URI exception writing test data (BeforeAll method)") ;
		}
	}
	
	@Test
	void test1() {
		
		Path src 		 = Paths.get("") ;
		Path tgt 		 = Paths.get("") ;
		
		BackUpCounters counters = new BackUpCounters() ;
		BackUpItem backUpItem = new BackUpItem(src, tgt, BackupAction.COPY_REPLACE, 0, counters, log) ;
		
		BackupAction action = backUpItem.getBackupAction() ;
		
		assertEquals(BackupAction.COPY_REPLACE, action) ;
	}
		
	@Test
	void test2() {
		
		final String SRC_FILE1 =  "file:///ForTests/BackUpFiles/TestDir1/File1.pdf" ;
		final String TGT_FILE1 =  "file:///ForTests/BackUpFiles/TestDir2/File1.pdf" ;
		
		Path src  = TestUtils.getPathFromUriString(SRC_FILE1) ;
		Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;
		
		BackUpCounters counters = new BackUpCounters() ;
		BackUpItem backUpItem = new BackUpItem(src, tgt, BackupAction.COPY_NEW, 0, counters, log) ;
		
		assertEquals(1, getTotalCounters(counters)) ;
		
		counters.reset();
		backUpItem.execute(counters);
		
		FileComparator fileComparator = new FileComparator(log) ;
		
		boolean same = fileComparator.haveSameContent(src, tgt) ;
		assertTrue(same) ;		
		assertEquals(1, counters.copyNewNb) ;
		assertEquals(1, counters.nbSourceFilesProcessed) ;
		assertEquals(2, getTotalCounters(counters)) ;
		
		counters.reset();
		backUpItem = new BackUpItem(tgt, BackupAction.DELETE, src.getParent(), 0, counters, log) ;
		assertEquals(0, counters.copyNewNb) ;
		assertEquals(0, counters.nbSourceFilesProcessed) ;
		assertEquals(1, counters.deleteNb) ;
		assertEquals(0, counters.nbTargetFilesProcessed) ;
		assertEquals(1, getTotalCounters(counters)) ;
		
		counters.reset();
		backUpItem.execute(counters);
		
		assertFalse(Files.exists(tgt)) ;
		assertEquals(0, counters.nbSourceFilesProcessed) ;
		assertEquals(1, counters.deleteNb) ;
		assertEquals(1, counters.nbTargetFilesProcessed) ;
		assertEquals(2, getTotalCounters(counters)) ;	
	}
	
	@Test
	void nullSrcShouldThrowException() {
		
		final String TGT_FILE1 = "file:///ForTests/BackUpFiles/TestDir2/File1.pdf" ;
		
		Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;
		
		BackUpCounters counters = new BackUpCounters() ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(null, tgt, BackupAction.COPY_NEW, 0, counters, log)) ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(null, tgt, BackupAction.COPY_REPLACE, 0, counters, log)) ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(null, tgt, BackupAction.COPY_TREE, 0, counters, log)) ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(null, tgt, BackupAction.AMBIGUOUS, 0, counters, log)) ;
	}
	@Test
	
	void illegalCopyShouldThrowException() {
		
		final String SRC_FILE1 =  "file:///ForTests/BackUpFiles/TestDir1" ;
		final String TGT_FILE1 =  "file:///ForTests/BackUpFiles/TestDir2/File1.pdf" ;
		
		Path src  = TestUtils.getPathFromUriString(SRC_FILE1) ;
		Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;
		
		BackUpCounters counters = new BackUpCounters() ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(tgt, BackupAction.COPY_NEW, src, 0, counters, log)) ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(tgt, BackupAction.COPY_REPLACE, src, 0, counters, log)) ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(tgt, BackupAction.COPY_TREE, src, 0, counters, log)) ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(tgt, BackupAction.AMBIGUOUS, src, 0, counters, log)) ;
	}
	
	@Test
	void illegalDeleteShouldThrowException() {
		
		final String SRC_FILE1 =  "file:///ForTests/BackUpFiles/TestDir1/File1.pdf" ;
		final String TGT_FILE1 =  "file:///ForTests/BackUpFiles/TestDir2/File1.pdf" ;
		
		Path src  = TestUtils.getPathFromUriString(SRC_FILE1) ;
		Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;
		
		BackUpCounters counters = new BackUpCounters() ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(src, tgt, BackupAction.DELETE, 0, counters, log)) ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(src, tgt, BackupAction.DELETE_DIR, 0, counters, log)) ;
	}
	
	@Test
	void unexistantSrcShouldThrowException() {
		
		final String SRC_FILE1 		= "file:///ForTests/BackUpFiles/TestDir1/doesNotExists.pdf" ;
		final String TGT_FILE1 		= "file:///ForTests/BackUpFiles/TestDir2/File1.pdf" ;
		
		Path src  			= TestUtils.getPathFromUriString(SRC_FILE1) ;
		Path tgt  			= TestUtils.getPathFromUriString(TGT_FILE1) ;
		
		BackUpCounters counters = new BackUpCounters() ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(src, tgt, BackupAction.COPY_NEW, 0, counters, log)) ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(src, tgt, BackupAction.COPY_REPLACE, 0, counters, log)) ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(src, tgt, BackupAction.COPY_TREE, 0, counters, log)) ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(src, tgt, BackupAction.AMBIGUOUS, 0, counters, log)) ;
	}
	
	@Test
	void unexistantExistingSrcShouldThrowException() throws IOException {
		
		final String SRC_FILE1 = "file:///ForTests/BackUpFiles/doesNotExists" ;
		final String TGT_FILE1 = "file:///ForTests/BackUpFiles/TestDir2/File1.pdf" ;
		
		Path src  = TestUtils.getPathFromUriString(SRC_FILE1) ;
		Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;
		
		BackUpCounters counters = new BackUpCounters() ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(tgt, BackupAction.DELETE, src, 0, counters, log)) ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(tgt, BackupAction.DELETE_DIR, src, 0, counters, log)) ;
	}
	
	@Test
	void existantTgtShouldThrowException() throws IOException {
		
		final String SRC_FILE1 = "file:///ForTests/BackUpFiles/TestDir1/File1.pdf" ;
		final String TGT_FILE1 = "file:///ForTests/BackUpFiles/TestDir2/File1.pdf" ;
		
		Path src  = TestUtils.getPathFromUriString(SRC_FILE1) ;
		Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;
		
		Files.copy(src, tgt) ;
		assertTrue(Files.exists(src)) ;
		assertTrue(Files.exists(tgt)) ;
		
		BackUpCounters counters = new BackUpCounters() ;
		assertThrows(IllegalBackUpItemException.class, () -> new BackUpItem(src, tgt, BackupAction.COPY_NEW, 0, counters, log)) ;
		Files.delete(tgt) ;
	}
	
	private long getTotalCounters(BackUpCounters counters) {
		
		return	counters.ambiguousNb			+
				counters.contentDifferentNb		+
				counters.copyNewNb				+
				counters.copyReplaceNb			+
				counters.copyTreeNb				+
				counters.deleteDirNb			+
				counters.deleteNb				+
				counters.nbSourceFilesFailed	+
				counters.nbSourceFilesProcessed	+
				counters.nbTargetFilesFailed	+
				counters.nbTargetFilesProcessed	;
	}
}
