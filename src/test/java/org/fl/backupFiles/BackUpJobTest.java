package org.fl.backupFiles;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.junit.jupiter.api.Test;

public class BackUpJobTest {

	private static Logger log = Logger.getLogger(BackUpJobTest.class.getName()) ;
	
	@Test
	void testNullJson() {
		
		BackUpJob bupj = new BackUpJob(null, log) ;
		
		assertNull(bupj.getTasks(JobTaskType.BUFFER_TO_TARGET)) ;
		assertNull(bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER)) ;
		assertNull(bupj.toString()) ;
	}
	
	@Test
	void testEmptyJson() {
		
		BackUpJob bupj = new BackUpJob("", log) ;
		
		assertNull(bupj.getTasks(JobTaskType.BUFFER_TO_TARGET)) ;
		assertNull(bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER)) ;
		assertNull(bupj.toString()) ;
	}
	
	@Test
	void testRegularJson() {
		
		String json = "	{\r\n" + 
				"		\"titre\" : \"FredericPersonnel sur USB S:\" ,\r\n" + 
				"		\"items\" : [\r\n" + 
				"			{\r\n" + 
				"				\"source\" : \"file:///C:/FredericPersonnel/\",\r\n" + 
				"				\"target\" : \"file:///S:/FredericPersonnel/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/FP_BackUpBuffer/FredericPersonnel/\"\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"source\" : \"file:///C:/FredericPersonnel2/\",\r\n" + 
				"				\"target\" : \"file:///S:/FredericPersonnel2/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/FP_BackUpBuffer/FredericPersonnel2/\"\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"source\" : \"file:///C:/pApps/\",\r\n" + 
				"				\"target\" : \"file:///S:/pApps/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/FP_BackUpBuffer/pApps/\"\r\n" + 
				"			}\r\n" + 
				"		]\r\n" + 
				"	}" ;
		
		BackUpJob bupj = new BackUpJob(json, log) ;
		
		List<BackUpTask> bTt = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertNotNull(bTt) ;
		assertNotNull(bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER)) ;
		assertNotNull(bupj.toString()) ;
		assertEquals("FredericPersonnel sur USB S:", bupj.toString()) ;
		
		assertEquals(3, bTt.size()) ;
	}
	
	@Test
	void testUnmodifiableList() {
		
		String json = "	{\r\n" + 
				"		\"titre\" : \"FredericPersonnel sur USB S:\" ,\r\n" + 
				"		\"items\" : [\r\n" + 
				"			{\r\n" + 
				"				\"source\" : \"file:///C:/FredericPersonnel/\",\r\n" + 
				"				\"target\" : \"file:///S:/FredericPersonnel/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/FP_BackUpBuffer/FredericPersonnel/\"\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"source\" : \"file:///C:/FredericPersonnel2/\",\r\n" + 
				"				\"target\" : \"file:///S:/FredericPersonnel2/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/FP_BackUpBuffer/FredericPersonnel2/\"\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"source\" : \"file:///C:/pApps/\",\r\n" + 
				"				\"target\" : \"file:///S:/pApps/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/FP_BackUpBuffer/pApps/\"\r\n" + 
				"			}\r\n" + 
				"		]\r\n" + 
				"	}" ;
		
		BackUpJob bupj = new BackUpJob(json, log) ;
		
		List<BackUpTask> bTt = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertNotNull(bTt) ;
		
		assertEquals(3, bTt.size()) ;
		assertThrows(UnsupportedOperationException.class , () -> bTt.clear());
	}
}
