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

package org.fl.backupFiles.gui.workers;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpCounters;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpJob;
import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.fl.backupFiles.BackUpJobList;
import org.fl.backupFiles.Config;
import org.fl.backupFiles.JobsChoice;
import org.fl.backupFiles.gui.BackUpJobInfoTableModel;
import org.fl.backupFiles.gui.BackUpTableModel;
import org.fl.backupFiles.gui.ProgressInformationPanel;
import org.fl.backupFiles.gui.UiControl;
import org.fl.util.AdvancedProperties;
import org.fl.util.FilterCounter;
import org.fl.util.FilterCounter.LogRecordCounter;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FilesBackUpScannerTest {

	private static final String DEFAULT_PROP_FILE = "file:///ForTests/BackUpFiles/backupFiles2.properties";
	
	private static final int THREAD_TO_NB_DIR_CORRELATION = 2;

	private static final Logger log = Logger.getLogger(FilesBackUpScannerTest.class.getName());
	private static TestDataManager testDataManager;
	private static Path configFileDir;
	private static int threadPoolSize;
	
	@BeforeAll
	static void generateTestData() {

		Config.initConfig(DEFAULT_PROP_FILE);
		AdvancedProperties backupProperty = Config.getRunningContext().getProps();

		// Get the different config path
		configFileDir = backupProperty.getPathFromURI("backupFiles.configFileDir");

		threadPoolSize = backupProperty.getInt("backupFiles.scan.threadPoolSize", 100);

		testDataManager = new TestDataManager(configFileDir, log);
		boolean genearationSuccessful = testDataManager.generateTestData(threadPoolSize * THREAD_TO_NB_DIR_CORRELATION);
		if (!genearationSuccessful) {
			fail("Fail to generate test data");
		}

	}
	
	@Test
	void test() {
		try {	
			
			LogRecordCounter logCounterForFilesBackUpScanner = 
					FilterCounter.getLogRecordCounter(Logger.getLogger("org.fl.backupFiles.gui.workers.FilesBackUpScanner"));
			
			LogRecordCounter logCounterForscannerBackUpScannerThread = 
					FilterCounter.getLogRecordCounter(Logger.getLogger("org.fl.backupFiles.scanner.BackUpScannerThread"));
			
			BackUpJobList backUpJobs = new BackUpJobList(configFileDir) ;

			if ((backUpJobs == null) || (backUpJobs.isEmpty())) {
				fail("Null or empty BackUpJobList");
			} else if (backUpJobs.size() > 1) {
				fail("There should be only 1 BackUpJob");
			}

			BackUpJob backUpJob = backUpJobs.firstElement() ;
			JobsChoice jobsChoice = new JobsChoice(Arrays.asList(backUpJob)) ;

			BackUpJobInfoTableModel  bujitm 	 	= new BackUpJobInfoTableModel() ;
			ProgressInformationPanel pip    	 	= new ProgressInformationPanel() ;
			BackUpItemList 			 backUpItems 	= new BackUpItemList() ;
			BackUpTableModel         btm    	 	= new BackUpTableModel(backUpItems) ;
			UiControl				 uicS2B		 	= new UiControl(JobTaskType.SOURCE_TO_BUFFER, btm, pip, bujitm) ;
			UiControl				 uicB2T		 	= new UiControl(JobTaskType.BUFFER_TO_TARGET, btm, pip, bujitm) ;
			BackUpCounters           backUpCounters ;
			// SOURCE_TO_BUFFER			
			FilesBackUpScanner filesBackUpScanner = new FilesBackUpScanner(uicS2B, JobTaskType.SOURCE_TO_BUFFER, jobsChoice, btm, pip, bujitm);
			assertThat(backUpItems).isEmpty();

			filesBackUpScanner.execute();

			// Wait for filesBackUpScanner end
			filesBackUpScanner.get() ;
			
			filesBackUpScanner.done();
			backUpCounters = filesBackUpScanner.getBackUpCounters() ;
			
			// buffer is supposed to be the same as source
			assertThat(backUpCounters.ambiguousNb).isZero();
			assertThat(backUpCounters.copyNewNb).isZero();
			assertThat(backUpCounters.copyReplaceNb).isZero();
			assertThat(backUpCounters.copyTreeNb).isZero();
			assertThat(backUpCounters.deleteDirNb).isZero();
			assertThat(backUpCounters.deleteNb).isZero();
			assertThat(backUpCounters.backupWithSizeAboveThreshold).isZero();
			assertThat(backUpCounters.contentDifferentNb).isZero();
			assertThat(backUpCounters.nbHighPermanencePath).isZero();
			assertThat(backUpCounters.nbMediumPermanencePath).isZero();
			assertThat(backUpCounters.nbSourceFilesFailed).isZero();
			assertThat(backUpCounters.nbSourceFilesProcessed).isEqualTo(19800);
			assertThat(backUpCounters.nbTargetFilesProcessed).isEqualTo(19700);
			assertThat(backUpCounters.nbTargetFilesFailed).isZero();
			assertThat(backUpCounters.totalSizeDifference).isZero();
			assertThat(backUpCounters.copyTargetNb).isZero();

			assertThat(backUpItems).isEmpty();
			
			// BUFFER_TO_TARGET
			filesBackUpScanner = new FilesBackUpScanner(uicB2T, JobTaskType.BUFFER_TO_TARGET, jobsChoice, btm, pip, bujitm) ;
			assertThat(backUpItems).isEmpty();

			filesBackUpScanner.execute();

			// Wait for filesBackUpScanner end
			filesBackUpScanner.get() ;
			filesBackUpScanner.done();
			backUpCounters = filesBackUpScanner.getBackUpCounters();

			// target is supposed to be empty
			assertThat(backUpCounters.ambiguousNb).isZero();
			assertThat(backUpCounters.copyNewNb).isZero();
			assertThat(backUpCounters.copyReplaceNb).isZero();
			assertThat(backUpCounters.copyTreeNb).isEqualTo(threadPoolSize*THREAD_TO_NB_DIR_CORRELATION);
			assertThat(backUpCounters.deleteDirNb).isZero();
			assertThat(backUpCounters.deleteNb).isZero();
			assertThat(backUpCounters.backupWithSizeAboveThreshold).isZero();
			assertThat(backUpCounters.contentDifferentNb).isZero();
			assertThat(backUpCounters.nbHighPermanencePath).isZero();
			assertThat(backUpCounters.nbMediumPermanencePath).isEqualTo(threadPoolSize*THREAD_TO_NB_DIR_CORRELATION);
			assertThat(backUpCounters.nbSourceFilesFailed).isZero();
			assertThat(backUpCounters.nbSourceFilesProcessed).isEqualTo(threadPoolSize*THREAD_TO_NB_DIR_CORRELATION);
			assertThat(backUpCounters.nbTargetFilesProcessed).isZero();		
			assertThat(backUpCounters.nbTargetFilesFailed).isZero();
			assertThat(backUpCounters.totalSizeDifference).isEqualTo(4997400);
			assertThat(backUpCounters.copyTargetNb).isZero();
		
			assertThat(backUpItems).hasSize(threadPoolSize*THREAD_TO_NB_DIR_CORRELATION);
			
			assertThat(logCounterForFilesBackUpScanner.getLogRecordCount()).isEqualTo(2);
			assertThat(logCounterForFilesBackUpScanner.getLogRecordCount(Level.INFO)).isEqualTo(2);
			
			// Stack trace is too long to have the test method in it. So warning are not counted
			assertThat(logCounterForscannerBackUpScannerThread.getLogRecordCount()).isZero();
			
		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Exception in BackUpScannerProcessor test", e);
			fail("Exception");
		}
	}

	@AfterAll
	static void deleteTestData() {
		// clean generated config, source files, buffer and target
		testDataManager.deleteTestData() ;
	}
	
}
