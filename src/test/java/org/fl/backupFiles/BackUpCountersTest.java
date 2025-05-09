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
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BackUpCountersTest {

	private static final Path pathForTargetFileStore = Paths.get("/");
	private static FileStore fileStore;
	
	@BeforeAll
	static void init() throws IOException {
		fileStore = Files.getFileStore(pathForTargetFileStore);
	}
	
	private static TargetFileStores newTargetFileStores() {
		TargetFileStores targetFileStores = new TargetFileStores();
		targetFileStores.addTargetFileStore(pathForTargetFileStore, 5);
		return targetFileStores;
	}
	
	@Test
	void shouldBeZeroAtCreation() {

		BackUpCounters bc = new BackUpCounters(newTargetFileStores(), null);
		assertFieldValue(bc, 0);
	}

	@Test
	void shouldBeZeroAtReset() {

		BackUpCounters bc = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		bc.reset();
		assertFieldValue(bc, 0);
		setFieldValue(bc, 12);
		assertFieldValue(bc, 12);
		bc.reset();
		assertFieldValue(bc, 0);
	}
	
	@Test
	void shouldAddCounters() {

		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);

		setFieldValue(bc1, 12);
		assertFieldValue(bc1, 12);

		setFieldValue(bc2, 11);
		assertFieldValue(bc2, 11);

		bc1.add(bc2);
		assertFieldValue(bc1, 23);
		assertFieldValue(bc2, 11);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isFalse();
	}

	@Test
	void shouldAddCountersWithIncrement() {

		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		setFieldValueWithIncrement(bc1, 12);
		assertFieldValueWithIncrement(bc1, 12, 1);

		setFieldValueWithIncrement(bc2, 11);
		assertFieldValueWithIncrement(bc2, 11, 1);

		bc1.add(bc2);
		assertFieldValueWithIncrement(bc1, 23, 2);
		assertFieldValueWithIncrement(bc2, 11, 1);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isFalse();
	}
	
	@Test
	void initialIndividualCountersShouldBeEquals() {
		assertThat(new BackUpCounters(newTargetFileStores(), OperationType.BACKUP).equalsIndividualCounters( new BackUpCounters(newTargetFileStores(), OperationType.BACKUP))).isTrue();
	}
	
	@Test
	void individualCountersShouldBeEquals() {
		
		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		setFieldValueWithIncrement(bc1, 12);
		setFieldValueWithIncrement(bc2, 12);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();
		assertThat(bc2.equalsIndividualCounters(bc1)).isTrue();
		assertThat(bc1.equalsIndividualCounters(bc1)).isTrue();
	}
	
	@Test
	void individualCountersShouldBeEquals2() {
		
		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		setFieldValueWithIncrement(bc1, 100);
		setFieldValueWithIncrement(bc2, 100);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();
		
		bc1.nbHighPermanencePath++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();
		bc1.nbMediumPermanencePath++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();
		bc1.nbSourceFilesFailed++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();
		bc1.nbSourceFilesProcessed++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();
		bc1.nbTargetFilesFailed++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();
		bc1.nbTargetFilesProcessed++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();
	}
	
	@Test
	void individualCountersShouldNotBeEquals() {
		
		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		setFieldValueWithIncrement(bc1, 12);
		setFieldValueWithIncrement(bc2, 12);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();

		bc1.copyNewNb++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isFalse();
	}
	
	@Test
	void individualCountersShouldNotBeEquals2() {
		
		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		setFieldValueWithIncrement(bc1, 12);
		setFieldValueWithIncrement(bc2, 12);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();

		bc1.copyTreeNb++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isFalse();
	}
	
	@Test
	void individualCountersShouldNotBeEquals3() {
		
		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		setFieldValueWithIncrement(bc1, 12);
		setFieldValueWithIncrement(bc2, 12);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();

		bc1.copyReplaceNb++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isFalse();
	}
	
	@Test
	void individualCountersShouldNotBeEquals4() {
		
		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		setFieldValueWithIncrement(bc1, 12);
		setFieldValueWithIncrement(bc2, 12);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();

		bc1.copyTargetNb++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isFalse();
	}
	
	@Test
	void individualCountersShouldNotBeEquals5() {
		
		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		setFieldValueWithIncrement(bc1, 12);
		setFieldValueWithIncrement(bc2, 12);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();

		bc1.deleteDirNb++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isFalse();
	}
	
	@Test
	void individualCountersShouldNotBeEquals6() {
		
		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		setFieldValueWithIncrement(bc1, 12);
		setFieldValueWithIncrement(bc2, 12);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();

		bc1.deleteNb++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isFalse();
	}
	
	@Test
	void individualCountersShouldNotBeEquals7() {
		
		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		setFieldValueWithIncrement(bc1, 12);
		setFieldValueWithIncrement(bc2, 12);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();

		bc1.adjustTimeNb++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isFalse();
	}
	
	@Test
	void individualCountersShouldNotBeEquals8() {
		
		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		setFieldValueWithIncrement(bc1, 12);
		setFieldValueWithIncrement(bc2, 12);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();

		bc1.ambiguousNb++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isFalse();
	}
	
	@Test
	void individualCountersShouldNotBeEquals9() {
		
		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		setFieldValueWithIncrement(bc1, 12);
		setFieldValueWithIncrement(bc2, 12);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();

		bc1.backupWithSizeAboveThreshold++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isFalse();
	}
	
	@Test
	void individualCountersShouldNotBeEquals10() {
		
		BackUpCounters bc1 = new BackUpCounters(newTargetFileStores(), OperationType.SCAN);
		BackUpCounters bc2 = new BackUpCounters(newTargetFileStores(), OperationType.BACKUP);

		setFieldValueWithIncrement(bc1, 12);
		setFieldValueWithIncrement(bc2, 12);
		
		assertThat(bc1.equalsIndividualCounters(bc2)).isTrue();

		bc1.contentDifferentNb++;
		assertThat(bc1.equalsIndividualCounters(bc2)).isFalse();
	}
	
	private static void assertFieldValue(BackUpCounters bc, long val) {
		
		assertThat(bc.ambiguousNb).isEqualTo(val);
		assertThat(bc.backupWithSizeAboveThreshold).isEqualTo(val);
		assertThat(bc.contentDifferentNb).isEqualTo(val);
		assertThat(bc.copyNewNb).isEqualTo(val);
		assertThat(bc.copyReplaceNb).isEqualTo(val);
		assertThat(bc.copyTreeNb).isEqualTo(val);
		assertThat(bc.deleteDirNb).isEqualTo(val);
		assertThat(bc.deleteNb).isEqualTo(val);
		assertThat(bc.nbHighPermanencePath).isEqualTo(val);
		assertThat(bc.nbMediumPermanencePath).isEqualTo(val);
		assertThat(bc.nbSourceFilesFailed).isEqualTo(val);
		assertThat(bc.nbSourceFilesProcessed).isEqualTo(val);
		assertThat(bc.nbTargetFilesFailed).isEqualTo(val);
		assertThat(bc.nbTargetFilesProcessed).isEqualTo(val);
		assertThat(bc.copyTargetNb).isEqualTo(val);
		assertThat(bc.getTargetFileStores().getPotentialSizeChange(fileStore)).isNotNull().isEqualTo(val);
	}

	private static void assertFieldValueWithIncrement(BackUpCounters bc, long val, long m) {
		
		assertThat(bc.ambiguousNb).isEqualTo(val);
		assertThat(bc.backupWithSizeAboveThreshold).isEqualTo(val+1*m);
		assertThat(bc.contentDifferentNb).isEqualTo(val+2*m);
		assertThat(bc.copyNewNb).isEqualTo(val+3*m);
		assertThat(bc.copyReplaceNb).isEqualTo(val+4*m);
		assertThat(bc.copyTreeNb).isEqualTo(val+5*m);
		assertThat(bc.deleteDirNb).isEqualTo(val+6*m);
		assertThat(bc.deleteNb).isEqualTo(val+7*m);
		assertThat(bc.nbHighPermanencePath).isEqualTo(val+8*m);
		assertThat(bc.nbMediumPermanencePath).isEqualTo(val+9*m);
		assertThat(bc.nbSourceFilesFailed).isEqualTo(val+10*m);
		assertThat(bc.nbSourceFilesProcessed).isEqualTo(val+11*m);
		assertThat(bc.nbTargetFilesFailed).isEqualTo(val+12*m);
		assertThat(bc.nbTargetFilesProcessed).isEqualTo(val+13*m);
		assertThat(bc.copyTargetNb).isEqualTo(val+14*m);
		assertThat(bc.getTargetFileStores().getPotentialSizeChange(fileStore)).isNotNull().isEqualTo(val + 15*m);
	}
	
	private static void setFieldValue(BackUpCounters bc, long val) {

		bc.ambiguousNb = val;
		bc.backupWithSizeAboveThreshold = val;
		bc.contentDifferentNb = val;
		bc.copyNewNb = val;
		bc.copyReplaceNb = val;
		bc.copyTreeNb = val;
		bc.deleteDirNb = val;
		bc.deleteNb = val;
		bc.nbHighPermanencePath = val;
		bc.nbMediumPermanencePath = val;
		bc.nbSourceFilesFailed = val;
		bc.nbSourceFilesProcessed = val;
		bc.nbTargetFilesFailed = val;
		bc.nbTargetFilesProcessed = val;
		bc.copyTargetNb = val;
		bc.recordPotentialSizeChange(fileStore, val);
	}

	private static void setFieldValueWithIncrement(BackUpCounters bc, long val) {

		bc.ambiguousNb = val;
		bc.backupWithSizeAboveThreshold = val + 1;
		bc.contentDifferentNb = val + 2;
		bc.copyNewNb = val + 3;
		bc.copyReplaceNb = val + 4;
		bc.copyTreeNb = val + 5;
		bc.deleteDirNb = val + 6;
		bc.deleteNb = val + 7;
		bc.nbHighPermanencePath = val + 8;
		bc.nbMediumPermanencePath = val + 9;
		bc.nbSourceFilesFailed = val + 10;
		bc.nbSourceFilesProcessed = val + 11;
		bc.nbTargetFilesFailed = val + 12;
		bc.nbTargetFilesProcessed = val + 13;
		bc.copyTargetNb = val + 14;
		bc.recordPotentialSizeChange(fileStore, val + 15);
	}
}
