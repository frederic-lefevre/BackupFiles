package org.fl.backupFiles;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpItem.BackupAction;
import org.junit.jupiter.api.Test;

import com.ibm.lge.fl.util.file.FileComparator;

public class BackUpItemTest {

	@Test
	void test() {
		
		Logger log = Logger.getGlobal();
		
		Path src 		 = Paths.get("") ;
		Path tgt 		 = Paths.get("") ;
		Path srcExisting = Paths.get("") ;
		
		BackUpItem backUpItem = new BackUpItem(src, tgt, srcExisting, BackupAction.COPY_REPLACE, log) ;
		
		BackupAction action = backUpItem.getBackupAction() ;
		
		assertEquals(BackupAction.COPY_REPLACE, action) ;
		
		final String SRC_FILE1 =  "file:///ForTests/BackUpFiles/TestDir1/File1.pdf" ;
		final String TGT_FILE1 =  "file:///ForTests/BackUpFiles/TestDir2/File1.pdf" ;
		
		src  = getPathFromUriString(SRC_FILE1) ;
		tgt  = getPathFromUriString(TGT_FILE1) ;
		backUpItem = new BackUpItem(src, tgt, src, BackupAction.COPY_NEW, log) ;
		
		BackUpCounters counters = new BackUpCounters() ;
		assertEquals(0, counters.copyNewNb) ;
		backUpItem.execute(counters);
		
		FileComparator fileComparator = new FileComparator(log) ;
		
		boolean same = fileComparator.haveSameContent(src, tgt) ;
		assertTrue(same) ;		
		assertEquals(1, counters.copyNewNb) ;
		
		backUpItem = new BackUpItem(null, tgt, src.getParent(), BackupAction.DELETE, log) ;
		backUpItem.execute(counters);
		
		assertEquals(1, counters.deleteNb) ;
		assertFalse(Files.exists(tgt)) ;
	}
	
	private Path getPathFromUriString(String uriString) {
		return Paths.get(URI.create(uriString)) ;
	}
}
