package org.fl.backupFiles;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

class BackUpTaskTest {

	@Test
	void test1() {
		
		Logger log = Logger.getGlobal() ;
		
		final String SRC_FILE1 =  "file:///ForTests/BackUpFiles/TestDir1/File1.pdf" ;
		final String TGT_FILE1 =  "file:///ForTests/BackUpFiles/TestDir2/File1.pdf" ;
		
		Path src  = TestUtils.getPathFromUriString(SRC_FILE1) ;
		Path tgt  = TestUtils.getPathFromUriString(TGT_FILE1) ;
		
		BackUpTask backUpTask = new BackUpTask(src, tgt, log) ;
		
		assertFalse(backUpTask.compareContent()) ;
		
		BackUpTask backUpTask2 = new BackUpTask(src, tgt, log) ;
		
		assertTrue(backUpTask.equals(backUpTask2)) ;
	}

}
