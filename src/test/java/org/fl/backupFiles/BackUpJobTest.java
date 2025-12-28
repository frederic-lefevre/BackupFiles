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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.fl.backupFiles.directoryGroup.DirectoryGroupConfiguration;
import org.fl.util.FilterCounter;
import org.fl.util.FilterCounter.LogRecordCounter;
import org.fl.util.file.FilesUtils;
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
		
		String json ="""		
				{ 
						"titre" : "Regular json",
						"items" : [
							{
								"source" : "file:///FredericPersonnel/",
								"target" : "file:///ForTests/",
								"buffer" : "file:///FP_BackUpBuffer/FredericPersonnel/"
							}, 
							{ 
								"source" : "file:///ForTests/", 
								"target" : "file:///tmp/",
								"buffer" : "file:///FP_BackUpBuffer/ForTests/"
							},
							{
								"source" : "file:///pApps/",
								"target" : "file:///tmp/",
								"buffer" : "file:///FP_BackUpBuffer/pApps/"
							}
						]
					}
	""" ;
		
		BackUpJob bupj = new BackUpJob(json, directoryGroupConfiguration);
		assertThat(bupj.toString())
			.isNotNull()
			.isEqualTo("Regular json");
		
		List<BackUpTask> bTt1 = bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER);
		assertThat(bTt1).isNotNull().hasSize(3);
		
		List<BackUpTask> bTt2 = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertThat(bTt2).isNotNull().hasSize(3);
		
		List<BackUpTask> bTt3 = bupj.getTasks(JobTaskType.SOURCE_TO_TARGET);
		assertThat(bTt3).isNotNull().isEmpty();
		
		assertThat(bupj.getAllJobTaskType())
			.isNotEmpty()
			.hasSameElementsAs(List.of(JobTaskType.SOURCE_TO_BUFFER, JobTaskType.BUFFER_TO_TARGET));
	}
	
	@Test
	void testSourceToTargetJson() {
		
		String json ="""		
				{ 
						"titre" : "Regular json",
						"items" : [
							{
								"source" : "file:///FredericPersonnel/",
								"target" : "file:///ForTests/"
							}, 
							{ 
								"source" : "file:///ForTests/", 
								"target" : "file:///tmp/",
								"buffer" : "file:///FP_BackUpBuffer/ForTests/"
							},
							{
								"source" : "file:///pApps/",
								"target" : "file:///tmp/",
								"buffer" : "file:///FP_BackUpBuffer/pApps/"
							}
						]
					}
	""" ;
		
		BackUpJob bupj = new BackUpJob(json, directoryGroupConfiguration);
		assertThat(bupj.toString())
			.isNotNull()
			.isEqualTo("Regular json");
		
		List<BackUpTask> bTt1 = bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER);
		assertThat(bTt1).isNotNull().hasSize(2);
		
		List<BackUpTask> bTt2 = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertThat(bTt2).isNotNull().hasSize(2);
		
		List<BackUpTask> bTt3 = bupj.getTasks(JobTaskType.SOURCE_TO_TARGET);
		assertThat(bTt3).isNotNull().hasSize(1);
		
		assertThat(bupj.getAllJobTaskType())
			.isNotEmpty()
			.hasSameElementsAs(List.of(JobTaskType.SOURCE_TO_TARGET, JobTaskType.SOURCE_TO_BUFFER, JobTaskType.BUFFER_TO_TARGET));
	}
	
	@Test
	void testSourceToTargetOnlyJson() {
		
		String json ="""		
				{ 
						"titre" : "Regular json",
						"items" : [
							{
								"source" : "file:///FredericPersonnel/",
								"target" : "file:///ForTests/"
							}
						]
					}
	""" ;
		
		BackUpJob bupj = new BackUpJob(json, directoryGroupConfiguration);
		assertThat(bupj.toString())
			.isNotNull()
			.isEqualTo("Regular json");
		
		List<BackUpTask> bTt1 = bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER);
		assertThat(bTt1).isNotNull().isEmpty();
		
		List<BackUpTask> bTt2 = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertThat(bTt2).isNotNull().isEmpty();
		
		List<BackUpTask> bTt3 = bupj.getTasks(JobTaskType.SOURCE_TO_TARGET);
		assertThat(bTt3).isNotNull().hasSize(1);
		
		assertThat(bupj.getAllJobTaskType())
			.isNotEmpty()
			.hasSameElementsAs(List.of(JobTaskType.SOURCE_TO_TARGET));
	}
	
	@Test
	void testParallelJson() throws URISyntaxException {
		
		String json ="""		
				{ 
						"titre" : "Parallel json",
						"items" : [
							{
								"source" : "file:///FredericPersonnel/",
								"target" : "file:///ForTests/Empty_Target",
								"buffer" : "file:///FP_BackUpBuffer/FredericPersonnel/",
								"parallelScan" : true
							}, 
							{ 
								"source" : "file:///ForTests/", 
								"target" : "file:///tmp/",
								"buffer" : "file:///FP_BackUpBuffer/ForTests/"
							},
							{
								"source" : "file:///pApps/",
								"target" : "file:///tmp/",
								"buffer" : "file:///FP_BackUpBuffer/pApps/",
								"parallelScan" : false
							}
						]
					}
	""" ;
		
		BackUpJob bupj = new BackUpJob(json, directoryGroupConfiguration) ;
		
		List<BackUpTask> bTt = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertThat(bTt).isNotNull();
		List<BackUpTask> sTb = bupj.getTasks(JobTaskType.SOURCE_TO_BUFFER);
		assertThat(sTb).isNotNull();
		assertThat(bupj.toString())
			.isNotNull()
			.hasToString("Parallel json");
		
		long expectedNbTasks = nbFileInDir("file:///FredericPersonnel/") + 2;
		assertThat(sTb).hasSize((int) expectedNbTasks);
		assertThat(bTt).hasSize((int) expectedNbTasks);

	}
	
	@Test
	void testParallelJson2() throws IOException, URISyntaxException {
		
		String json ="""		
				{ 
						"titre" : "Parrallel test with delete",
						"items" : [
							{
								"source" : "file:///ForTests/BackUpFiles/FP_Test_Source3/",
								"target" : "file:///ForTests/BackUpFiles/FP_Test_Target3/",
								"buffer" : "file:///ForTests/BackUpFiles/FP_Test_Buffer3/",
								"parallelScan" : true
							}
						]
					}
	""" ;
		
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
		Path tgtPath = FilesUtils.uriStringToAbsolutePath("file:///ForTests/BackUpFiles/FP_Test_Target3");
		
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
		
		String json ="""		
				{ 
						"titre" : "Unmodifiable list test",
						"items" : [
							{
								"source" : "file:///FredericPersonnel/",
								"target" : "file:///ForTests/Empty_Target",
								"buffer" : "file:///FP_BackUpBuffer/FredericPersonnel/"
							}, 
							{ 
								"source" : "file:///ForTests/", 
								"target" : "file:///tmp/",
								"buffer" : "file:///FP_BackUpBuffer/ForTests/"
							},
							{
								"source" : "file:///pApps/",
								"target" : "file:///tmp/",
								"buffer" : "file:///FP_BackUpBuffer/pApps/"
							}
						]
					}
	""" ;
		
		BackUpJob bupj = new BackUpJob(json, directoryGroupConfiguration) ;
		
		List<BackUpTask> bTt = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertThat(bTt)
			.isNotNull()
			.hasSize(3);
		
		assertThatExceptionOfType(UnsupportedOperationException.class)
			.isThrownBy(() -> bTt.clear());
	}
	
	@Test
	void testNoBufferAndTarget() {
		
		String json ="""		
				{ 
						"titre" : "No Buffer and Target",
						"items" : [
							{
								"source" : "file:///FredericPersonnel/"
							}, 
							{ 
								"source" : "file:///ForTests/", 
								"target" : "file:///tmp/",
								"buffer" : "file:///FP_BackUpBuffer/ForTests/"
							},
							{
								"source" : "file:///pApps/",
								"target" : "file:///tmp/",
								"buffer" : "file:///FP_BackUpBuffer/pApps/"
							}
						]
					}
	""" ;
		
		LogRecordCounter logCounter = FilterCounter.getLogRecordCounter(Logger.getLogger(BackUpJob.class.getName()));
		
		BackUpJob bupj = new BackUpJob(json, directoryGroupConfiguration) ;
		
		List<BackUpTask> bTt = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertThat(bTt)
			.isNotNull()
			.hasSize(2);
		
		assertThat(logCounter.getLogRecordCount()).isEqualTo(1);
		assertThat(logCounter.getLogRecordCount(Level.SEVERE)).isEqualTo(1);
		
		assertThat(logCounter.getLogRecords()).singleElement()
			.satisfies(logRecord -> assertThat(logRecord.getMessage()).contains("No buffer and target"));
	}
	
	@Test
	void testInvalidURI() {
		
		String json ="""		
				{ 
						"titre" : "Wrong URI",
						"items" : [
							{
								"source" : "file:///FredericPersonnel/",
								"target" : "**** WRONG URI",
								"buffer" : "file:///FP_BackUpBuffer/FredericPersonnel/"
							}, 
							{ 
								"source" : "file:///ForTests/", 
								"target" : "file:///tmp/",
								"buffer" : "file:///FP_BackUpBuffer/ForTests/"
							},
							{
								"source" : "file:///pApps/",
								"target" : "file:///tmp/",
								"buffer" : "file:///FP_BackUpBuffer/pApps/"
							}
						]
					}
	""" ;
		
		LogRecordCounter logCounter = FilterCounter.getLogRecordCounter(Logger.getLogger(BackUpJob.class.getName()));
		
		BackUpJob bupj = new BackUpJob(json, directoryGroupConfiguration) ;
		
		List<BackUpTask> bTt = bupj.getTasks(JobTaskType.BUFFER_TO_TARGET);
		assertThat(bTt)
			.isNotNull()
			.isEmpty();
		
		assertThat(logCounter.getLogRecordCount()).isEqualTo(1);
		assertThat(logCounter.getLogRecordCount(Level.SEVERE)).isEqualTo(1);

	}
	
	private long nbFileInDir(String dir) throws URISyntaxException {
		
		Path dirPath = FilesUtils.uriStringToAbsolutePath(dir);
		long res = 0;
		try (Stream<Path> sourceFileStream = Files.list(dirPath)) {		 
			res = sourceFileStream.count();
		 } catch (Exception e) {
			fail("Exception in counting files indir " + e.getMessage());
		}
		return res;
	}
}
