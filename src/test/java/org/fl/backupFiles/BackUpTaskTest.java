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
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

import org.fl.backupFiles.directoryGroup.DirectoryGroupMap;
import org.fl.backupFiles.directoryGroup.DirectoryPermanenceLevel;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroup;
import org.fl.util.file.FilesUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BackUpTaskTest {

	private static final Logger logger = Logger.getLogger(BackUpTaskTest.class.getName());
	
	private static final String DEFAULT_PROP_FILE = "file:///ForTests/BackUpFiles/backupFiles.properties";
	
	private static DirectoryGroupMap directoryGroupMap;
	
	@BeforeAll
	static void initConfig() throws IOException {

		Path sourcePathForDirectoryMap = TestUtils.getPathFromUriString("file:///C:/ForTests/BackUpFiles/TestDir1/");
		Config.initConfig(DEFAULT_PROP_FILE);
		directoryGroupMap = new DirectoryGroupMap(sourcePathForDirectoryMap, sourcePathForDirectoryMap, Config.getBackupGroupConfiguration());
	}
	
	@Test
	void test1() throws IOException {
		
		final String SRC_FILE1 = "file:///C:/ForTests/BackUpFiles/TestDir1/File1.pdf";
		final String TGT_FILE1 = "file:///C:/ForTests/BackUpFiles/TestDir2/File1.pdf";

		Path src = TestUtils.getPathFromUriString(SRC_FILE1);
		Path tgt = TestUtils.getPathFromUriString(TGT_FILE1);

		BackUpTask backUpTask = new BackUpTask(src, tgt, directoryGroupMap, 0);

		assertThat(backUpTask.compareContent()).isFalse();
		assertThat(backUpTask.compareContentOnAmbiguous()).isTrue();

		BackUpTask backUpTask2 = new BackUpTask(src, tgt, directoryGroupMap, 0);

		assertThat(backUpTask).isEqualTo(backUpTask2);
	}

	@Test
	void test2() throws IOException {
		
		final String SRC_FILE1 = "file:///C:/ForTests/BackUpFiles/TestDir1/File1.pdf";
		final String TGT_FILE1 = "file:///C:/ForTests/BackUpFiles/TestDir2/File1.pdf";

		Path src = TestUtils.getPathFromUriString(SRC_FILE1);
		Path tgt = TestUtils.getPathFromUriString(TGT_FILE1);

		BackUpTask backUpTask = new BackUpTask(src, tgt, directoryGroupMap, 0);
		BackUpTask backUpTask2 = new BackUpTask(src, tgt, directoryGroupMap, 1);

		assertThat(backUpTask).isEqualTo(backUpTask2);
	}	

	@Test
	void test3() throws IOException {
		
		final String SRC_FILE1 = "file://C://ForTests/BackUpFiles/TestDir1/File1.pdf";
		Path src = TestUtils.getPathFromUriString(SRC_FILE1);

		assertThatIllegalArgumentException().isThrownBy(() -> new BackUpTask(src, null, directoryGroupMap, 0));
	}
	
	@Test
	void test4() throws IOException {

		final String TGT_FILE1 = "file:///C:/ForTests/BackUpFiles/TestDir2/File1.pdf";
		Path tgt = TestUtils.getPathFromUriString(TGT_FILE1);

		assertThatIllegalArgumentException().isThrownBy(() -> new BackUpTask(null, tgt, directoryGroupMap, 0));
	}
	
	@Test
	void test5() throws IOException {
		
		assertThatIllegalArgumentException().isThrownBy(() -> new BackUpTask(null, null, directoryGroupMap, 0));
	}
	
	@Test
	void test6() throws IOException {
		
		final String SRC_FILE1 = "file:///C:/ForTests/BackUpFiles/TestDir1/File1.pdf";
		final String TGT_FILE1 = "file:///C:/ForTests/BackUpFiles/TestDir2/File1.pdf";
		final String SRC_FILE2 = "file:///C:/ForTests/BackUpFiles/TestDir1/File2.pdf";

		Path src = TestUtils.getPathFromUriString(SRC_FILE1);
		Path tgt = TestUtils.getPathFromUriString(TGT_FILE1);
		Path src2 = TestUtils.getPathFromUriString(SRC_FILE2);

		BackUpTask backUpTask = new BackUpTask(src, tgt, directoryGroupMap, 0);
		BackUpTask backUpTask2 = new BackUpTask(src2, tgt, directoryGroupMap, 0);

		assertThat(backUpTask).isNotEqualTo(backUpTask2);
	}
	
	@Test
	void test7() throws IOException {
		
		final String SRC_FILE1 = "file:///C:/ForTests/BackUpFiles/TestDir1/File1.pdf";
		final String TGT_FILE1 = "file:///C:/ForTests/BackUpFiles/TestDir2/File1.pdf";
		final String TGT_FILE2 = "file:///C:/ForTests/BackUpFiles/TestDir1/File2.pdf";

		Path src = TestUtils.getPathFromUriString(SRC_FILE1);
		Path tgt = TestUtils.getPathFromUriString(TGT_FILE1);
		Path tgt2 = TestUtils.getPathFromUriString(TGT_FILE2);

		BackUpTask backUpTask = new BackUpTask(src, tgt, directoryGroupMap, 0);
		BackUpTask backUpTask2 = new BackUpTask(src, tgt2, directoryGroupMap, 0);

		assertThat(backUpTask).isNotEqualTo(backUpTask2);
	}
	
	@Test
	void testFileStore() throws IOException {
		
		final String SRC_FILE1 = "file:///C:/ForTests/BackUpFiles/TestDir1/File1.pdf";
		final String TGT_FILE1 = "file:///C:/ForTests/BackUpFiles/TestDir2/File1.pdf";

		Path src = TestUtils.getPathFromUriString(SRC_FILE1);
		Path tgt = TestUtils.getPathFromUriString(TGT_FILE1);

		BackUpTask backUpTask = new BackUpTask(src, tgt, directoryGroupMap, 0);
		
		FileStore fileStore = backUpTask.getTargetFileStore();
		assertThat(fileStore).isNotNull().isEqualTo(Files.getFileStore(src)).isEqualTo(FilesUtils.findFileStore(tgt, logger));
	}
	
	@Test
	void testDirectoryGroupMap() throws IOException {
		
		final String SRC_FILE1 = "file:///C:/ForTests/BackUpFiles/TestDir1/File1.pdf";
		final String TGT_FILE1 = "file:///C:/ForTests/BackUpFiles/TestDir2/File1.pdf";

		Path src = TestUtils.getPathFromUriString(SRC_FILE1);
		Path tgt = TestUtils.getPathFromUriString(TGT_FILE1);

		BackUpTask backUpTask = new BackUpTask(src, tgt, directoryGroupMap, 0);
		
		DirectoryGroupMap directoryGroupmMap = backUpTask.getDirectoryGroupMap();
		assertThat(directoryGroupmMap).isNotNull();
		DirectoryGroup directoryGroup = directoryGroupmMap.getDirectoryGroup(src);
		assertThat(directoryGroup).isNotNull();
		assertThat(directoryGroup.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.MEDIUM);
	}
}
