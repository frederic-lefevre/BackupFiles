package org.fl.backupFiles.directoryPermanence;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Paths;
import java.util.logging.Logger;

import org.junit.jupiter.api.Test;

class DirectoryPermanenceMapTest {

	private final static String JSON_CONF = 
		"[{\"path\" : \"/FredericPersonnel/photos/\",\"permanence\" : \"HIGH\"}," +
		" {\"path\" : \"/FredericPersonnel/tmp/\",   \"permanence\" : \"MEDIUM\"}," +
		" {\"path\" : \"/FredericPersonnel/tmp/low/insideMedium\",   \"permanence\" : \"LOW\"}]" ;
	
	@Test
	void test() {
		
		DirectoryPermanenceMap permMap = new DirectoryPermanenceMap(JSON_CONF, Logger.getGlobal()) ;
		
		DirectoryPermanenceLevel lvl1 = permMap.getPermanenceLevel(Paths.get("C:\\FredericPersonnel\\photos\\bidon")) ;
		assertEquals(DirectoryPermanenceLevel.HIGH, lvl1) ;
		
		DirectoryPermanenceLevel lvl2 = permMap.getPermanenceLevel(Paths.get("C:\\FredericPersonnel\\tmp\\bidon")) ;
		assertEquals(DirectoryPermanenceLevel.MEDIUM, lvl2) ;

		DirectoryPermanenceLevel lvl3 = permMap.getPermanenceLevel(Paths.get("C:\\FredericPersonnel\\tmp\\low\\insideMedium\\bidon")) ;
		assertEquals(DirectoryPermanenceLevel.LOW, lvl3) ;

		DirectoryPermanenceLevel lvl4 = permMap.getPermanenceLevel(Paths.get("C:\\FredericPersonnel\\default")) ;
		assertEquals(DirectoryPermanence.DEFAULT_PERMANENCE_LEVEL, lvl4) ;
		

	}

}
