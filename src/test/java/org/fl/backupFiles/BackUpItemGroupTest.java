/*
 * MIT License

Copyright (c) 2017, 2025 Frederic Lefevre

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

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.fl.backupFiles.directoryGroup.DirectoryPermanenceLevel;
import org.fl.backupFiles.scanner.PathPairBasicAttributes;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BackUpItemGroupTest {

	private static final String DEFAULT_PROP_FILE = "file:///ForTests/BackUpFiles/backupFiles.properties";
	
	private static final String EXISTANT_FOLDER = "file:///ForTests/BackUpFiles/TestDir1/";
	private static final String UNEXISTANT_FOLDER = "file:///ForTests/BackUpFiles/doesNotExists";
	private static final String SRC_FILE1 = EXISTANT_FOLDER + "File1.pdf";
	private static final String TGT_FILE1 = "file:///ForTests/BackUpFiles/TestDir2/File1.pdf";
	
	private static final Path EXISTANT_FOLDER_PATH = TestUtils.getPathFromUriString(EXISTANT_FOLDER);
	private static final Path UNEXISTANT_FOLDER_PATH = TestUtils.getPathFromUriString(UNEXISTANT_FOLDER);
	private static final Path EXISTANT_SOURCE = TestUtils.getPathFromUriString(SRC_FILE1);
	private static final Path UNEXISTANT_TARGET = TestUtils.getPathFromUriString(TGT_FILE1);
	
	private static BackUpTask backUpTask;
	
	private static TargetFileStores newTargetFileStores() {
		TargetFileStores targetFileStores = new TargetFileStores();
		targetFileStores.addTargetFileStore(Paths.get("/"), 5);
		return targetFileStores;
	}
	
	@BeforeAll
	static void initConfig() throws IOException {

		Config.initConfig(DEFAULT_PROP_FILE);
		backUpTask = new BackUpTask(EXISTANT_FOLDER_PATH, EXISTANT_FOLDER_PATH, 0);
	}
	
	@Test
	void nullBackupActionShouldThrowException() {
		assertThatExceptionOfType(IllegalBackupActionException.class)
			.isThrownBy(() -> new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, null, BackupStatus.DIFFERENT, DirectoryPermanenceLevel.HIGH));
	}
	
	@Test
	void nullBackupStatusShouldThrowException() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, BackupAction.COPY_NEW, null, DirectoryPermanenceLevel.HIGH));
	}
	
	@Test
	void testNewBackUpItemGroup() {
		
		BackUpItemGroup backUpItemGroup =
				new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, DirectoryPermanenceLevel.HIGH);
		
		assertThat(backUpItemGroup).isNotNull();
		assertThat(backUpItemGroup.getSourcePath()).isEqualTo(EXISTANT_FOLDER_PATH);
		assertThat(backUpItemGroup.getTargetPath()).isEqualTo(UNEXISTANT_FOLDER_PATH);
		assertThat(backUpItemGroup.getBackupAction()).isEqualTo(BackupAction.COPY_NEW);
		assertThat(backUpItemGroup.getBackupStatus()).isEqualTo(BackupStatus.DIFFERENT);
		assertThat(backUpItemGroup.getBackUpItems()).isNotNull().isEmpty();
		assertThat(backUpItemGroup.getBackUpItemNumber()).isZero();
		assertThat(backUpItemGroup.getSizeDifference()).isZero();
		assertThat(backUpItemGroup.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.MEDIUM);
	}
	
	@Test
	void testAddBackUpItem() {
		
		BackUpItemGroup backUpItemGroup =
				new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, DirectoryPermanenceLevel.MEDIUM);
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask);
		
		BackUpItemGroup backUpItemGroupReturned =backUpItemGroup.addBackUpItem(backUpItem);
		
		assertThat(backUpItemGroupReturned).isEqualTo(backUpItemGroup);
		assertThat(backUpItemGroup.getBackUpItemNumber()).isEqualTo(1);
		assertThat(backUpItemGroup.getBackUpItems()).isNotNull().singleElement().isEqualTo(backUpItem);
		assertThat(backUpItemGroup.getSizeDifference()).isEqualTo(backUpItem.getSizeDifference());		
	}
	
	@Test
	void addBackUpItemWithDifferentStatusShouldThrowException() {
		
		BackUpItemGroup backUpItemGroup =
				new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, DirectoryPermanenceLevel.MEDIUM);
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFF_BY_CONTENT, counters, backUpTask);
		
		assertThatIllegalArgumentException().isThrownBy(() -> backUpItemGroup.addBackUpItem(backUpItem)).withMessageContaining("status");			
	}
	
	@Test
	void addBackUpItemWithDifferentActionShouldThrowException() {
		
		BackUpItemGroup backUpItemGroup =
				new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, BackupAction.COPY_REPLACE, BackupStatus.DIFFERENT, DirectoryPermanenceLevel.MEDIUM);
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask);
		
		assertThatIllegalArgumentException().isThrownBy(() -> backUpItemGroup.addBackUpItem(backUpItem)).withMessageContaining("action");			
	}
	
	/*
	@Test
	void addBackUpItemWithDifferentPermanenceLevelShouldThrowException() {
		
		BackUpItemGroup backUpItemGroup =
				new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, DirectoryPermanenceLevel.HIGH);
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask);
		
		assertThatIllegalArgumentException().isThrownBy(() -> backUpItemGroup.addBackUpItem(backUpItem)).withMessageContaining("permanance level");		
	}
	*/
}
