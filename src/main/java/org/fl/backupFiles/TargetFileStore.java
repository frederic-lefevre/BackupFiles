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

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TargetFileStore {

	private static final Logger tLog = Logger.getLogger(TargetFileStore.class.getName());

	private final FileStore fileStore;
	private final String name;
	private final String identification;
	private final long totalFileStoreSpace;
	private final long warningThresholdForRemainingSpace;
	private long remainingSpaceBeforeWarning;	
	private boolean sizeWarningRaised;
	private long initialRemainingSpace;
	private long potentialSizeChange;

	public TargetFileStore(FileStore fileStore, Path mountPoint, long warningThrehold) throws IOException {
		this.fileStore = fileStore;
		totalFileStoreSpace = fileStore.getTotalSpace();
		warningThresholdForRemainingSpace = (totalFileStoreSpace / 100)*warningThrehold;
		name = fileStore.name() + " " + mountPoint;
		identification ="fileStore=" + fileStore.name() + ", root folder=" + mountPoint;
		reset();
	}

	public long getPotentialSizeChange() {
		return potentialSizeChange;
	}

	public FileStore getFileStore() {
		return fileStore;
	}
	
	public long recordPotentialSizeChange(long sizeDifference) {
		potentialSizeChange = potentialSizeChange + sizeDifference;
		if ((potentialSizeChange > remainingSpaceBeforeWarning) && (!sizeWarningRaised)) {
			tLog.warning("Remaing space for " + getFileStoreIdentification() + " is too low: " + getRemainingSpace());
			sizeWarningRaised = true;
		}
		return potentialSizeChange;
	}
	
	public void reset() {
		potentialSizeChange = 0;
		initialRemainingSpace = getRemainingSpace();
		sizeWarningRaised = false;

		remainingSpaceBeforeWarning = initialRemainingSpace - warningThresholdForRemainingSpace;
		if (remainingSpaceBeforeWarning < 0) {
			tLog.warning("Remaing space for " + getFileStoreIdentification() + " is too low: " + initialRemainingSpace);
			sizeWarningRaised = true;
		}
	}
	
	private long getRemainingSpace() {
		try {
			return fileStore.getUsableSpace();
		} catch (IOException e) {	
			tLog.log(Level.SEVERE, "IOException when getting remaining space for " + getFileStoreIdentification(), e);
			return 0;
		}
	}
	
	public long getInitialRemainingSpace() {
		return initialRemainingSpace;
	}
	
	public long getWarningThresholdForRemainingSpace() {
		return warningThresholdForRemainingSpace;
	}

	private String getFileStoreIdentification() {
		return identification;
	}
	
	public String getName() {
		return name;
	}
	
	public long getUsableSpace() throws IOException {
		return fileStore.getUsableSpace();
	}
	
	public long getTotalSpace() {
		return totalFileStoreSpace;
	}
}
