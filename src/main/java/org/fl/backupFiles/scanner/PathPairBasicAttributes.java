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

package org.fl.backupFiles.scanner;

import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

// The purpose of this class is to make one and only one access to the source and target paths attributes
// (exists or not, size, last modified time... etc)
public class PathPairBasicAttributes {

	private static final Logger pLog = Logger.getLogger(PathPairBasicAttributes.class.getName());
	
	private final Path sourcePath;
	private Path targetPath;
	private boolean sourcePathAttributesKnown;
	private boolean targetPathAttributesKnown;
	private boolean sourceExists;
	private boolean targetExists;

	private BasicFileAttributes sourceBasicAttributes;
	private BasicFileAttributes targetBasicAttributes;

	public PathPairBasicAttributes(Path sourcePath, Path targetPath) {

		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		sourcePathAttributesKnown = false;
		targetPathAttributesKnown = false;
	}
	
	public Path getSourcePath() {
		return sourcePath;
	}

	public Path getTargetPath() {
		return targetPath;
	}

	public BasicFileAttributes getSourceBasicAttributes() {
		// Lazy get, to avoid to access file system if it is not needed
		
		if (!sourcePathAttributesKnown) {
			if (sourcePath == null) {
				sourcePathAttributesKnown = true;
				sourceBasicAttributes = null;
				sourceExists = false;
			} else {
				try {
					sourceBasicAttributes = sourcePath.getFileSystem().provider().readAttributesIfExists(sourcePath, BasicFileAttributes.class);
					sourcePathAttributesKnown = true;
					sourceExists = sourceBasicAttributes != null;
				} catch (Exception e) {
					pLog.log(Level.SEVERE, "Exception when getting basic file attributes of " + Objects.toString(sourcePath), e);
				}
			}
		}
		return sourceBasicAttributes;
	}

	public BasicFileAttributes getTargetBasicAttributes() {
		// Lazy get, to avoid to access file system if it is not needed
		
		if (!targetPathAttributesKnown) {
			if (targetPath == null) {
				targetPathAttributesKnown = true;
				targetBasicAttributes = null;
				targetExists = false;
			} else {
				try {
					targetBasicAttributes = targetPath.getFileSystem().provider().readAttributesIfExists(targetPath, BasicFileAttributes.class);
					targetPathAttributesKnown = true;
					targetExists = targetBasicAttributes != null;
				} catch (Exception e) {
					pLog.log(Level.SEVERE, "Exception when getting basic file attributes of " + Objects.toString(targetPath), e);
				}
			}
		}
		return targetBasicAttributes;
	}
	
	public boolean sourceExists() {
		getSourceBasicAttributes();
		return sourceExists;
	}

	public boolean targetExists() {
		getTargetBasicAttributes();
		return targetExists;
	}

	// sourcePath must exists
	public long getSourceSize() {
		return getSourceBasicAttributes().size();
	}
	
	// targetPath must exists
	public long getTargetSize() {
		return getTargetBasicAttributes().size();
	}
	
	// True if source exists and is a directory
	// false otherwise
	public boolean sourceIsDirectory() {
		return (sourceExists() && sourceBasicAttributes.isDirectory());
	}
	
	// True if source exists and is a directory
	// false otherwise
	public boolean targetIsDirectory() {
		return (targetExists() && targetBasicAttributes.isDirectory());
	}
	
	public boolean noTargetPath() {
		return (targetPath == null) ;
	}

	public boolean noSourcePath() {
		return (sourcePath == null) ;
	}
	
	public void setTargetPath(Path targetPath) {
		this.targetPath = targetPath;
		targetPathAttributesKnown = false;
	}
	
	public static PathPairBasicAttributes getClosestExistingParentBasicAttributes(Path path) {
		
		Path parentPath = path.getParent();
		if (parentPath == null) {
			return null;
		} else {
			PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(parentPath, null);
			if (pathPairBasicAttributes.sourceExists()) {
				return pathPairBasicAttributes;
			} else {
				return getClosestExistingParentBasicAttributes(parentPath.getParent());
			}
		}
	}
}
