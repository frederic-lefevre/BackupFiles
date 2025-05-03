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

import org.junit.jupiter.api.Test;

class DirectoryGroupeMapTest {

	private final static String JSON_CONF = """
		[{\"path\" : \"/FredericPersonnel/photos/\",\"permanence\" : \"HIGH\", \"groupPolicy\" : \"DO_NOT_GROUP\"},
		 {\"path\" : \"/FredericPersonnel/tmp/\",   \"permanence\" : \"MEDIUM\", \"groupPolicy\" : \"GROUP_SUB_ITEMS\"},
		 {\"path\" : \"/FredericPersonnel/tmp/low/insideMedium\",   \"permanence\" : \"LOW\", \"groupPolicy\" : \"GROUP_ALL\"}]
		 """;
	
	@Test
	void test() {
		
		DirectoryGroupMap directoryGroupmMap = new DirectoryGroupMap(JSON_CONF);
		
		DirectoryGroup group1 = directoryGroupmMap.getDirectoryGroup(Paths.get("C:\\FredericPersonnel\\photos\\bidon"));
		assertThat(group1.getPath()).isEqualTo(Paths.get("/FredericPersonnel/photos"));
		assertThat(group1.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.HIGH);
		assertThat(group1.getGroupPolicy()).isEqualTo(GroupPolicy.DO_NOT_GROUP);
		
		DirectoryGroup group2 = directoryGroupmMap.getDirectoryGroup(Paths.get("C:\\FredericPersonnel\\tmp\\bidon"));
		assertThat(group2.getPath()).isEqualTo(Paths.get("/FredericPersonnel/tmp"));
		assertThat(group2.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.MEDIUM);
		assertThat(group2.getGroupPolicy()).isEqualTo(GroupPolicy.GROUP_SUB_ITEMS);

		DirectoryGroup group3 = directoryGroupmMap.getDirectoryGroup(Paths.get("C:\\FredericPersonnel\\tmp\\low\\insideMedium\\bidon"));
		assertThat(group3.getPath()).isEqualTo(Paths.get("/FredericPersonnel/tmp/low/insideMedium"));
		assertThat(group3.getPermanenceLevel()).isEqualTo(DirectoryPermanenceLevel.LOW);
		assertThat(group3.getGroupPolicy()).isEqualTo(GroupPolicy.GROUP_ALL);

		DirectoryGroup group4 = directoryGroupmMap.getDirectoryGroup(Paths.get("C:\\FredericPersonnel\\default"));
		assertThat(group4.getPath()).isEqualTo(Paths.get("/"));
		assertThat(group4.getPermanenceLevel()).isEqualTo(DirectoryGroupMap.DEFAULT_PERMANENCE_LEVEL);
		assertThat(group4.getGroupPolicy()).isEqualTo(DirectoryGroupMap.DEFAULT_GROUP_POLICY);
		
	}

}
