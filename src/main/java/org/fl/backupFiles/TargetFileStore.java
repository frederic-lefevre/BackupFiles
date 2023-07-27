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

package org.fl.backupFiles;

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.NumberFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.util.file.FilesUtils;

public class TargetFileStore {

	private final FileStore fileStore ;
	private final Path mountPoint ;
	private final Logger tLog ;
	private long initialRemainingSpace ;
	
	public TargetFileStore(Path path, Logger l) {
		
		tLog = l ;
		FileStore fs = null ;
		Path	  mp = null ;
		if ((path != null) && (Files.exists(path))) {
			try {
				fs  			  	  = Files.getFileStore(path) ;
				mp 			  		  = FilesUtils.getMountPoint(path, tLog) ;
				initialRemainingSpace = fs.getUsableSpace() ;
			} catch (IOException e) {	
				tLog.log(Level.SEVERE, "IOException when getting filestore and mount point for " + path, e) ;
			}
		}
		fileStore   = fs ;
		mountPoint = mp ;
	}

	public void memorizeInitialRemainingSpace() {
		if (fileStore != null) {
			try {
				initialRemainingSpace = fileStore.getUsableSpace() ;
			} catch (IOException e) {	
				tLog.log(Level.SEVERE, "IOException when getting remaining space for " + mountPoint, e) ;
			}
		} else {
			tLog.severe("Ste initial remaining space of a fileStore : Null filestore");
		}
	}

	public void getSpaceEvolution(StringBuilder result) {
		
		if (fileStore != null) {
			
			result.append(fileStore.name()).append(" ").append(mountPoint).append(" = ") ;
			try {
				long currentSpace = fileStore.getUsableSpace() ;
				long difference = currentSpace - initialRemainingSpace ;
				
				result.append( NumberFormat.getNumberInstance().format(currentSpace)).append(" ( ") ;
				if (difference > 0) {
					result.append("+") ;
				} 
				result.append(NumberFormat.getNumberInstance().format(difference)).append(" ) bytes") ;
				
			} catch (IOException e) {
				String error = "IOException getting usable space of filestore " + fileStore.name() ;
				tLog.log(Level.SEVERE, error, e);
				result.append(error) ;
			}
		}
	}

	public FileStore getFileStore() {
		return fileStore;
	}
}
