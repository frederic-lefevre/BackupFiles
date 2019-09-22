package org.fl.backupFiles.gui.workers;

import static org.junit.jupiter.api.Assertions.*;

import java.net.URI;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.junit.jupiter.api.Test;

import com.ibm.lge.fl.util.AdvancedProperties;
import com.ibm.lge.fl.util.RunningContext;

class FilesBackUpScannerTest {

	private static final String DEFAULT_PROP_FILE = "file:///ForTests/BackUpFiles/backupFiles2.properties";
	
	@Test
	void test() {

		try {	
			RunningContext runningContext = new RunningContext("BackupFilesTest", null, new URI(DEFAULT_PROP_FILE));
			Logger log = runningContext.getpLog() ;

			AdvancedProperties backupProperty = runningContext.getProps() ;
			Config.initConfig(runningContext.getProps());

			// Get the different config path
			Path configFileDir = backupProperty.getPathFromURI("backupFiles.configFileDir") ;
			
			TestDataManager testDataManager = new TestDataManager(configFileDir, log) ;
			boolean genearationSuccessful = testDataManager.generateTestData(4) ;
			if (! genearationSuccessful) {
				fail("Fail to generate test data") ;
			}

			BackUpJobList backUpJobs = new BackUpJobList(configFileDir, log) ;

			if ((backUpJobs == null) || (backUpJobs.isEmpty())) {
				fail("Null or empty BackUpJobList");
			} else if (backUpJobs.size() > 1) {
				fail("There should be only 1 BackUpJob");
			}

			BackUpJob backUpJob = backUpJobs.firstElement() ;
			JobsChoice jobsChoice = new JobsChoice(Arrays.asList(backUpJob), log) ;

			BackUpJobInfoTableModel  bujitm 	 = new BackUpJobInfoTableModel() ;
			ProgressInformationPanel pip    	 = new ProgressInformationPanel() ;
			BackUpItemList 			 backUpItems = new BackUpItemList() ;
			BackUpTableModel         btm    	 = new BackUpTableModel(backUpItems) ;
			UiControl				 uicS2B		 = new UiControl(JobTaskType.SOURCE_TO_BUFFER, btm, pip, bujitm, log) ;
			UiControl				 uicB2T		 = new UiControl(JobTaskType.BUFFER_TO_TARGET, btm, pip, bujitm, log) ;

			// SOURCE_TO_BUFFER			
//			FilesBackUpScanner filesBackUpScanner = new FilesBackUpScanner(uicS2B, JobTaskType.SOURCE_TO_BUFFER, jobsChoice, btm, pip, bujitm, log) ;
//			assertEquals(0, backUpItems.size()) ;
//
//			filesBackUpScanner.execute();
//
//			// Wait for filesBackUpScanner end
//			filesBackUpScanner.get() ;
//
//			// buffer is supposed to be the same as source
//			assertEquals(0, backUpItems.size()) ;
//
//			// BUFFER_TO_TARGET
//			filesBackUpScanner = new FilesBackUpScanner(uicB2T, JobTaskType.BUFFER_TO_TARGET, jobsChoice, btm, pip, bujitm, log) ;
//			assertEquals(0, backUpItems.size()) ;
//
//			filesBackUpScanner.execute();
//
//			// Wait for filesBackUpScanner end
//			filesBackUpScanner.get() ;
//
//			// target is supposed to be empty
//			assertNotEquals(0, backUpItems.size()) ;

			// clean generated config, source files, buffer and target
			testDataManager.deleteTestData() ;

		} catch (Exception e) {
			Logger.getGlobal().log(Level.SEVERE, "Exception in BackUpScannerProcessor test", e);
			fail("Exception");
		}
	}

}
