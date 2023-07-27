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

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class BackUpCountersTest {

	@Test
	void shouldBeZeroAtCreation() {
		
		BackUpCounters bc = new BackUpCounters() ;	
		assertFieldValue(bc, 0) ;
	}
	
	@Test	
	void shouldBeZeroAtReset() {
		
		BackUpCounters bc = new BackUpCounters() ;
		bc.reset();
		assertFieldValue(bc, 0) ;
		setFieldValue(bc, 12);
		assertFieldValue(bc, 12) ;
		bc.reset();
		assertFieldValue(bc, 0) ;
	}
	
	@Test	
	void shouldAddCounters() {
		
		BackUpCounters bc1 = new BackUpCounters() ;
		BackUpCounters bc2 = new BackUpCounters() ;

		setFieldValue(bc1, 12);
		assertFieldValue(bc1, 12) ;
		
		setFieldValue(bc2, 11);
		assertFieldValue(bc2, 11) ;
		
		bc1.add(bc2);
		assertFieldValue(bc1, 23) ;
		assertFieldValue(bc2, 11) ;
	}
	
	@Test	
	void shouldAddCountersWithIncrement() {
		
		BackUpCounters bc1 = new BackUpCounters() ;
		BackUpCounters bc2 = new BackUpCounters() ;

		setFieldValueWithIncrement(bc1, 12);
		assertFieldValueWithIncrement(bc1, 12, 1) ;
		
		setFieldValueWithIncrement(bc2, 11);
		assertFieldValueWithIncrement(bc2, 11, 1) ;
		
		bc1.add(bc2);
		assertFieldValueWithIncrement(bc1, 23, 2) ;
		assertFieldValueWithIncrement(bc2, 11, 1) ;
	}
	
	private static void assertFieldValue(BackUpCounters bc, long val) {
		
		assertEquals(val, bc.ambiguousNb);
		assertEquals(val, bc.backupWithSizeAboveThreshold);
		assertEquals(val, bc.contentDifferentNb);
		assertEquals(val, bc.copyNewNb);
		assertEquals(val, bc.copyReplaceNb);
		assertEquals(val, bc.copyTreeNb);
		assertEquals(val, bc.deleteDirNb);
		assertEquals(val, bc.deleteNb);
		assertEquals(val, bc.nbHighPermanencePath);
		assertEquals(val, bc.nbMediumPermanencePath);
		assertEquals(val, bc.nbSourceFilesFailed);
		assertEquals(val, bc.nbSourceFilesProcessed);
		assertEquals(val, bc.nbTargetFilesFailed);
		assertEquals(val, bc.nbTargetFilesProcessed);
		assertEquals(val, bc.totalSizeDifference);
	}

	private static void assertFieldValueWithIncrement(BackUpCounters bc, long val, long m) {
		
		assertEquals(val, bc.ambiguousNb);
		assertEquals(val+1*m, bc.backupWithSizeAboveThreshold);
		assertEquals(val+2*m, bc.contentDifferentNb);
		assertEquals(val+3*m, bc.copyNewNb);
		assertEquals(val+4*m, bc.copyReplaceNb);
		assertEquals(val+5*m, bc.copyTreeNb);
		assertEquals(val+6*m, bc.deleteDirNb);
		assertEquals(val+7*m, bc.deleteNb);
		assertEquals(val+8*m, bc.nbHighPermanencePath);
		assertEquals(val+9*m, bc.nbMediumPermanencePath);
		assertEquals(val+10*m, bc.nbSourceFilesFailed);
		assertEquals(val+11*m, bc.nbSourceFilesProcessed);
		assertEquals(val+12*m, bc.nbTargetFilesFailed);
		assertEquals(val+13*m, bc.nbTargetFilesProcessed);
		assertEquals(val+14*m, bc.totalSizeDifference);
	}
	
	private static void setFieldValue(BackUpCounters bc, long val) {
		
		bc.ambiguousNb = val ;
		bc.backupWithSizeAboveThreshold = val ;
		bc.contentDifferentNb = val ;
		bc.copyNewNb = val ;
		bc.copyReplaceNb = val ;
		bc.copyTreeNb = val ;
		bc.deleteDirNb = val ;
		bc.deleteNb = val ;
		bc.nbHighPermanencePath = val ;
		bc.nbMediumPermanencePath = val ;
		bc.nbSourceFilesFailed = val ;
		bc.nbSourceFilesProcessed = val ;
		bc.nbTargetFilesFailed = val ;
		bc.nbTargetFilesProcessed = val ;
		bc.totalSizeDifference = val ;
	}
	
	private static void setFieldValueWithIncrement(BackUpCounters bc, long val) {
		
		bc.ambiguousNb = val ;
		bc.backupWithSizeAboveThreshold = val+1 ;
		bc.contentDifferentNb = val+2 ;
		bc.copyNewNb = val+3 ;
		bc.copyReplaceNb = val+4 ;
		bc.copyTreeNb = val+5 ;
		bc.deleteDirNb = val+6 ;
		bc.deleteNb = val+7 ;
		bc.nbHighPermanencePath = val+8 ;
		bc.nbMediumPermanencePath = val+9 ;
		bc.nbSourceFilesFailed = val+10 ;
		bc.nbSourceFilesProcessed = val+11 ;
		bc.nbTargetFilesFailed = val+12 ;
		bc.nbTargetFilesProcessed = val+13 ;
		bc.totalSizeDifference = val+14 ;
	}
}
