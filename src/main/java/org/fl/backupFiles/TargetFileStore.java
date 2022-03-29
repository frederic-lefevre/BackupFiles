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
