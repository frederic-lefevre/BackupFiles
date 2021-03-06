package org.fl.backupFiles.gui.workers;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
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
import org.fl.util.RunningContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class FilesBackUpScannerTest {

	private static final String DEFAULT_PROP_FILE = "file:///ForTests/BackUpFiles/backupFiles2.properties";
	
	private static final int THREAD_TO_NB_DIR_CORRELATION = 2 ;
	
	private static Logger 		   log ;
	private static TestDataManager testDataManager ;
	private static Path 		   configFileDir ;
	private static int 			   threadPoolSize ;
	
	@BeforeAll
	static void generateTestData() {
		
		log = Logger.getGlobal() ;
		try {
			RunningContext runningContext = new RunningContext("BackupFilesTest", null, new URI(DEFAULT_PROP_FILE));

			log = runningContext.getpLog() ;

			AdvancedProperties backupProperty = runningContext.getProps() ;
			Config.initConfig(runningContext.getProps(), log);

			// Get the different config path
			configFileDir = backupProperty.getPathFromURI("backupFiles.configFileDir") ;

			threadPoolSize = backupProperty.getInt("backupFiles.scan.threadPoolSize", 100) ;

			testDataManager = new TestDataManager(configFileDir, log) ;
			boolean genearationSuccessful = testDataManager.generateTestData(threadPoolSize*THREAD_TO_NB_DIR_CORRELATION) ;
			if (! genearationSuccessful) {
				fail("Fail to generate test data") ;
			}
		} catch (URISyntaxException e) {
			log.log(Level.SEVERE, "Exception getting property file URI", e);
			fail("Fail to generate test data") ;
		}
	}
	
	@Test
	void test() {

		try {	
			
			BackUpJobList backUpJobs = new BackUpJobList(configFileDir, log) ;

			if ((backUpJobs == null) || (backUpJobs.isEmpty())) {
				fail("Null or empty BackUpJobList");
			} else if (backUpJobs.size() > 1) {
				fail("There should be only 1 BackUpJob");
			}

			BackUpJob backUpJob = backUpJobs.firstElement() ;
			JobsChoice jobsChoice = new JobsChoice(Arrays.asList(backUpJob), log) ;

			BackUpJobInfoTableModel  bujitm 	 	= new BackUpJobInfoTableModel() ;
			ProgressInformationPanel pip    	 	= new ProgressInformationPanel() ;
			BackUpItemList 			 backUpItems 	= new BackUpItemList() ;
			BackUpTableModel         btm    	 	= new BackUpTableModel(backUpItems) ;
			UiControl				 uicS2B		 	= new UiControl(JobTaskType.SOURCE_TO_BUFFER, btm, pip, bujitm, log) ;
			UiControl				 uicB2T		 	= new UiControl(JobTaskType.BUFFER_TO_TARGET, btm, pip, bujitm, log) ;
			BackUpCounters           backUpCounters ;
			// SOURCE_TO_BUFFER			
			FilesBackUpScanner filesBackUpScanner = new FilesBackUpScanner(uicS2B, JobTaskType.SOURCE_TO_BUFFER, jobsChoice, btm, pip, bujitm, log) ;
			assertEquals(0, backUpItems.size()) ;

			filesBackUpScanner.execute();

			// Wait for filesBackUpScanner end
			filesBackUpScanner.get() ;
			
			filesBackUpScanner.done();
			backUpCounters = filesBackUpScanner.getBackUpCounters() ;
			
			// buffer is supposed to be the same as source
			assertEquals(0, backUpCounters.ambiguousNb) ;
			assertEquals(0, backUpCounters.copyNewNb) ;
			assertEquals(0, backUpCounters.copyReplaceNb) ;
			assertEquals(0, backUpCounters.copyTreeNb) ;
			assertEquals(0, backUpCounters.deleteDirNb) ;
			assertEquals(0, backUpCounters.deleteNb) ;
			assertEquals(0, backUpCounters.backupWithSizeAboveThreshold) ;
			assertEquals(0, backUpCounters.contentDifferentNb) ;
			assertEquals(0, backUpCounters.nbHighPermanencePath) ;
			assertEquals(0, backUpCounters.nbMediumPermanencePath) ;
			assertEquals(0, backUpCounters.nbSourceFilesFailed) ;
			assertEquals(19800, backUpCounters.nbSourceFilesProcessed) ;
			assertEquals(19700, backUpCounters.nbTargetFilesProcessed) ;
			assertEquals(0, backUpCounters.nbTargetFilesFailed) ;
			assertEquals(0, backUpCounters.totalSizeDifference) ;

			assertEquals(0, backUpItems.size()) ;
			
			// BUFFER_TO_TARGET
			filesBackUpScanner = new FilesBackUpScanner(uicB2T, JobTaskType.BUFFER_TO_TARGET, jobsChoice, btm, pip, bujitm, log) ;
			assertEquals(0, backUpItems.size()) ;

			filesBackUpScanner.execute();

			// Wait for filesBackUpScanner end
			filesBackUpScanner.get() ;
			filesBackUpScanner.done();
			backUpCounters = filesBackUpScanner.getBackUpCounters() ;

			// target is supposed to be empty
			assertEquals(0, backUpCounters.ambiguousNb) ;
			assertEquals(0, backUpCounters.copyNewNb) ;
			assertEquals(0, backUpCounters.copyReplaceNb) ;
			assertEquals(threadPoolSize*THREAD_TO_NB_DIR_CORRELATION, backUpCounters.copyTreeNb) ;
			assertEquals(0, backUpCounters.deleteDirNb) ;
			assertEquals(0, backUpCounters.deleteNb) ;
			assertEquals(0, backUpCounters.backupWithSizeAboveThreshold) ;
			assertEquals(0, backUpCounters.contentDifferentNb) ;
			assertEquals(0, backUpCounters.nbHighPermanencePath) ;
			assertEquals(threadPoolSize*THREAD_TO_NB_DIR_CORRELATION, backUpCounters.nbMediumPermanencePath) ;
			assertEquals(0, backUpCounters.nbSourceFilesFailed) ;
			assertEquals(threadPoolSize*THREAD_TO_NB_DIR_CORRELATION, backUpCounters.nbSourceFilesProcessed) ;
			assertEquals(0, backUpCounters.nbTargetFilesProcessed) ;
			assertEquals(0, backUpCounters.nbTargetFilesFailed) ;
			assertEquals(4997400, backUpCounters.totalSizeDifference) ;

			assertEquals(threadPoolSize*THREAD_TO_NB_DIR_CORRELATION, backUpItems.size()) ;
			
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
