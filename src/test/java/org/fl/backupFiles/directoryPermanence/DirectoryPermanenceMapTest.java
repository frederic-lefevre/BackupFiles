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

package org.fl.backupFiles.directoryPermanence;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

class DirectoryPermanenceMapTest {

	private final static String JSON_CONF = 
		"[{\"path\" : \"/FredericPersonnel/photos/\",\"permanence\" : \"HIGH\"}," +
		" {\"path\" : \"/FredericPersonnel/tmp/\",   \"permanence\" : \"MEDIUM\"}," +
		" {\"path\" : \"/FredericPersonnel/tmp/low/insideMedium\",   \"permanence\" : \"LOW\"}]";
	
	@Test
	void test() {
		
		DirectoryPermanenceMap permMap = new DirectoryPermanenceMap(JSON_CONF);
		
		DirectoryPermanenceLevel lvl1 = permMap.getPermanenceLevel(Paths.get("C:\\FredericPersonnel\\photos\\bidon"));
		assertThat(lvl1).isEqualTo(DirectoryPermanenceLevel.HIGH);
		
		DirectoryPermanenceLevel lvl2 = permMap.getPermanenceLevel(Paths.get("C:\\FredericPersonnel\\tmp\\bidon"));
		assertThat(lvl2).isEqualTo(DirectoryPermanenceLevel.MEDIUM);

		DirectoryPermanenceLevel lvl3 = permMap.getPermanenceLevel(Paths.get("C:\\FredericPersonnel\\tmp\\low\\insideMedium\\bidon"));
		assertThat(lvl3).isEqualTo(DirectoryPermanenceLevel.LOW);

		DirectoryPermanenceLevel lvl4 = permMap.getPermanenceLevel(Paths.get("C:\\FredericPersonnel\\default"));
		assertThat(lvl4).isEqualTo(DirectoryPermanence.DEFAULT_PERMANENCE_LEVEL);
		
	}

}
