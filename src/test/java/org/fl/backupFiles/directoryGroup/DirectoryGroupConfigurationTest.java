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

package org.fl.backupFiles.directoryGroup;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.directoryGroup.core.DirectoryGroup;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroupAll;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroupBuilder;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroupSub;
import org.fl.util.FilterCounter;
import org.fl.util.FilterCounter.LogRecordCounter;
import org.fl.util.file.FilesUtils;
import org.junit.jupiter.api.Test;

class DirectoryGroupConfigurationTest {


	private final static String JSON_CONF = """
		[{\"path\" : \"/FredericPersonnel/photos/\",\"permanence\" : \"HIGH\", \"groupPolicy\" : \"DO_NOT_GROUP\"},
		 {\"path\" : \"/FredericPersonnel/tmp/\",   \"permanence\" : \"MEDIUM\", \"groupPolicy\" : \"GROUP_SUB_ITEMS\"},
		 {\"path\" : \"/FredericPersonnel/tmp/low/insideMedium\",   \"permanence\" : \"LOW\", \"groupPolicy\" : \"GROUP_ALL\"}]
		 """;
	
	@Test
	void testDirectoryGroupConfiguration() {
		
		DirectoryGroupConfiguration directoryGroupConfiguration = new DirectoryGroupConfiguration(JSON_CONF);
		
		assertThat(directoryGroupConfiguration).isNotNull();
		
		List<DirectoryGroup> directoryGroupList = directoryGroupConfiguration.getDirectoryGroupList();
		
		assertThat(directoryGroupList).isNotNull().isNotEmpty()
			.satisfiesExactlyInAnyOrder(
					group -> {
						assertThat(group.getPath()).isEqualTo(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/photos"));
						assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.HIGH);
						assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.DO_NOT_GROUP);
						assertThat(group).isInstanceOf(DirectoryGroup.class); },
					group -> { 
						assertThat(group.getPath()).isEqualTo(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/tmp/low/insideMedium"));
						assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.LOW);
						assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.GROUP_ALL);
						assertThat(group).isInstanceOf(DirectoryGroupAll.class); },
					group -> {
						assertThat(group.getPath()).isEqualTo(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/tmp"));
						assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.MEDIUM);
						assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.GROUP_SUB_ITEMS);
						assertThat(group).isInstanceOf(DirectoryGroupSub.class); }
					);
	}
	
	@Test
	void tresultListShouldBeUnmodifiable() {

		assertThatExceptionOfType(UnsupportedOperationException.class)
			.isThrownBy(() -> 
				new DirectoryGroupConfiguration(JSON_CONF).getDirectoryGroupList()
					.add(DirectoryGroupBuilder.build(Path.of("/"), DirectoryPermanenceLevel.LOW, GroupPolicy.GROUP_ALL)));
	}
	
	
	@Test
	void testNullDirectoryGroupConfiguration() {
		
		DirectoryGroupConfiguration directoryGroupConfiguration = new DirectoryGroupConfiguration(null);
		assertThat(directoryGroupConfiguration).isNotNull();
		assertThat(directoryGroupConfiguration.getDirectoryGroupList()).isNotNull().isEmpty();
	}
	
	@Test
	void testEmptyDirectoryGroupConfiguration() {
		
		DirectoryGroupConfiguration directoryGroupConfiguration = new DirectoryGroupConfiguration("");
		assertThat(directoryGroupConfiguration).isNotNull();
		assertThat(directoryGroupConfiguration.getDirectoryGroupList()).isNotNull().isEmpty();
	}
	
	@Test
	void wrongDirectoryGroupConfigurationShouldRaiseError() {
		
		LogRecordCounter logCounter = 
				FilterCounter.getLogRecordCounter(Logger.getLogger(DirectoryGroupConfiguration.class.getName()));
		
		DirectoryGroupConfiguration directoryGroupConfiguration = new DirectoryGroupConfiguration("no Json here");
		assertThat(directoryGroupConfiguration).isNotNull();
		assertThat(directoryGroupConfiguration.getDirectoryGroupList()).isNotNull().isEmpty();
		
		assertThat(logCounter.getLogRecordCount()).isEqualTo(1);
		assertThat(logCounter.getLogRecordCount(Level.SEVERE)).isEqualTo(1);
	}
}
