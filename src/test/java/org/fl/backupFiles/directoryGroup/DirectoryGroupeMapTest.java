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

import java.nio.file.Paths;

import org.fl.backupFiles.directoryGroup.core.DirectoryGroup;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroupAll;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroupSub;
import org.junit.jupiter.api.Test;

class DirectoryGroupeMapTest {

	private final static String JSON_CONF = """
		[{\"path\" : \"/FredericPersonnel/photos/\",\"permanence\" : \"HIGH\", \"groupPolicy\" : \"DO_NOT_GROUP\"},
		 {\"path\" : \"/FredericPersonnel/tmp/\",   \"permanence\" : \"MEDIUM\", \"groupPolicy\" : \"GROUP_SUB_ITEMS\"},
		 {\"path\" : \"/FredericPersonnel/tmp/low/insideMedium\",   \"permanence\" : \"LOW\", \"groupPolicy\" : \"GROUP_ALL\"}]
		 """;
	
	@Test
	void testDoNotGroup() {
		
		DirectoryGroupMap directoryGroupmMap = new DirectoryGroupMap(JSON_CONF);
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(Paths.get("C:\\FredericPersonnel\\photos\\bidon"));
		assertThat(group.getPath()).isEqualTo(Paths.get("/FredericPersonnel/photos"));
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.HIGH);
		assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.DO_NOT_GROUP);
		assertThat(group).isInstanceOf(DirectoryGroup.class);
	}
	
	@Test
	void testGroupSub() {
		
		DirectoryGroupMap directoryGroupmMap = new DirectoryGroupMap(JSON_CONF);
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(Paths.get("C:\\FredericPersonnel\\tmp\\bidon"));
		assertThat(group.getPath()).isEqualTo(Paths.get("/FredericPersonnel/tmp"));
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.MEDIUM);
		assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.GROUP_SUB_ITEMS);
		assertThat(group).isInstanceOf(DirectoryGroupSub.class);
	}

	@Test
	void testGroupAll() {
		
		DirectoryGroupMap directoryGroupmMap = new DirectoryGroupMap(JSON_CONF);
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(Paths.get("C:\\FredericPersonnel\\tmp\\low\\insideMedium\\bidon"));
		assertThat(group.getPath()).isEqualTo(Paths.get("/FredericPersonnel/tmp/low/insideMedium"));
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.LOW);
		assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.GROUP_ALL);
		assertThat(group).isInstanceOf(DirectoryGroupAll.class);

	}
	
	@Test
	void testGroupDefault() {
		
		DirectoryGroupMap directoryGroupmMap = new DirectoryGroupMap(JSON_CONF);
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(Paths.get("C:\\FredericPersonnel\\default"));
		assertThat(group.getPath()).isEqualTo(Paths.get("/"));
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryGroupMap.DEFAULT_PERMANENCE_LEVEL);
		assertThat(group.getGroupPolicy()).isEqualTo(DirectoryGroupMap.DEFAULT_GROUP_POLICY);	
	}
	
	@Test
	void testGroupRoot() {
		
		DirectoryGroupMap directoryGroupmMap = new DirectoryGroupMap(JSON_CONF);
		
		DirectoryGroup group = directoryGroupmMap.getDirectoryGroup(Paths.get("C:\\FredericPersonnel\\photos"));
		assertThat(group.getPath()).isEqualTo(Paths.get("/FredericPersonnel/photos"));
		assertThat(group.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.HIGH);
		assertThat(group.getGroupPolicy()).isEqualTo(GroupPolicy.DO_NOT_GROUP);
		assertThat(group).isInstanceOf(DirectoryGroup.class);
	}
}
