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

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.junit.jupiter.api.Test;

public class BackUpJobTest {
	
	@Test
	void testNullJson() {
		
		BackUpJob bupj = new BackUpJob(null) ;
		
		assertNull(bupj.getTasks(JobTaskType.BUFFER_TO_TARGET)) ;
		assertNull(bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER)) ;
		assertNull(bupj.toString()) ;
	}
	
	@Test
	void testEmptyJson() {
		
		BackUpJob bupj = new BackUpJob("") ;
		
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
				"				\"source\" : \"file:///C:/ForTests/\",\r\n" + 
				"				\"target\" : \"file:///S:/ForTests/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/FP_BackUpBuffer/ForTests/\"\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"source\" : \"file:///C:/pApps/\",\r\n" + 
				"				\"target\" : \"file:///S:/pApps/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/FP_BackUpBuffer/pApps/\"\r\n" + 
				"			}\r\n" + 
				"		]\r\n" + 
				"	}" ;
		
		BackUpJob bupj = new BackUpJob(json) ;
		
		List<BackUpTask> bTt = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertNotNull(bTt) ;
		assertNotNull(bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER)) ;
		assertNotNull(bupj.toString()) ;
		assertEquals("FredericPersonnel sur USB S:", bupj.toString()) ;
		
		assertEquals(3, bTt.size()) ;
	}
	
	@Test
	void testParallelJson() {
		
		String json = "	{\r\n" + 
				"		\"titre\" : \"FredericPersonnel sur USB S:\" ,\r\n" + 
				"		\"items\" : [\r\n" + 
				"			{\r\n" + 
				"				\"source\" : \"file:///C:/FredericPersonnel/\",\r\n" + 
				"				\"target\" : \"file:///S:/FredericPersonnel/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/FP_BackUpBuffer/FredericPersonnel/\",\r\n" + 
				"				\"parallelScan\" : true\r\n" +
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"source\" : \"file:///C:/ForTests/\",\r\n" + 
				"				\"target\" : \"file:///S:/ForTests/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/FP_BackUpBuffer/ForTests/\"\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"source\" : \"file:///C:/pApps/\",\r\n" + 
				"				\"target\" : \"file:///S:/pApps/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/FP_BackUpBuffer/pApps/\",\r\n" + 
				"				\"parallelScan\" : false\r\n" +
				"			}\r\n" + 
				"		]\r\n" + 
				"	}" ;
		
		BackUpJob bupj = new BackUpJob(json) ;
		
		List<BackUpTask> bTt = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET) ;
		assertNotNull(bTt) ;
		List<BackUpTask> sTb = bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER) ;
		assertNotNull(sTb) ;
		assertNotNull(bupj.toString()) ;
		assertEquals("FredericPersonnel sur USB S:", bupj.toString()) ;
		
		long expectedNbTasks = nbFileInDir("file:///C:/FredericPersonnel/") + 2;
		assertEquals(expectedNbTasks, sTb.size()) ;
		assertEquals(expectedNbTasks, bTt.size()) ;

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
				"				\"source\" : \"file:///C:/ForTests/\",\r\n" + 
				"				\"target\" : \"file:///S:/ForTests/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/FP_BackUpBuffer/FredericPersonnel2/\"\r\n" + 
				"			},\r\n" + 
				"			{\r\n" + 
				"				\"source\" : \"file:///C:/pApps/\",\r\n" + 
				"				\"target\" : \"file:///S:/pApps/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/FP_BackUpBuffer/pApps/\"\r\n" + 
				"			}\r\n" + 
				"		]\r\n" + 
				"	}" ;
		
		BackUpJob bupj = new BackUpJob(json) ;
		
		List<BackUpTask> bTt = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertNotNull(bTt) ;
		
		assertEquals(3, bTt.size()) ;
		assertThrows(UnsupportedOperationException.class , () -> bTt.clear());
	}
	
	private long nbFileInDir(String dir) {
		
		Path dirPath = Paths.get(URI.create(dir)) ;
		long res = 0;
		try (Stream<Path> sourceFileStream = Files.list(dirPath)) {		 
			res = sourceFileStream.count();
		 } catch (Exception e) {
			fail("Exception in counting files indir " + e.getMessage());
		}
		return res;
	}
}
