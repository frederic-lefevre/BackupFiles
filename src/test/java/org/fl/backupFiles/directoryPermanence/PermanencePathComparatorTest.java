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

}
