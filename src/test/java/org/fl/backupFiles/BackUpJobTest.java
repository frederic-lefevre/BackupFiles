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
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.fl.backupFiles.directoryGroup.DirectoryGroupConfiguration;
import org.fl.util.FilterCounter;
import org.fl.util.FilterCounter.LogRecordCounter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class BackUpJobTest {
	
	private static DirectoryGroupConfiguration directoryGroupConfiguration;
	
	@BeforeAll
	static void init() {
		directoryGroupConfiguration = new DirectoryGroupConfiguration("");
	}
	
	@Test
	void testNullJson() {

		LogRecordCounter logCounter = FilterCounter.getLogRecordCounter(Logger.getLogger(BackUpJob.class.getName()));
		
		BackUpJob bupj = new BackUpJob(null, directoryGroupConfiguration);
		
		Stream.of(JobTaskType.values())
			.forEach(jobTaskType -> 
				assertThat(bupj.getTasks(jobTaskType))
					.isNotNull()
					.isEmpty()
			);
		
		assertThat(bupj.toString()).isNull();
		
		assertThat(logCounter.getLogRecordCount()).isEqualTo(1);
		assertThat(logCounter.getLogRecordCount(Level.SEVERE)).isEqualTo(1);
	}
	
	@Test
	void testEmptyJson() {
		
		LogRecordCounter logCounter = FilterCounter.getLogRecordCounter(Logger.getLogger(BackUpJob.class.getName()));
		
		BackUpJob bupj = new BackUpJob("", directoryGroupConfiguration);
		
		Stream.of(JobTaskType.values())
			.forEach(jobTaskType -> 
			assertThat(bupj.getTasks(jobTaskType))
				.isNotNull()
				.isEmpty()
		);
		
		assertThat(bupj.toString()).isNull();
		
		assertThat(logCounter.getLogRecordCount()).isEqualTo(1);
		assertThat(logCounter.getLogRecordCount(Level.SEVERE)).isEqualTo(1);
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
		
		LogRecordCounter logCounter = FilterCounter.getLogRecordCounter(Logger.getLogger(BackUpTask.class.getName()));
		
		BackUpJob bupj = new BackUpJob(json, directoryGroupConfiguration);
		assertThat(bupj.toString())
			.isNotNull()
			.isEqualTo("FredericPersonnel sur USB S:");
		
		List<BackUpTask> bTt1 = bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER);
		assertThat(bTt1).isNotNull().hasSize(3);
		
		List<BackUpTask> bTt2 = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertThat(bTt2).isNotNull().hasSize(3);
		
		int nbBackUpTasks = bTt1.size() + bTt2.size();
		
		if (Files.exists(Path.of(URI.create("file:///S:/")))) {
			assertThat(logCounter.getLogRecordCount()).isZero();
		} else {
			assertThat(logCounter.getLogRecordCount()).isEqualTo(nbBackUpTasks);
			assertThat(logCounter.getLogRecordCount(Level.WARNING)).isEqualTo(nbBackUpTasks);
		}
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
				"	}";
		
		LogRecordCounter logCounter = FilterCounter.getLogRecordCounter(Logger.getLogger(BackUpTask.class.getName()));
		
		BackUpJob bupj = new BackUpJob(json, directoryGroupConfiguration) ;
		
		List<BackUpTask> bTt = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertThat(bTt).isNotNull();
		List<BackUpTask> sTb = bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER);
		assertThat(sTb).isNotNull();
		assertThat(bupj.toString())
			.isNotNull()
			.hasToString("FredericPersonnel sur USB S:");
		
		long expectedNbTasks = nbFileInDir("file:///C:/FredericPersonnel/") + 2;
		assertThat(sTb).hasSize((int) expectedNbTasks);
		assertThat(bTt).hasSize((int) expectedNbTasks);

		if (Files.exists(Path.of(URI.create("file:///S:/")))) {
			assertThat(logCounter.getLogRecordCount()).isZero();
		} else {
			assertThat(logCounter.getLogRecordCount()).isEqualTo(expectedNbTasks*2);
			assertThat(logCounter.getLogRecordCount(Level.WARNING)).isEqualTo(expectedNbTasks*2);
		}
	}
	
	@Test
	void testParallelJson2() throws IOException {
		
		String json = "	{\r\n" + 
				"		\"titre\" : \"Parrallel test with delete\" ,\r\n" + 
				"		\"items\" : [\r\n" + 
				"			{\r\n" + 
				"				\"source\" : \"file:///C:/ForTests/BackUpFiles/FP_Test_Source3/\",\r\n" + 
				"				\"target\" : \"file:///C:/ForTests/BackUpFiles/FP_Test_Target3/\",\r\n" + 
				"				\"buffer\" : \"file:///C:/ForTests/BackUpFiles/FP_Test_Buffer3/\",\r\n" + 
				"				\"parallelScan\" : true\r\n" +
				"			}" +
				"		]\r\n" + 
				"	}";
		
		BackUpJob bupj = new BackUpJob(json, directoryGroupConfiguration) ;
		
		List<BackUpTask> bTt = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertThat(bTt).isNotNull();
		List<BackUpTask> sTb = bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER);
		assertThat(sTb).isNotNull();
		assertThat(bupj.toString())
			.isNotNull()
			.hasToString("Parrallel test with delete");
		
		// 3 folder in source, 3 + 1 folder in buffer, 3 + 1 folder in target
		assertThat(sTb).hasSize((int) 5);
		assertThat(bTt).hasSize((int) 5);

		// Add a folder in target
		Path tgtPath = Paths.get(URI.create("file:///C:/ForTests/BackUpFiles/FP_Test_Target3"));
		
		Path newFolderPath = tgtPath.resolve("aNewFolder");
		Files.createDirectory(newFolderPath);
		
		List<BackUpTask> bTt2 = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertThat(bTt).isNotNull();
		List<BackUpTask> sTb2 = bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER);
		assertThat(sTb2).isNotNull();
		assertThat(bupj.toString())
			.isNotNull()
			.hasToString("Parrallel test with delete");
		
		// 3 folder in source, 3 + 1 folder in buffer, 3 + 2 folder in target
		assertThat(sTb2).hasSize((int) 6);
		assertThat(bTt2).hasSize((int) 6);
		
		Files.delete(newFolderPath);
		
		List<BackUpTask> bTt3 = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertThat(bTt).isNotNull();
		List<BackUpTask> sTb3 = bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER);
		assertThat(sTb3).isNotNull();
		assertThat(bupj.toString())
			.isNotNull()
			.hasToString("Parrallel test with delete");
		
		// 3 folder in source, 3 + 1 folder in buffer, 3 + 1 folder in target
		assertThat(sTb3).hasSize((int) 5);
		assertThat(bTt3).hasSize((int) 5);
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
		
		LogRecordCounter logCounter = FilterCounter.getLogRecordCounter(Logger.getLogger(BackUpTask.class.getName()));
		
		BackUpJob bupj = new BackUpJob(json, directoryGroupConfiguration) ;
		
		List<BackUpTask> bTt = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertThat(bTt)
			.isNotNull()
			.hasSize(3);
		
		int nbBackUpTasks = bTt.size();
		
		if (Files.exists(Path.of(URI.create("file:///S:/")))) {
			assertThat(logCounter.getLogRecordCount()).isZero();
		} else {
			assertThat(logCounter.getLogRecordCount()).isEqualTo(nbBackUpTasks);
			assertThat(logCounter.getLogRecordCount(Level.WARNING)).isEqualTo(nbBackUpTasks);
		}
		
		assertThatExceptionOfType(UnsupportedOperationException.class)
			.isThrownBy(() -> bTt.clear());
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
