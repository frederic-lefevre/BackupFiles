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

package org.fl.backupFiles.scanner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.Config;

public class PathPairBasicAttributes {

	private static final Logger pLog = Config.getLogger();
	
	private final Path sourcePath;
	private Path targetPath;

	private BasicFileAttributes sourceBasicAttributes;

	private BasicFileAttributes targetBasicAttributes;

	public PathPairBasicAttributes(Path p) {

		sourcePath = p;
		targetPath = null;
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
