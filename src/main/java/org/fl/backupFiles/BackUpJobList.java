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

package org.fl.backupFiles;

import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.directoryGroup.DirectoryGroupConfiguration;

public class BackUpJobList extends Vector<BackUpJob> {

	private static final Logger bLog = Logger.getLogger(BackUpJobList.class.getName());
	
	private static final long serialVersionUID = 1L;
	
	// Class to hold all the back up job
	// Each back up job is defined by a Json configuration
	// All the Json configurations are in files located in the directory path "configFilesDir"
	// which is passed in parameter of the constructor
	public BackUpJobList(Path configFilesDir) {

		super();

		DirectoryGroupConfiguration directoryGroupConfiguration = new DirectoryGroupConfiguration(Config.getBackupGroupConfiguration());
		
		try (DirectoryStream<Path> paths = Files.newDirectoryStream(configFilesDir, "*.json")) {
			for (Path configFile : paths) {
				bLog.fine(() -> "Find config file " + configFile.toString());
				String jsonConfig = new String(Files.readAllBytes(configFile), StandardCharsets.UTF_8);
				bLog.finest(() -> "New configuration : " + jsonConfig);
				addElement(new BackUpJob(jsonConfig, directoryGroupConfiguration));
			}
			if (size() == 0) {
				bLog.warning("No back up job config file found in " + configFilesDir.toString());
			}
		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception when getting config files in " + configFilesDir, e);
		}
	}

}
