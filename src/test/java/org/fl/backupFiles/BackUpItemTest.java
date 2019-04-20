package org.fl.backupFiles;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpItem.BackupAction;
import org.junit.jupiter.api.Test;

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
	}
}
