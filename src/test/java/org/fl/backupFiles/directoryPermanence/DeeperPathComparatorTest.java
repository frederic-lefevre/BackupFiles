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

package org.fl.backupFiles.directoryPermanence;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.TreeMap;

import org.junit.jupiter.api.Test;

class DeeperPathComparatorTest {
	
	@Test
	void shouldOrderTreeMap() {

		TreeMap<Path, String> pathsMap = new TreeMap<Path, String>(new DeeperPathComparator());

		pathsMap.put(Paths.get("/Fred"), "f");
		pathsMap.put(Paths.get("/Fred/Pers"), "d");
		pathsMap.put(Paths.get("/Fred/tmp"), "e");
		pathsMap.put(Paths.get("/Fred/Pers/Photos"), "c");
		pathsMap.put(Paths.get("/Fred/Pers/Photos/tmp"), "b");
		pathsMap.put(Paths.get("/Fred/Pers/famille"), "a");

		Set<Path> pathKeys = pathsMap.keySet();
		assertThat(pathKeys)
			.hasToString("[\\Fred\\Pers\\famille, \\Fred\\Pers\\Photos\\tmp, \\Fred\\Pers\\Photos, \\Fred\\Pers, \\Fred\\tmp, \\Fred]");

	}

	@Test
	void samePathsShouldBeEqual() {
		
		DeeperPathComparator deeperPathComparator = new DeeperPathComparator();
		
		int comp = deeperPathComparator.compare(Paths.get("/toto/titi/tata"), Paths.get("/toto/titi/tata"));
		assertThat(comp).isZero();
	}
	
	@Test
	void deeperPathShouldBeNegative() {
		
		DeeperPathComparator deeperPathComparator = new DeeperPathComparator();
		
		int comp = deeperPathComparator.compare(Paths.get("/toto/titi/tata"), Paths.get("/toto/titi"));
		assertThat(comp).isNegative();
	}
	
	@Test
	void shorterPathShouldBePositive() {
		
		DeeperPathComparator deeperPathComparator = new DeeperPathComparator();
		
		int comp = deeperPathComparator.compare(Paths.get("/toto/titi"), Paths.get("/toto/titi/tata"));
		assertThat(comp).isPositive();
	}
	
	@Test
	void sameLevelPathShouldBeOrderedLexicographically() {
		
		DeeperPathComparator deeperPathComparator = new DeeperPathComparator();
		
		int comp = deeperPathComparator.compare(Paths.get("/toto/titi/tutu"), Paths.get("/toto/titi/tata"));
		assertThat(comp).isPositive();
	}
	
	@Test
	void separatedBranchPathShouldBeOrderedLexicographically() {
		
		DeeperPathComparator deeperPathComparator = new DeeperPathComparator();
		
		int comp = deeperPathComparator.compare(Paths.get("/toto/titi/tutu/tete"), Paths.get("/toto/titi/tata"));
		assertThat(comp).isPositive();
	}
	
	@Test
	void emptyPathShouldBeNegative() {
		
		DeeperPathComparator deeperPathComparator = new DeeperPathComparator();
		
		int comp = deeperPathComparator.compare(Paths.get(""), Paths.get("/"));
		assertThat(comp).isNegative();
	}
	
	@Test
	void rootPathShouldBePositive() {
		
		DeeperPathComparator deeperPathComparator = new DeeperPathComparator();
		
		int comp = deeperPathComparator.compare(Paths.get("/"), Paths.get("/t"));
		assertThat(comp).isPositive();
	}
	
	@Test
	void nullArgumentShouldThrowNPE() {
		
		DeeperPathComparator deeperPathComparator = new DeeperPathComparator();
		assertThatNullPointerException().isThrownBy(() ->
			deeperPathComparator.compare(null, Paths.get("/t")));
	}
	
	@Test
	void nullArgument2ShouldThrowNPE() {
		
		DeeperPathComparator deeperPathComparator = new DeeperPathComparator();
		assertThatNullPointerException().isThrownBy(() ->
			deeperPathComparator.compare(Paths.get("/t"), null));
	}
	
	@Test
	void twoNullArgumentShouldThrowNPE() {
		
		DeeperPathComparator deeperPathComparator = new DeeperPathComparator();
		assertThatNullPointerException().isThrownBy(() ->
			deeperPathComparator.compare(Paths.get("/t"), null));
	}
}
