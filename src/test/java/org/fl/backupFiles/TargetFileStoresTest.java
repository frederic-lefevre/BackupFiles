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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.util.FilterCounter;
import org.fl.util.FilterCounter.LogRecordCounter;
import org.junit.jupiter.api.Test;

public class TargetFileStoresTest {

	@Test
	void nullPathShouldReturnNullTargetFileStore() {

		TargetFileStores targetFileStores = new TargetFileStores();
		TargetFileStore targetFileStore = targetFileStores.addTargetFileStore(null, 5);
		assertThat(targetFileStore).isNull();
	}

	@Test
	void unexistantPathShouldReturnTargetFileStore() {

		Path path1 = TestUtils.getPathFromUriString( "file:///ForTests/BackUpFiles/doesNotExists");
		Path path2 = TestUtils.getPathFromUriString( "file:///ForTests");
		TargetFileStores targetFileStores = new TargetFileStores();
		TargetFileStore targetFileStore = targetFileStores.addTargetFileStore(path1, 5);
		TargetFileStore targetFileStore2 = targetFileStores.addTargetFileStore(path2, 5);
		assertThat(targetFileStore).isNotNull().isEqualTo(targetFileStore2);
	}

	
	@Test
	void twoPathInSameFileStoreShouldReturnSameFileStore() {

		Path path1 = TestUtils.getPathFromUriString( "file:///ForTests/BackUpFiles/TestDir1/File1.pdf");
		Path path2 = TestUtils.getPathFromUriString("file:///ForTests/BackUpFiles/backupFiles.properties");
		
		TargetFileStores targetFileStores = new TargetFileStores();
		TargetFileStore targetFileStore1 = targetFileStores.addTargetFileStore(path1, 5);
		TargetFileStore targetFileStore2 = targetFileStores.addTargetFileStore(path2, 5);
		
		assertThat(targetFileStore1).isNotNull().isEqualTo(targetFileStore2);
	}
	
	@Test
	void testCreatedTargetFileStore() throws IOException {
		
		Path pathForTargetFileStore = Paths.get("/");
		TargetFileStores targetFileStores = new TargetFileStores();
		TargetFileStore targetFileStore = targetFileStores.addTargetFileStore(pathForTargetFileStore, 5);
		
		assertThat(targetFileStore).isNotNull();
		
		assertThat(targetFileStore.getFileStore()).isEqualTo(Files.getFileStore(pathForTargetFileStore));
		assertThat(targetFileStores.getPotentialSizeChange(targetFileStore.getFileStore())).isZero();
		assertThat(targetFileStores.getTotalPotentialSizeChange()).isZero();
		
		StringBuilder spaceEvolutionString = new StringBuilder();
		
		targetFileStore.getSpaceEvolution(spaceEvolutionString);
		
		assertThat(spaceEvolutionString).isNotEmpty();
	}
	
	@Test
	void testrecordPotentialSizeChangeOfTargetFileStore() throws IOException {
		
		Path pathForTargetFileStore = Paths.get("/");
		TargetFileStores targetFileStores = new TargetFileStores();
		TargetFileStore targetFileStore = targetFileStores.addTargetFileStore(pathForTargetFileStore, 5);
		
		assertThat(targetFileStore.getFileStore()).isEqualTo(Files.getFileStore(pathForTargetFileStore));
		assertThat(targetFileStores.getPotentialSizeChange(targetFileStore.getFileStore())).isZero();
		assertThat(targetFileStores.getTotalPotentialSizeChange()).isZero();
		
		final long sizeDifference = 100;
		long newPotentialSizeChange = targetFileStores.recordPotentialSizeChange(targetFileStore.getFileStore(), sizeDifference);
		
		assertThat(newPotentialSizeChange).isEqualTo(sizeDifference);
		
		final long sizeDifference2 = 105;
		
		assertThat(targetFileStores.recordPotentialSizeChange(targetFileStore.getFileStore(), sizeDifference2)).isEqualTo(sizeDifference + sizeDifference2);
		
		assertThat(targetFileStores.getTotalPotentialSizeChange()).isEqualTo(sizeDifference + sizeDifference2);
	}
	
	@Test
	void testReset() throws IOException {
		
		Path pathForTargetFileStore = Paths.get("/");
		TargetFileStores targetFileStores = new TargetFileStores();
		TargetFileStore targetFileStore = targetFileStores.addTargetFileStore(pathForTargetFileStore, 5);
		
		assertThat(targetFileStores.getPotentialSizeChange(targetFileStore.getFileStore())).isZero();
		assertThat(targetFileStores.getTotalPotentialSizeChange()).isZero();
		
		final long sizeDifference = 100;
		long newPotentialSizeChange = targetFileStores.recordPotentialSizeChange(targetFileStore.getFileStore(), sizeDifference);	
		assertThat(newPotentialSizeChange).isEqualTo(sizeDifference);	
		assertThat(targetFileStores.getTotalPotentialSizeChange()).isEqualTo(sizeDifference );
		
		targetFileStores.reset();
		assertThat(targetFileStores.getPotentialSizeChange(targetFileStore.getFileStore())).isZero();
		assertThat(targetFileStores.getTotalPotentialSizeChange()).isZero();
	}
	
	@Test
	void testMergeTargetFileStores() throws IOException {
		
		Path pathForTargetFileStore = Paths.get("/");
		TargetFileStores targetFileStores = new TargetFileStores();
		TargetFileStore targetFileStore = targetFileStores.addTargetFileStore(pathForTargetFileStore, 5);
		
		TargetFileStores targetFileStores2 = new TargetFileStores();
		TargetFileStore targetFileStore2 = targetFileStores2.addTargetFileStore(pathForTargetFileStore, 5);
		
		assertThat(targetFileStore.getFileStore())
			.isEqualTo(targetFileStore.getFileStore())
			.isEqualTo(Files.getFileStore(pathForTargetFileStore));
		
		assertThat(targetFileStores.getPotentialSizeChange(targetFileStore.getFileStore())).isZero();
		assertThat(targetFileStores.getTotalPotentialSizeChange()).isZero();
		assertThat(targetFileStores2.getPotentialSizeChange(targetFileStore2.getFileStore())).isZero();
		assertThat(targetFileStores2.getTotalPotentialSizeChange()).isZero();
		
		final long sizeDifference = 100;
		long newPotentialSizeChange = targetFileStores.recordPotentialSizeChange(targetFileStore.getFileStore(), sizeDifference);		
		assertThat(newPotentialSizeChange).isEqualTo(sizeDifference);
		assertThat(targetFileStores.getTotalPotentialSizeChange()).isEqualTo(sizeDifference);
		
		final long sizeDifference2 = 105;		
		assertThat(targetFileStores2.recordPotentialSizeChange(targetFileStore2.getFileStore(), sizeDifference2)).isEqualTo(sizeDifference2);		
		assertThat(targetFileStores2.getTotalPotentialSizeChange()).isEqualTo(sizeDifference2);
		
		targetFileStores.mergeWith(targetFileStores2);
		assertThat(targetFileStores.getTotalPotentialSizeChange()).isEqualTo(sizeDifference + sizeDifference2);
		assertThat(targetFileStores.getPotentialSizeChange(targetFileStore.getFileStore())).isEqualTo(sizeDifference + sizeDifference2);
		
		assertThat(targetFileStores2.getPotentialSizeChange(targetFileStore2.getFileStore())).isEqualTo(sizeDifference2);		
		assertThat(targetFileStores2.getTotalPotentialSizeChange()).isEqualTo(sizeDifference2);
	}
	
	@Test
	void testNullFileStore() {
		
		Path pathForTargetFileStore = Paths.get("/");
		TargetFileStores targetFileStores = new TargetFileStores();
		targetFileStores.addTargetFileStore(pathForTargetFileStore, 5);
		
		LogRecordCounter logCounter = FilterCounter.getLogRecordCounter(Logger.getLogger(TargetFileStores.class.getName()));
		
		assertThat(targetFileStores.getPotentialSizeChange(null)).isZero();
		
		assertThat(logCounter.getLogRecordCount()).isEqualTo(1);
		assertThat(logCounter.getLogRecordCount(Level.WARNING)).isEqualTo(1);
	}
	
	@Test
	void testNullFileStore2() {
		
		Path pathForTargetFileStore = Paths.get("/");
		TargetFileStores targetFileStores = new TargetFileStores();
		targetFileStores.addTargetFileStore(pathForTargetFileStore, 5);
		
		LogRecordCounter logCounter = FilterCounter.getLogRecordCounter(Logger.getLogger(TargetFileStores.class.getName()));
		
		assertThat(targetFileStores.recordPotentialSizeChange(null, 100)).isZero();
		
		assertThat(logCounter.getLogRecordCount()).isEqualTo(1);
		assertThat(logCounter.getLogRecordCount(Level.WARNING)).isEqualTo(1);
	}
}
