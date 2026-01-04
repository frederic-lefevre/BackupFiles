/*
 * MIT License

Copyright (c) 2017, 2026 Frederic Lefevre

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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

import org.fl.backupFiles.directoryGroup.DirectoryGroupConfiguration;
import org.fl.backupFiles.directoryGroup.DirectoryGroupMap;
import org.fl.backupFiles.directoryGroup.DirectoryPermanenceLevel;
import org.fl.backupFiles.scanner.PathPairBasicAttributes;
import org.fl.util.RunningContext;
import org.fl.util.file.FileComparator;
import org.fl.util.file.FilesUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BackUpItemGroupTest {

	private static Logger log = Logger.getLogger(BackUpItemGroupTest.class.getName());
	
	private static final String DEFAULT_PROP_FILE = "file:///ForTests/BackUpFiles/backupFiles.properties";
	
	private static final String EXISTANT_FOLDER = "file:///ForTests/BackUpFiles/TestDir1/";
	private static final String UNEXISTANT_FOLDER = "file:///ForTests/BackUpFiles/doesNotExists";
	private static final String SRC_FILE1 = EXISTANT_FOLDER + "File1.pdf";
	private static final String TGT_FILE1 = "file:///ForTests/BackUpFiles/TestDir2/File1.pdf";
	
	private static Path EXISTANT_FOLDER_PATH;
	private static Path UNEXISTANT_FOLDER_PATH;
	private static Path EXISTANT_SOURCE;
	private static Path UNEXISTANT_TARGET;
	private static Path FOLDER_PATH_FROM_A_DIFFERENT_GROUP;
	
	private static BackUpTask backUpTask;
	
	private static TargetFileStores newTargetFileStores() {
		TargetFileStores targetFileStores = new TargetFileStores();
		targetFileStores.addTargetFileStore(Paths.get("/"), 5);
		return targetFileStores;
	}
	
	@BeforeAll
	static void initConfig() throws IOException, URISyntaxException {

		EXISTANT_FOLDER_PATH = FilesUtils.uriStringToAbsolutePath(EXISTANT_FOLDER);
		UNEXISTANT_FOLDER_PATH = FilesUtils.uriStringToAbsolutePath(UNEXISTANT_FOLDER);
		EXISTANT_SOURCE = FilesUtils.uriStringToAbsolutePath(SRC_FILE1);
		UNEXISTANT_TARGET = FilesUtils.uriStringToAbsolutePath(TGT_FILE1);
		FOLDER_PATH_FROM_A_DIFFERENT_GROUP = FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/ecrins/");
		Path sourcePathForDirectoryMap = FilesUtils.uriStringToAbsolutePath("file:///ForTests/BackUpFiles/TestDir1/");
		Config.setRunningContextSupplier(() -> new RunningContext("org.fl.backupFiles", DEFAULT_PROP_FILE));
		DirectoryGroupConfiguration directoryGroupConfiguration = new DirectoryGroupConfiguration(Config.getBackupGroupConfiguration());
		backUpTask = new BackUpTask(EXISTANT_FOLDER_PATH, EXISTANT_FOLDER_PATH, new DirectoryGroupMap(sourcePathForDirectoryMap, sourcePathForDirectoryMap, directoryGroupConfiguration), 0);
	}
	
	@Test
	void nullBackupActionShouldThrowException() {
		assertThatExceptionOfType(IllegalBackupActionException.class)
			.isThrownBy(() -> new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, EXISTANT_FOLDER_PATH, null, BackupStatus.DIFFERENT, backUpTask));
	}
	
	@Test
	void nullBackupStatusShouldThrowException() {
		assertThatIllegalArgumentException()
			.isThrownBy(() -> new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, EXISTANT_FOLDER_PATH, BackupAction.COPY_NEW, null, backUpTask));
	}
	
	@Test
	void testNewBackUpItemGroup() {
		
		BackUpItemGroup backUpItemGroup =
				new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, EXISTANT_FOLDER_PATH, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, backUpTask);
		
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
				new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, EXISTANT_FOLDER_PATH, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, backUpTask);
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask);
		
		assertThat(backUpItemGroup.addBackUpItem(backUpItem)).isTrue();
		
		assertThat(backUpItemGroup.getBackUpItemNumber()).isEqualTo(1);
		assertThat(backUpItemGroup.getBackUpItems()).isNotNull().singleElement().isEqualTo(backUpItem);
		assertThat(backUpItemGroup.getSizeDifference()).isEqualTo(backUpItem.getSizeDifference());		
	}
	
	@Test
	void testExecuteBackUpItemGroup() {
		
		BackUpItemGroup backUpItemGroup =
				new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, EXISTANT_FOLDER_PATH, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, backUpTask);
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask);
		
		backUpItemGroup.addBackUpItem(backUpItem);
		counters.reset();
		boolean result = backUpItemGroup.execute(counters);

		assertThat(result).isTrue();
		assertThat(backUpItem.getBackupStatus()).isEqualTo(BackupStatus.DONE);
		assertThat(backUpItemGroup.getBackupStatus()).isEqualTo(BackupStatus.DONE);
		
		FileComparator fileComparator = new FileComparator(log);

		assertThat(fileComparator.haveSameContent(EXISTANT_SOURCE, UNEXISTANT_TARGET)).isTrue();
	}
	
	@Test
	void addBackUpItemWithDifferentStatusShouldThrowException() {
		
		BackUpItemGroup backUpItemGroup =
				new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, EXISTANT_FOLDER_PATH, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, backUpTask);
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFF_BY_CONTENT, counters, backUpTask);
		
		assertThatIllegalArgumentException().isThrownBy(() -> backUpItemGroup.addBackUpItem(backUpItem)).withMessageContaining("status");			
	}
	
	@Test
	void addBackUpItemWithDifferentActionShouldThrowException() {
		
		BackUpItemGroup backUpItemGroup =
				new BackUpItemGroup(EXISTANT_FOLDER_PATH, UNEXISTANT_FOLDER_PATH, EXISTANT_FOLDER_PATH, BackupAction.COPY_REPLACE, BackupStatus.DIFFERENT, backUpTask);
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask);
		
		assertThatIllegalArgumentException().isThrownBy(() -> backUpItemGroup.addBackUpItem(backUpItem)).withMessageContaining("action");			
	}
	
	@Test
	void addBackUpItemWithDifferentPermanenceLevelShouldThrowException() {
		
		BackUpItemGroup backUpItemGroup =
				new BackUpItemGroup(FOLDER_PATH_FROM_A_DIFFERENT_GROUP, UNEXISTANT_FOLDER_PATH, FOLDER_PATH_FROM_A_DIFFERENT_GROUP, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, backUpTask);
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask);

		assertThatIllegalArgumentException().isThrownBy(() -> backUpItemGroup.addBackUpItem(backUpItem)).withMessageContaining("permanance level");		
	}
	
	@AfterEach
	void clean() throws IOException {

		if (Files.exists(UNEXISTANT_TARGET)) {
			Files.delete(UNEXISTANT_TARGET);
		}
	}
}
