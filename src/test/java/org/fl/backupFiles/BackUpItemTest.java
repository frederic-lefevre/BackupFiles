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

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.logging.Logger;

import org.fl.backupFiles.directoryGroup.DirectoryGroupConfiguration;
import org.fl.backupFiles.directoryGroup.DirectoryGroupMap;
import org.fl.backupFiles.directoryGroup.DirectoryPermanenceLevel;
import org.fl.backupFiles.directoryGroup.GroupPolicy;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroup;
import org.fl.backupFiles.scanner.PathPairBasicAttributes;
import org.fl.util.file.FileComparator;
import org.fl.util.file.FilesUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.*;

public class BackUpItemTest {

	private static final String DEFAULT_PROP_FILE = "file:///ForTests/BackUpFiles/backupFiles.properties";

	private static final Path pathForTargetFileStore = Paths.get("/");
	
	private static Logger log = Logger.getLogger(BackUpItemTest.class.getName());

	private static final String SRC_FOLDER = "file:///ForTests/BackUpFiles/TestDir1/";
	private static final String SRC_FILE1 = SRC_FOLDER + "File1.pdf";
	private static final String TGT_FILE1 = "file:///ForTests/BackUpFiles/TestDir2/File1.pdf";
	private static final String UNEXISTANT_FILE = "file:///ForTests/BackUpFiles/TestDir1/doesNotExists.pdf";
	private static final String UNEXISTANT_FOLDER = "file:///ForTests/BackUpFiles/doesNotExists";

	private static Path EXISTANT_SOURCE_FOLDER;
	private static Path EXISTANT_SOURCE;
	private static Path UNEXISTANT_TARGET;
	private static Path UNEXISTANT_PATH;
	private static Path UNEXISTANT_FOLDER_PATH;

	private static BackUpTask backUpTask;
	
	@BeforeAll
	static void initConfig() throws IOException, URISyntaxException {

		EXISTANT_SOURCE_FOLDER = FilesUtils.uriStringToAbsolutePath(SRC_FOLDER);
		EXISTANT_SOURCE = FilesUtils.uriStringToAbsolutePath(SRC_FILE1);
		UNEXISTANT_TARGET = FilesUtils.uriStringToAbsolutePath(TGT_FILE1);
		UNEXISTANT_PATH = FilesUtils.uriStringToAbsolutePath(UNEXISTANT_FILE);
		UNEXISTANT_FOLDER_PATH = FilesUtils.uriStringToAbsolutePath(UNEXISTANT_FOLDER);
		Path sourcePathForDirectoryMap = FilesUtils.uriStringToAbsolutePath("file:///ForTests/BackUpFiles/TestDir1/");
		Config.initConfig(DEFAULT_PROP_FILE);
		DirectoryGroupConfiguration directoryGroupConfiguration = new DirectoryGroupConfiguration(Config.getBackupGroupConfiguration());
		backUpTask = new BackUpTask(EXISTANT_SOURCE_FOLDER, EXISTANT_SOURCE_FOLDER, 
				new DirectoryGroupMap(sourcePathForDirectoryMap, sourcePathForDirectoryMap, directoryGroupConfiguration), 0);
	}

	private static TargetFileStores newTargetFileStores() {
		TargetFileStores targetFileStores = new TargetFileStores();
		targetFileStores.addTargetFileStore(pathForTargetFileStore, 5);
		return targetFileStores;
	}
	
	@Test
	void test1() {

		Path src = Paths.get("");
		Path tgt = Paths.get("");
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(src, tgt);

		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_REPLACE, BackupStatus.DIFFERENT, counters, backUpTask);

		BackupAction action = backUpItem.getBackupAction();

		assertThat(action).isEqualTo(BackupAction.COPY_REPLACE);
		assertThat(backUpItem.getBackupStatus()).isEqualTo(BackupStatus.DIFFERENT);
	}

	@Test
	void test2() {

		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask);

		assertThat(getTotalCounters(counters)).isEqualTo(1);

		counters.reset();
		boolean result1 = backUpItem.execute(counters);
		assertThat(result1).isTrue();
		assertThat(backUpItem.getBackupStatus()).isEqualTo(BackupStatus.DONE);

		FileComparator fileComparator = new FileComparator(log);

		assertThat(fileComparator.haveSameContent(EXISTANT_SOURCE, UNEXISTANT_TARGET)).isTrue();

		assertThat(counters.nbSourceFilesProcessed).isEqualTo(1);
		assertThat(counters.copyNewNb).isEqualTo(1);
		assertThat(getTotalCounters(counters)).isEqualTo(2);

		counters.reset();
		pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE, pathPairBasicAttributes, counters, backUpTask);

		assertThat(counters.copyNewNb).isZero();
		assertThat(counters.nbSourceFilesProcessed).isZero();
		assertThat(counters.nbTargetFilesProcessed).isZero();
		assertThat(counters.deleteNb).isEqualTo(1);
		assertThat(getTotalCounters(counters)).isEqualTo(1);

		counters.reset();
		boolean result2 = backUpItem.execute(counters);
		assertThat(result2).isTrue();
		assertThat(backUpItem.getBackupStatus()).isEqualTo(BackupStatus.DONE);

		assertThat(Files.exists(UNEXISTANT_TARGET)).isFalse();
		assertThat(counters.nbSourceFilesProcessed).isZero();
		assertThat(counters.deleteNb).isEqualTo(1);
		assertThat(counters.nbTargetFilesProcessed).isEqualTo(1);
		assertThat(getTotalCounters(counters)).isEqualTo(2);
	}


	@Test
	void testItemDirectoryGroup() {
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask);
		
		DirectoryGroup directoryGroup = backUpItem.getDirectoryGroup();
		
		assertThat(directoryGroup).isNotNull();
		assertThat(directoryGroup.getGroupPolicy()).isEqualTo(GroupPolicy.GROUP_ALL);
		assertThat(directoryGroup.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.MEDIUM);
		assertThat(backUpItem.getSourcePath().startsWith(directoryGroup.getPath()));
	}
	
	@Test
	void sumIndivualCounterTest() {
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask);
		
		BackUpCounters sumCounters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		
		backUpItem.sumIndividualCounters(sumCounters);
		
		assertThat(sumCounters.equalsIndividualCounters(counters)).isTrue();
	}
	
	@Test
	void sumIndivualCounterTest2() {
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask);
		
		PathPairBasicAttributes pathPairBasicAttributes2 = new PathPairBasicAttributes(EXISTANT_SOURCE, EXISTANT_SOURCE);
		BackUpCounters counters2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem2 = new BackUpItem(pathPairBasicAttributes2, BackupAction.COPY_REPLACE, BackupStatus.DIFFERENT, counters2, backUpTask);
		
		BackUpCounters sumCounters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		
		backUpItem.sumIndividualCounters(sumCounters);
		backUpItem2.sumIndividualCounters(sumCounters);
		
		assertThat(sumCounters.equalsIndividualCounters(counters)).isFalse();
		
		counters.add(counters2);
		assertThat(sumCounters.equalsIndividualCounters(counters)).isTrue();
	}
	
	@Test
	void testSourcePresentTargetNot() {
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask);
		
		assertThat(backUpItem.isSourcePresent()).isTrue();
		assertThat(pathPairBasicAttributes.sourceExists()).isTrue();
		
		assertThat(backUpItem.isTargetPresent()).isFalse();
		assertThat(pathPairBasicAttributes.targetExists()).isFalse();
		assertThat(pathPairBasicAttributes.noTargetPath()).isFalse();
	}
	
	@Test
	void testSourcePresentTargetNull() {
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, null);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask);
		
		assertThat(backUpItem.isSourcePresent()).isTrue();
		assertThat(pathPairBasicAttributes.sourceExists()).isTrue();
		
		assertThat(backUpItem.isTargetPresent()).isFalse();
		assertThat(pathPairBasicAttributes.targetExists()).isFalse();
		assertThat(pathPairBasicAttributes.noTargetPath()).isTrue();
	}
	
	@Test
	void testTargetPresentSourceNot() {
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(UNEXISTANT_TARGET, EXISTANT_SOURCE);
		PathPairBasicAttributes pathPairBasicAttributes2 = new PathPairBasicAttributes(EXISTANT_SOURCE_FOLDER, null);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE, pathPairBasicAttributes2, counters, backUpTask);
		
		assertThat(backUpItem.isSourcePresent()).isFalse();
		assertThat(pathPairBasicAttributes.sourceExists()).isFalse();
		
		assertThat(backUpItem.isTargetPresent()).isTrue();
		assertThat(pathPairBasicAttributes.targetExists()).isTrue();
		assertThat(pathPairBasicAttributes.noTargetPath()).isFalse();
	}
	
	@Test
	void testTargetPresentSourceNull() {
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(null, EXISTANT_SOURCE);
		PathPairBasicAttributes pathPairBasicAttributes2 = new PathPairBasicAttributes(EXISTANT_SOURCE_FOLDER, null);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE, pathPairBasicAttributes2, counters, backUpTask);
		
		assertThat(backUpItem.isSourcePresent()).isFalse();
		assertThat(pathPairBasicAttributes.sourceExists()).isFalse();
		
		assertThat(backUpItem.isTargetPresent()).isTrue();
		assertThat(pathPairBasicAttributes.targetExists()).isTrue();
		assertThat(pathPairBasicAttributes.noTargetPath()).isFalse();
	}
	
	@Test
	void testTargetAndSourcePresent() {
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, EXISTANT_SOURCE);
		PathPairBasicAttributes pathPairBasicAttributes2 = new PathPairBasicAttributes(EXISTANT_SOURCE_FOLDER, null);
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE, pathPairBasicAttributes2, counters, backUpTask);
		
		assertThat(backUpItem.isSourcePresent()).isTrue();
		assertThat(pathPairBasicAttributes.sourceExists()).isTrue();
		
		assertThat(backUpItem.isTargetPresent()).isTrue();
		assertThat(pathPairBasicAttributes.targetExists()).isTrue();
		assertThat(pathPairBasicAttributes.noTargetPath()).isFalse();
	}
	
	@Test
	void nullSrcShouldThrowException() {

		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(null, UNEXISTANT_TARGET);
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);

		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_REPLACE, BackupStatus.DIFFERENT, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_TREE, BackupStatus.DIFFERENT, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.AMBIGUOUS, BackupStatus.SAME_CONTENT, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_TARGET, BackupStatus.SAME_CONTENT, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.ADJUST_TIME, BackupStatus.SAME_CONTENT, counters, backUpTask));
	}

	@Test

	void illegalCopyShouldThrowException() {

		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(null, EXISTANT_SOURCE);
		PathPairBasicAttributes pathPairBasicAttributes2 = new PathPairBasicAttributes(EXISTANT_SOURCE_FOLDER, null);
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);

		assertThatExceptionOfType(IllegalBackupActionException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, pathPairBasicAttributes2, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackupActionException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_REPLACE, pathPairBasicAttributes2, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackupActionException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_TREE, pathPairBasicAttributes2, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackupActionException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.AMBIGUOUS, pathPairBasicAttributes2, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackupActionException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_TARGET, pathPairBasicAttributes2, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackupActionException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.ADJUST_TIME, pathPairBasicAttributes2, counters, backUpTask));
	}

	@Test
	void illegalDeleteShouldThrowException() {

		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);

		assertThatExceptionOfType(IllegalBackupActionException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE, BackupStatus.DIFFERENT, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackupActionException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE_DIR, BackupStatus.DIFFERENT, counters, backUpTask));
	}

	@Test
	void unexistantSrcShouldThrowException() {

		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(UNEXISTANT_PATH, UNEXISTANT_TARGET);
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);

		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_REPLACE, BackupStatus.DIFFERENT, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_TREE, BackupStatus.DIFFERENT, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.AMBIGUOUS, BackupStatus.DIFFERENT, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_TARGET, BackupStatus.DIFFERENT, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.ADJUST_TIME, BackupStatus.DIFFERENT, counters, backUpTask));
	}

	@Test
	void unexistantExistingSrcShouldThrowException() throws IOException {

		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(null, EXISTANT_SOURCE);
		PathPairBasicAttributes pathPairBasicAttributes2 = new PathPairBasicAttributes(UNEXISTANT_FOLDER_PATH, null);
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE, pathPairBasicAttributes2, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE_DIR, pathPairBasicAttributes2, counters, backUpTask));
	}

	@Test
	void unexistantTargetShouldThrowException() throws IOException {

		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(null, UNEXISTANT_PATH);
		PathPairBasicAttributes pathPairBasicAttributes2 = new PathPairBasicAttributes(EXISTANT_SOURCE_FOLDER, null);
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE, pathPairBasicAttributes2, counters, backUpTask));
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE_DIR, pathPairBasicAttributes2, counters, backUpTask));
	}
	
	@Test
	void existantTgtShouldThrowException() throws IOException {

		Files.copy(EXISTANT_SOURCE, UNEXISTANT_TARGET);
		Path nowExists = UNEXISTANT_TARGET;
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, nowExists);

		assertThat(Files.exists(EXISTANT_SOURCE)).isTrue();
		assertThat(Files.exists(nowExists)).isTrue();

		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		assertThatExceptionOfType(IllegalBackUpItemException.class)
			.isThrownBy(() -> new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, counters, backUpTask));
	}

	@Test
	void shouldCopyTarget() throws IOException {

		Files.copy(EXISTANT_SOURCE, UNEXISTANT_TARGET);

		Path nowExists = UNEXISTANT_TARGET;
		assertThat(Files.exists(EXISTANT_SOURCE)).isTrue();
		assertThat(Files.exists(nowExists)).isTrue();

		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_SOURCE, nowExists);
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_TARGET, BackupStatus.SAME_CONTENT, counters, backUpTask);

		assertThat(counters.copyTargetNb).isEqualTo(1);

		counters.reset();
		boolean result = backUpItem.execute(counters);
		assertThat(result).isTrue();

		assertThat(counters.copyTargetNb).isEqualTo(1);
		assertThat(getTotalCounters(counters)).isEqualTo(2);
	}

	@Test
	void shouldAdjustTargetTime() throws IOException, URISyntaxException {

		boolean debug = false;
		boolean testWithExternalDrive = false;
		
		Path SOURCE_FILE;
		Path TARGET_FILE;
		
		if (testWithExternalDrive) {
			SOURCE_FILE = Paths.get(new java.net.URI("file:///E:/Musique/a/Aerosmith/Rocks/01.Back%20in%20the%20Saddle.flac"));
			TARGET_FILE = Paths.get(new java.net.URI("file:///I:/Musique/a/Aerosmith/Rocks/01.Back%20in%20the%20Saddle.flac"));		
			org.fl.util.file.FilesSecurityUtils.setWritable(TARGET_FILE, SOURCE_FILE.getParent());
		} else {
			SOURCE_FILE = EXISTANT_SOURCE;
			TARGET_FILE = UNEXISTANT_TARGET;
		}		
				
		if (!Files.exists(TARGET_FILE)) {
			Files.copy(SOURCE_FILE, TARGET_FILE);
		}

		assertThat(Files.exists(SOURCE_FILE)).isTrue();
		assertThat(Files.exists(TARGET_FILE)).isTrue();

		// Change target file last modified time 
		FileTime now = FileTime.from(Instant.now());
		Files.setLastModifiedTime(TARGET_FILE, now);
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(SOURCE_FILE, TARGET_FILE);
		if (debug) {
			System.out.println("1-FileTime now to millis=" + now.toMillis());
			System.out.println("1-Source attribute to millis=" + pathPairBasicAttributes.getSourceBasicAttributes().lastModifiedTime().toMillis());
			System.out.println("1-Target attribute to millis=" + pathPairBasicAttributes.getTargetBasicAttributes().lastModifiedTime().toMillis());
		}
		
		assertThat(pathPairBasicAttributes.getTargetBasicAttributes().lastModifiedTime().compareTo(now)).isZero();
		assertThat(pathPairBasicAttributes.getTargetBasicAttributes().lastModifiedTime().toMillis())
			.isEqualTo(now.toMillis());
		assertThat(pathPairBasicAttributes.getTargetBasicAttributes().lastModifiedTime()
				.compareTo(pathPairBasicAttributes.getSourceBasicAttributes().lastModifiedTime())).isEqualTo(1);
		if (debug) {
			System.out.println("2-Source attribute to millis=" + pathPairBasicAttributes.getSourceBasicAttributes().lastModifiedTime().toMillis());
			System.out.println("2-Target attribute now to millis=" + pathPairBasicAttributes.getTargetBasicAttributes().lastModifiedTime().toMillis());
			System.out.println("2-FileTime now to millis=" + now.toMillis());
		}
		
		BackUpCounters counters = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		BackUpItem backUpItem = new BackUpItem(pathPairBasicAttributes, BackupAction.ADJUST_TIME, BackupStatus.SAME_CONTENT, counters, backUpTask);

		assertThat(counters.adjustTimeNb).isEqualTo(1);

		counters.reset();
		boolean result = backUpItem.execute(counters);
		assertThat(result).isTrue();

		// Check with Files.getLastModifiedTime
		assertThat(Files.getLastModifiedTime(TARGET_FILE)
			.compareTo(pathPairBasicAttributes.getSourceBasicAttributes().lastModifiedTime())).isZero();
		
		// Check reading again with PathPairBasicAttributes
		PathPairBasicAttributes pathPairBasicAttributes2 = new PathPairBasicAttributes(SOURCE_FILE, TARGET_FILE);
		if (debug) {
			System.out.println("3-Source attribute to millis=" + pathPairBasicAttributes2.getSourceBasicAttributes().lastModifiedTime().toMillis());
			System.out.println("3-Target attribute now to millis=" + pathPairBasicAttributes2.getTargetBasicAttributes().lastModifiedTime().toMillis());
		}
		
		assertThat(pathPairBasicAttributes2.getTargetBasicAttributes().lastModifiedTime()
				.compareTo(pathPairBasicAttributes2.getSourceBasicAttributes().lastModifiedTime())).isZero();
		
		assertThat(pathPairBasicAttributes2.getTargetBasicAttributes().lastModifiedTime().toMillis())
			.isEqualTo(pathPairBasicAttributes2.getSourceBasicAttributes().lastModifiedTime().toMillis());
		
		assertThat(counters.adjustTimeNb).isEqualTo(1);
		assertThat(getTotalCounters(counters)).isEqualTo(2);
	}
	
	@AfterEach
	void clean() throws IOException {

		if (Files.exists(UNEXISTANT_TARGET)) {
			Files.delete(UNEXISTANT_TARGET);
		}
	}

	private long getTotalCounters(BackUpCounters counters) {

		return counters.ambiguousNb + counters.contentDifferentNb + counters.copyNewNb + counters.copyReplaceNb
				+ counters.copyTreeNb + counters.deleteDirNb + counters.deleteNb + counters.copyTargetNb + counters.adjustTimeNb
				+ counters.nbSourceFilesFailed + counters.nbSourceFilesProcessed + counters.nbTargetFilesFailed
				+ counters.nbTargetFilesProcessed;
	}
}
