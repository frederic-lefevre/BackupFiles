package org.fl.backupFiles;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackUpJobs {

	private Vector<BackUpJob> backUpJobs ;
	private Logger bLog ;
	
	// Class to hold all the back up job
	// Each back up job is defined by a Json configuration
	// All the Json configurations are in files located in the directory path "configFilesDir"
	// which is passed in parameter of the constructor
	public BackUpJobs(Path configFilesDir, Logger l) {
		
		bLog = l ;
		backUpJobs = new Vector<BackUpJob>() ;
		
		try (DirectoryStream<Path> paths = Files.newDirectoryStream(configFilesDir, "*.json")) {
			for (Path configFile : paths) {
				bLog.fine("Find config file " + configFile.toString());
				String jsonConfig =  new String(Files.readAllBytes(configFile)) ;
				bLog.finest("New configuration : " + jsonConfig);
				backUpJobs.addElement(new BackUpJob(jsonConfig, bLog));
			}
		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception when getting config files in " + configFilesDir, e) ;
		}
	}

	// Get the list of back up jobs
	public Vector<BackUpJob> getBackUpJobs() {
		return backUpJobs;
	}

}
