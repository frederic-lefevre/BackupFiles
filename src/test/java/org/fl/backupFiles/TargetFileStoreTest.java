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

import org.junit.jupiter.api.Test;

public class TargetFileStoreTest {

	private static final Path pathForTargetFileStore = Paths.get("/");
	
	@Test
	void targetFileStoreCreationTest() throws IOException {
		
		TargetFileStores targetFileStores = new TargetFileStores();
		TargetFileStore targetFileStore = targetFileStores.addTargetFileStore(pathForTargetFileStore);
		
		assertThat(targetFileStore).isNotNull();
		
		assertThat(targetFileStore.getPotentialSizeChange()).isZero();
		assertThat(targetFileStore.getFileStore()).isEqualTo(Files.getFileStore(pathForTargetFileStore));
		
		StringBuilder spaceEvolutionString = new StringBuilder();
		
		targetFileStore.getSpaceEvolution(spaceEvolutionString);
		
		assertThat(spaceEvolutionString).isNotEmpty();
		
		final long sizeDifference = 100;
		long newPotentialSizeChange = targetFileStore.recordPotentialSizeChange(sizeDifference);
		
		assertThat(newPotentialSizeChange).isEqualTo(sizeDifference);
		
		final long sizeDifference2 = 105;
		
		assertThat(targetFileStore.recordPotentialSizeChange(sizeDifference2)).isEqualTo(sizeDifference + sizeDifference2);
	}

	
	@Test
	void targetFileStoreResetTest() throws IOException {
		
		TargetFileStores targetFileStores = new TargetFileStores();
		TargetFileStore targetFileStore = targetFileStores.addTargetFileStore(pathForTargetFileStore);
		
		assertThat(targetFileStore.getPotentialSizeChange()).isZero();
		
		final long sizeDifference = 100;
		long newPotentialSizeChange = targetFileStore.recordPotentialSizeChange(sizeDifference);
		
		assertThat(newPotentialSizeChange).isEqualTo(sizeDifference).isEqualTo(targetFileStore.getPotentialSizeChange());
		
		targetFileStore.reset();
		assertThat(targetFileStore.getPotentialSizeChange()).isZero();
	}
}
