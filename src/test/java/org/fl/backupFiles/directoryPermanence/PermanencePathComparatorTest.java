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

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeMap;
import org.fl.backupFiles.directoryPermanence.PermanencePathComparatorTest;

import org.junit.jupiter.api.Test;

class PermanencePathComparatorTest {
	
	@Test
	void test() {

		TreeMap<Path, String> permanenceMap = new TreeMap<Path, String>(new PermanencePathComparator());

		permanenceMap.put(Paths.get("/Fred"), "f");
		permanenceMap.put(Paths.get("/Fred/Pers"), "d");
		permanenceMap.put(Paths.get("/Fred/tmp"), "e");
		permanenceMap.put(Paths.get("/Fred/Pers/Photos"), "c");
		permanenceMap.put(Paths.get("/Fred/Pers/Photos/tmp"), "b");
		permanenceMap.put(Paths.get("/Fred/Pers/famille"), "a");

		Set<Path> pathKeys = permanenceMap.keySet();
		assertEquals(
				"[\\Fred\\Pers\\famille, \\Fred\\Pers\\Photos\\tmp, \\Fred\\Pers\\Photos, \\Fred\\Pers, \\Fred\\tmp, \\Fred]",
				pathKeys.toString());
	}

	@Test
	void test2() {
		
		PermanencePathComparator permComp = new PermanencePathComparator() ;
		
		int comp = permComp.compare(Paths.get("/toto/titi/tata"), Paths.get("/toto/titi/tata")) ;
		assertEquals(0, comp) ;
	}
}
