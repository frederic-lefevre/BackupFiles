package org.fl.backupFiles;

import static org.junit.jupiter.api.Assertions.*;

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
		Path srcExisting = Paths.get("") ;
		
		BackUpCounters counters = new BackUpCounters() ;
		BackUpItem backUpItem = new BackUpItem(src, tgt, srcExisting, BackupAction.COPY_REPLACE, 0, false, counters, log) ;
		
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
		BackUpItem backUpItem = new BackUpItem(src, tgt, src, BackupAction.COPY_NEW, 0, false, counters, log) ;
		
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
		backUpItem = new BackUpItem(null, tgt, src.getParent(), BackupAction.DELETE, 0, false, counters, log) ;
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
