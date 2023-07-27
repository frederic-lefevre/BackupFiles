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

package org.fl.backupFiles;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpItem.BackupAction;
import org.fl.backupFiles.BackUpItem.BackupStatus;
import org.fl.util.file.FileComparator;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class BackUpItemTest {

	private static final String DEFAULT_PROP_FILE = "file:///ForTests/BackUpFiles/backupFiles.properties";

	private static Logger log;
	
	private static final String SRC_FOLDER = "file:///ForTests/BackUpFiles/TestDir1/";
	private static final String SRC_FILE1 = SRC_FOLDER + "File1.pdf";
	private static final String TGT_FILE1 = "file:///ForTests/BackUpFiles/TestDir2/File1.pdf";
	private static final String UNEXISTANT_FILE = "file:///ForTests/BackUpFiles/TestDir1/doesNotExists.pdf";
	
	private static final Path EXISTANT_SOURCE_FOLDER  = TestUtils.getPathFromUriString(SRC_FOLDER);
	private static final Path EXISTANT_SOURCE  = TestUtils.getPathFromUriString(SRC_FILE1);
	private static final Path UNEXISTANT_TARGET  = TestUtils.getPathFromUriString(TGT_FILE1);
	private static final Path UNEXISTANT_PATH = TestUtils.getPathFromUriString(UNEXISTANT_FILE);
	
	
	private static final String UNEXISTANT_FOLDER = "file:///ForTests/BackUpFiles/doesNotExists" ;
	private static final Path UNEXISTANT_FOLDER_PATH  = TestUtils.getPathFromUriString(UNEXISTANT_FOLDER) ;
	
	@BeforeAll
	static void initConfig() {
		
		Config.initConfig(DEFAULT_PROP_FILE);
		log = Config.getLogger();
	}
	
	@Test
	void test1() {
		
		Path src 		 = Paths.get("") ;
		Path tgt 		 = Paths.get("") ;
		
		BackUpCounters counters = new BackUpCounters() ;
		BackUpItem backUpItem = new BackUpItem(src, tgt, BackupAction.COPY_REPLACE, BackupStatus.DIFFERENT, 0, counters, log) ;
		
		BackupAction action = backUpItem.getBackupAction() ;
		
		assertThat(action).isEqualTo(BackupAction.COPY_REPLACE);
	}
		
	@Test
	void test2() {
		
		BackUpCounters counters = new BackUpCounters() ;
		BackUpItem backUpItem = new BackUpItem(EXISTANT_SOURCE, UNEXISTANT_TARGET, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, 0, counters, log) ;
		
		assertThat(getTotalCounters(counters)).isEqualTo(1);
		
		counters.reset();
		backUpItem.execute(counters);
		
		FileComparator fileComparator = new FileComparator(log) ;
		
		assertThat(fileComparator.haveSameContent(EXISTANT_SOURCE, UNEXISTANT_TARGET)).isTrue();
	
		assertThat(counters.nbSourceFilesProcessed).isEqualTo(1);
		assertThat(counters.copyNewNb).isEqualTo(1);
		assertThat(getTotalCounters(counters)).isEqualTo(2);
		
		counters.reset();
		backUpItem = new BackUpItem(UNEXISTANT_TARGET, BackupAction.DELETE, EXISTANT_SOURCE.getParent(), 0, counters, log) ;
		
		assertThat(counters.copyNewNb).isZero();
		assertThat(counters.nbSourceFilesProcessed).isZero();
		assertThat(counters.nbTargetFilesProcessed).isZero();
		assertThat(counters.deleteNb).isEqualTo(1);
		assertThat(getTotalCounters(counters)).isEqualTo(1);
		
		counters.reset();
		backUpItem.execute(counters);
		
		assertThat(Files.exists(UNEXISTANT_TARGET)).isFalse();
		assertThat(counters.nbSourceFilesProcessed).isZero();
		assertThat(counters.deleteNb).isEqualTo(1);
		assertThat(counters.nbTargetFilesProcessed).isEqualTo(1);
		assertThat(getTotalCounters(counters)).isEqualTo(2);
	}
	
	@Test
	void nullSrcShouldThrowException() {
		
		BackUpCounters counters = new BackUpCounters() ;

		assertThatExceptionOfType(IllegalBackUpItemException.class).isThrownBy(() -> 
			new BackUpItem(null, UNEXISTANT_TARGET, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, 0, counters, log));
		assertThatExceptionOfType(IllegalBackUpItemException.class).isThrownBy(() -> 
			new BackUpItem(null, UNEXISTANT_TARGET, BackupAction.COPY_REPLACE, BackupStatus.DIFFERENT, 0, counters, log));
		assertThatExceptionOfType(IllegalBackUpItemException.class).isThrownBy(() -> 
			new BackUpItem(null, UNEXISTANT_TARGET, BackupAction.COPY_TREE, BackupStatus.DIFFERENT, 0, counters, log));
		assertThatExceptionOfType(IllegalBackUpItemException.class).isThrownBy(() ->
			new BackUpItem(null, UNEXISTANT_TARGET, BackupAction.AMBIGUOUS, BackupStatus.SAME_CONTENT, 0, counters, log));
		assertThatExceptionOfType(IllegalBackUpItemException.class).isThrownBy(() -> 
			new BackUpItem(null, UNEXISTANT_TARGET, BackupAction.COPY_TARGET, BackupStatus.SAME_CONTENT, 0, counters, log));
	}
	@Test
	
	void illegalCopyShouldThrowException() {
		
		Path tgt  = EXISTANT_SOURCE;
		
		BackUpCounters counters = new BackUpCounters() ;

		assertThatExceptionOfType(IllegalBackupActionException.class).isThrownBy(() -> 
			new BackUpItem(tgt, BackupAction.COPY_NEW, EXISTANT_SOURCE_FOLDER, 0, counters, log));
		assertThatExceptionOfType(IllegalBackupActionException.class).isThrownBy(() -> 
			new BackUpItem(tgt, BackupAction.COPY_REPLACE, EXISTANT_SOURCE_FOLDER, 0, counters, log));
		assertThatExceptionOfType(IllegalBackupActionException.class).isThrownBy(() -> 
			new BackUpItem(tgt, BackupAction.COPY_TREE, EXISTANT_SOURCE_FOLDER, 0, counters, log));
		assertThatExceptionOfType(IllegalBackupActionException.class).isThrownBy(() -> 
			new BackUpItem(tgt, BackupAction.AMBIGUOUS, EXISTANT_SOURCE_FOLDER, 0, counters, log));
		assertThatExceptionOfType(IllegalBackupActionException.class).isThrownBy(() -> 
			new BackUpItem(tgt, BackupAction.COPY_TARGET, EXISTANT_SOURCE_FOLDER, 0, counters, log));
	}
	
	@Test
	void illegalDeleteShouldThrowException() {
		
		BackUpCounters counters = new BackUpCounters() ;

		assertThatExceptionOfType(IllegalBackupActionException.class).isThrownBy(() -> 
			new BackUpItem(EXISTANT_SOURCE, UNEXISTANT_TARGET, BackupAction.DELETE, BackupStatus.DIFFERENT, 0, counters, log));
		assertThatExceptionOfType(IllegalBackupActionException.class).isThrownBy(() -> 
			new BackUpItem(EXISTANT_SOURCE, UNEXISTANT_TARGET, BackupAction.DELETE_DIR, BackupStatus.DIFFERENT, 0, counters, log));
	}
	
	@Test
	void unexistantSrcShouldThrowException() {
		
		BackUpCounters counters = new BackUpCounters() ;

		assertThatExceptionOfType(IllegalBackUpItemException.class).isThrownBy(() -> 
			new BackUpItem(UNEXISTANT_PATH, UNEXISTANT_TARGET, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, 0, counters, log));
		assertThatExceptionOfType(IllegalBackUpItemException.class).isThrownBy(() -> 
			new BackUpItem(UNEXISTANT_PATH, UNEXISTANT_TARGET, BackupAction.COPY_REPLACE, BackupStatus.DIFFERENT, 0, counters, log));
		assertThatExceptionOfType(IllegalBackUpItemException.class).isThrownBy(() -> 
			new BackUpItem(UNEXISTANT_PATH, UNEXISTANT_TARGET, BackupAction.COPY_TREE, BackupStatus.DIFFERENT, 0, counters, log));
		assertThatExceptionOfType(IllegalBackUpItemException.class).isThrownBy(() -> 
			new BackUpItem(UNEXISTANT_PATH, UNEXISTANT_TARGET, BackupAction.AMBIGUOUS, BackupStatus.DIFFERENT, 0, counters, log));
		assertThatExceptionOfType(IllegalBackUpItemException.class).isThrownBy(() -> 
			new BackUpItem(UNEXISTANT_PATH, UNEXISTANT_TARGET, BackupAction.COPY_TARGET, BackupStatus.DIFFERENT, 0, counters, log));
	}
	
	@Test
	void unexistantExistingSrcShouldThrowException() throws IOException {
		
		BackUpCounters counters = new BackUpCounters() ;
		assertThatExceptionOfType(IllegalBackUpItemException.class).isThrownBy(() -> 
			new BackUpItem(UNEXISTANT_TARGET, BackupAction.DELETE, UNEXISTANT_FOLDER_PATH, 0, counters, log));
		assertThatExceptionOfType(IllegalBackUpItemException.class).isThrownBy(() -> 
			new BackUpItem(UNEXISTANT_TARGET, BackupAction.DELETE_DIR, UNEXISTANT_FOLDER_PATH, 0, counters, log));
	}
	
	@Test
	void existantTgtShouldThrowException() throws IOException {
			
		Files.copy(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		Path nowExists = UNEXISTANT_TARGET;
		
		assertThat(Files.exists(EXISTANT_SOURCE)).isTrue();
		assertThat(Files.exists(nowExists)).isTrue();
		
		BackUpCounters counters = new BackUpCounters() ;
		assertThatExceptionOfType(IllegalBackUpItemException.class).isThrownBy(() -> 
			new BackUpItem(EXISTANT_SOURCE, nowExists, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, 0, counters, log));
	}
	
	@Test
	void shouldCopyTarget() throws IOException {
			
		Files.copy(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		
		Path nowExists = UNEXISTANT_TARGET;
		assertThat(Files.exists(EXISTANT_SOURCE)).isTrue();
		assertThat(Files.exists(nowExists)).isTrue();
		
		BackUpCounters counters = new BackUpCounters() ;
		
		BackUpItem backUpItem = new BackUpItem(EXISTANT_SOURCE, nowExists, BackupAction.COPY_TARGET, BackupStatus.SAME_CONTENT, 0, counters, log) ;
		
		assertThat(counters.copyTargetNb).isEqualTo(1);
		
		counters.reset();
		backUpItem.execute(counters);
		
		assertThat(counters.copyTargetNb).isEqualTo(1);
		assertThat(getTotalCounters(counters)).isEqualTo(2);
	}
	
	@AfterEach
	void clean() throws IOException {
		
		if (Files.exists(UNEXISTANT_TARGET)) {
			Files.delete(UNEXISTANT_TARGET);
		}
	
	}
	
	private long getTotalCounters(BackUpCounters counters) {
		
		return	counters.ambiguousNb			+
				counters.contentDifferentNb		+
				counters.copyNewNb				+
				counters.copyReplaceNb			+
				counters.copyTreeNb				+
				counters.deleteDirNb			+
				counters.deleteNb				+
				counters.copyTargetNb			+
				counters.nbSourceFilesFailed	+
				counters.nbSourceFilesProcessed	+
				counters.nbTargetFilesFailed	+
				counters.nbTargetFilesProcessed	;
	}
}
