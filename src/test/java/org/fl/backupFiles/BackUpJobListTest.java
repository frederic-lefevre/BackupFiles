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
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.fl.util.FilterCounter;
import org.fl.util.FilterCounter.LogRecordCounter;
import org.fl.util.file.FilesUtils;
import org.junit.jupiter.api.Test;

class BackUpJobListTest {
	
	@Test
	void shouldCreateBackpUpJobList() throws URISyntaxException {
		
		Path configPath = FilesUtils.uriStringToAbsolutePath("file:///ForTests/BackUpFiles/configTest/");
		
		try (Stream<Path> pathStream = Files.list(configPath)) {
			
			long configFileNumber = pathStream.map(Path::getFileName).filter(p -> p.toString().endsWith(".json")).count();
			
			BackUpJobList jobs = new BackUpJobList(configPath);
			
			assertThat(jobs).isNotNull().hasSize((int) configFileNumber);
			
			assertThat(jobs).allSatisfy(job -> 
				assertThat(job.getAllJobTaskType())
					.hasSameElementsAs(Set.of(JobTaskType.SOURCE_TO_BUFFER, JobTaskType.BUFFER_TO_TARGET)));
			
			assertThat(jobs.getJobTaskTypes()).isNotNull()
				.hasSameElementsAs(Set.of(JobTaskType.SOURCE_TO_BUFFER, JobTaskType.BUFFER_TO_TARGET));
			
		} catch (Exception e) {
			fail(e);
		}
	}

	@Test
	void shouldCreateEmptyBackpUpJobList() throws URISyntaxException {
		
		LogRecordCounter logCounter = FilterCounter.getLogRecordCounter(Logger.getLogger(BackUpJobList.class.getName()));
		
		Path configPath = FilesUtils.uriStringToAbsolutePath("file:///ForTests/BackUpFiles/emptyConfigDir/");
		
		try (Stream<Path> pathStream = Files.list(configPath)) {
			
			BackUpJobList jobs = new BackUpJobList(configPath);
			
			assertThat(jobs).isNotNull().isEmpty();
			
			assertThat(jobs.getJobTaskTypes()).isNotNull().isEmpty();
			
			assertThat(logCounter.getLogRecordCount()).isEqualTo(1);
			assertThat(logCounter.getLogRecordCount(Level.WARNING)).isEqualTo(1);
			
		} catch (Exception e) {
			fail(e);
		}
	}
}
