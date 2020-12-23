package org.fl.backupFiles;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackUpJobList extends Vector<BackUpJob> {

	private static final long serialVersionUID = 1L;
	private final Logger bLog ;
	
	// Class to hold all the back up job
	// Each back up job is defined by a Json configuration
	// All the Json configurations are in files located in the directory path "configFilesDir"
	// which is passed in parameter of the constructor
	public BackUpJobList(Path configFilesDir, Logger l) {
		
		super() ;
		bLog = l ;
		
		try (DirectoryStream<Path> paths = Files.newDirectoryStream(configFilesDir, "*.json")) {
			for (Path configFile : paths) {
				if (bLog.isLoggable(Level.FINE)) {
					bLog.fine("Find config file " + configFile.toString());
				}
				String jsonConfig =  new String(Files.readAllBytes(configFile)) ;
				if (bLog.isLoggable(Level.FINEST)) {
					bLog.finest("New configuration : " + jsonConfig);
				}
				addElement(new BackUpJob(jsonConfig, bLog));
			}
			if (size() == 0) {
				bLog.warning("No back up job config file found in " + configFilesDir.toString());
			}
		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception when getting config files in " + configFilesDir, e) ;
		}
	}

}
