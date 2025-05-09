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

import java.net.URISyntaxException;
import java.nio.file.Path;

import org.fl.backupFiles.directoryGroup.core.DirectoryGroup;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroupAll;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroupSub;
import org.fl.util.file.FilesUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class DirectoryGroupeMapTest {

	private final static String JSON_CONF = """
		[{\"path\" : \"/FredericPersonnel/photos/\",\"permanence\" : \"HIGH\", \"groupPolicy\" : \"DO_NOT_GROUP\"},
		 {\"path\" : \"/FredericPersonnel/tmp/\",   \"permanence\" : \"MEDIUM\", \"groupPolicy\" : \"GROUP_SUB_ITEMS\"},
		 {\"path\" : \"/FredericPersonnel/tmp/low/insideMedium\",   \"permanence\" : \"LOW\", \"groupPolicy\" : \"GROUP_ALL\"}]
		 """;
	
	private static DirectoryGroupMap directoryGroupmMap;
	private static DirectoryGroupConfiguration directoryGroupConfiguration;
	
	@BeforeAll
	static void init() throws Exception {
		Path sourcePath = FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/");
		directoryGroupConfiguration = new DirectoryGroupConfiguration(JSON_CONF);
		directoryGroupmMap = new DirectoryGroupMap(sourcePath, sourcePath, directoryGroupConfiguration);
	}
	
	@Test
	void testGroupPathStartsWithSourcePath() throws URISyntaxException {
		
		Path sourcePath = FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/photos/tmp");
		DirectoryGroupMap directoryGroupmMap =  new DirectoryGroupMap(sourcePath, sourcePath, directoryGroupConfiguration);
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/photos/tmp/bidon"));
		assertThat(group.getPath()).isEqualTo(sourcePath);
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.HIGH);
		assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.DO_NOT_GROUP);
		assertThat(group).isInstanceOf(DirectoryGroup.class);
	}
	
	@Test
	void testGroupPathStartsWithSourcePath2() throws URISyntaxException {
		
		Path sourcePath = FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/tmp/tmp");
		DirectoryGroupMap directoryGroupmMap =  new DirectoryGroupMap(sourcePath, sourcePath, directoryGroupConfiguration);
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/tmp/tmp/bidon"));
		assertThat(group.getPath()).isEqualTo(sourcePath);
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.MEDIUM);
		assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.GROUP_SUB_ITEMS);
		assertThat(group).isInstanceOf(DirectoryGroupSub.class);
	}
	
	@Test
	void testGroupPathStartsWithSourcePath3() throws URISyntaxException {
		
		Path sourcePath = FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/tmp/low");
		DirectoryGroupMap directoryGroupmMap =  new DirectoryGroupMap(sourcePath, sourcePath, directoryGroupConfiguration);
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/tmp/low/bidon"));
		assertThat(group.getPath()).isEqualTo(sourcePath);
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.MEDIUM);
		assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.GROUP_SUB_ITEMS);
		assertThat(group).isInstanceOf(DirectoryGroupSub.class);
		
		DirectoryGroup groupLow = directoryGroupmMap.getDirectoryGroup(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/tmp/low/insideMedium/bidon"));
		assertThat(groupLow.getPath()).isEqualTo(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/tmp/low/insideMedium"));
		assertThat(groupLow.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.LOW);
		assertThat(groupLow.getGroupPolicy()).isEqualTo(GroupPolicy.GROUP_ALL);
		assertThat(groupLow).isInstanceOf(DirectoryGroupAll.class);
	}
	
	@Test
	void testMap() {
		assertThat(directoryGroupmMap).isNotNull();
	}
	
	@Test
	void testNullJson() throws URISyntaxException {
		
		assertThatNullPointerException().isThrownBy(() -> new DirectoryGroupMap(Path.of("/"), Path.of("/"), null));
	}
	
	@Test
	void testAllNull() throws URISyntaxException {
		
		assertThatNullPointerException().isThrownBy(() ->  new DirectoryGroupMap(null, null, null));
	}
	
	@Test
	void originalSourcePathNullShouldRaiseError() throws URISyntaxException {
		
		assertThatNullPointerException().isThrownBy(() -> new DirectoryGroupMap(null, Path.of("/"), directoryGroupConfiguration));
	}
	
	@Test
	void originalActualPathNullShouldRaiseError() throws URISyntaxException {
		
		assertThatNullPointerException().isThrownBy(() -> new DirectoryGroupMap(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/photos/"), null, directoryGroupConfiguration));
	}
	
	@Test
	void testDoNotGroup() throws URISyntaxException {
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/photos/bidon"));
		assertThat(group.getPath()).isEqualTo(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/photos"));
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.HIGH);
		assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.DO_NOT_GROUP);
		assertThat(group).isInstanceOf(DirectoryGroup.class);
	}
	
	@Test
	void testGroupSub() throws URISyntaxException {
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/tmp/bidon"));
		assertThat(group.getPath()).isEqualTo(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/tmp"));
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.MEDIUM);
		assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.GROUP_SUB_ITEMS);
		assertThat(group).isInstanceOf(DirectoryGroupSub.class);
	}

	@Test
	void testGroupAll() throws URISyntaxException {
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/tmp/low/insideMedium/bidon"));
		assertThat(group.getPath()).isEqualTo(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/tmp/low/insideMedium"));
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.LOW);
		assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.GROUP_ALL);
		assertThat(group).isInstanceOf(DirectoryGroupAll.class);

	}
	
	@Test
	void testGroupDefault() throws Exception {
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/default"));
		assertThat(group.getPath()).isEqualTo(Path.of("/"));
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryGroupMap.DEFAULT_PERMANENCE_LEVEL);
		assertThat(group.getGroupPolicy()).isEqualTo(DirectoryGroupMap.DEFAULT_GROUP_POLICY);	
	}
	
	@Test
	void testGroupDefault2() throws URISyntaxException {
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(FilesUtils.uriStringToAbsolutePath("file:///any/path"));
		assertThat(group.getPath()).isEqualTo(Path.of("/"));
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryGroupMap.DEFAULT_PERMANENCE_LEVEL);
		assertThat(group.getGroupPolicy()).isEqualTo(DirectoryGroupMap.DEFAULT_GROUP_POLICY);	
	}
	
	@Test
	void testGroupRoot() throws URISyntaxException {
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/photos"));
		assertThat(group.getPath()).isEqualTo(FilesUtils.uriStringToAbsolutePath("file:///FredericPersonnel/photos"));
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.HIGH);
		assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.DO_NOT_GROUP);
		assertThat(group).isInstanceOf(DirectoryGroup.class);
	}
}
