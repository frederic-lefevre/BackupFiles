package org.fl.backupFiles.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PathPairBasicAttributes {

	private final Path   		sourcePath ;
	private Path   				targetPath ;
	
	private BasicFileAttributes sourceBasicAttributes ;
	
	private BasicFileAttributes targetBasicAttributes ;
	private Logger 				pLog ;
	
	public PathPairBasicAttributes(Path p, Logger l) {
		
		sourcePath = p ;
		targetPath = null ;		
		pLog 	   = l ;
	}

	public Path getSourcePath() {
		return sourcePath;
	}

	public Path getTargetPath() {
		return targetPath;
	}

	public BasicFileAttributes getSourceBasicAttributes() {
		// Lazy get, to avoid to access file system if it is not needed
		
		if (sourceBasicAttributes == null) {
			try {
				sourceBasicAttributes = Files.readAttributes(sourcePath, BasicFileAttributes.class);
			} catch (IOException e) {
				pLog.log(Level.SEVERE, "IOException when getting basic file attributes of " + sourcePath, e);
			}
		}
		return sourceBasicAttributes;
	}

	public BasicFileAttributes getTargetBasicAttributes() {
		// Lazy get, to avoid to access file system if it is not needed
		
		if (targetBasicAttributes == null) {
			try {
				targetBasicAttributes = Files.readAttributes(targetPath, BasicFileAttributes.class);
			} catch (IOException e) {
				pLog.log(Level.SEVERE, "IOException when getting basic file attributes of " + targetPath, e);
			}
		}
		return targetBasicAttributes;
	}
	
	public boolean noTargetPath() {
		return (targetPath == null) ;
	}

	public void setTargetPath(Path targetPath) {
		this.targetPath = targetPath;
	}
}
