package org.fl.backupFiles;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpItem.BackupAction;
import org.junit.jupiter.api.Test;

import com.ibm.lge.fl.util.file.FileComparator;

public class BackUpItemTest {

	@Test
	void test1() {
		
		Path src 		 = Paths.get("") ;
		Path tgt 		 = Paths.get("") ;
		Path srcExisting = Paths.get("") ;
		
		BackUpItem backUpItem = new BackUpItem(src, tgt, srcExisting, BackupAction.COPY_REPLACE, 0, Logger.getGlobal()) ;
		
		BackupAction action = backUpItem.getBackupAction() ;
		
		assertEquals(BackupAction.COPY_REPLACE, action) ;
	}
		
	@Test
	void test2() {
		
		Logger log = Logger.getGlobal() ;
		
		final String SRC_FILE1 =  "file:///ForTests/BackUpFiles/TestDir1/File1.pdf" ;
		final String TGT_FILE1 =  "file:///ForTests/BackUpFiles/TestDir2/File1.pdf" ;
		
		Path src  = TestUtils.getPathFromUriString(SRC_FILE1) ;
		Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;
		
		BackUpItem backUpItem = new BackUpItem(src, tgt, src, BackupAction.COPY_NEW, 0, log) ;
		
		BackUpCounters counters = new BackUpCounters() ;
		assertEquals(0, getTotalCounters(counters)) ;
		backUpItem.execute(counters);
		
		FileComparator fileComparator = new FileComparator(log) ;
		
		boolean same = fileComparator.haveSameContent(src, tgt) ;
		assertTrue(same) ;		
		assertEquals(1, counters.copyNewNb) ;
		assertEquals(1, counters.nbSourceFilesProcessed) ;
		assertEquals(2, getTotalCounters(counters)) ;
		
		backUpItem = new BackUpItem(null, tgt, src.getParent(), BackupAction.DELETE, 0, log) ;
		backUpItem.execute(counters);
		
		assertFalse(Files.exists(tgt)) ;
		assertEquals(1, counters.copyNewNb) ;
		assertEquals(1, counters.nbSourceFilesProcessed) ;
		assertEquals(1, counters.deleteNb) ;
		assertEquals(1, counters.nbTargetFilesProcessed) ;
		assertEquals(4, getTotalCounters(counters)) ;
		
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
