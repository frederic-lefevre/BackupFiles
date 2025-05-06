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

import java.nio.file.FileStore;
import java.nio.file.Path;

import org.fl.backupFiles.directoryGroup.DirectoryPermanenceLevel;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroup;

public abstract class AbstractBackUpItem {
	
	protected final Path sourcePath;
	protected final Path targetPath;
	protected final BackupAction backupAction;
	protected long sizeDifference;
	protected BackupStatus backupStatus;
	protected final long fileSizeWarningThreshold;
	protected final FileStore targetFileStore;
	protected final DirectoryGroup directoryGroup;
	protected long backUpItemNumber;
	protected final BackUpTask backUpTask;
	protected final Path sourceClosestExistingPath;
	
	protected AbstractBackUpItem(Path sourcePath, Path targetPath, Path sourceClosestExistingPath, BackupAction backupAction, BackupStatus backupStatus, BackUpTask backUpTask) {
		
		super();
		
		if (backupAction == null) {
			throw new IllegalBackupActionException("Illegal null backup action");
		}
		if (backupStatus == null) {
			throw new IllegalArgumentException("Illegal null backup status");
		}
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		this.sourceClosestExistingPath = sourceClosestExistingPath;
		this.backupAction = backupAction;
		this.backupStatus = backupStatus;
		
		fileSizeWarningThreshold = backUpTask.getSizeWarningLimit();
		targetFileStore = backUpTask.getTargetFileStore();
		
		if (sourcePath != null) {
			directoryGroup = backUpTask.getDirectoryGroupMap().getDirectoryGroup(sourcePath);
		} else {
			directoryGroup = backUpTask.getDirectoryGroupMap().getDirectoryGroup(sourceClosestExistingPath);
		}
		this.backUpTask = backUpTask;
	}
	
	public abstract boolean execute(BackUpCounters backUpCounters);
	
	public abstract boolean isAboveFileSizeLimitThreshold();
	
	public abstract void sumIndividualCounters(BackUpCounters backUpCounters);
	 
	public Path getSourcePath() {
		return sourcePath;
	}

	public Path getTargetPath() {
		return targetPath; 
	}
	
	public BackupAction getBackupAction() {
		return backupAction;
	}

	public BackupStatus getBackupStatus() {
		return backupStatus;
	}

	public DirectoryGroup getDirectoryGroup() {
		return directoryGroup;
	}
	
	public DirectoryPermanenceLevel getPermanenceLevel() {
		return directoryGroup.getPermanenceLevel();
	}

	public long getSizeDifference() {
		return sizeDifference;
	}

	public long getBackUpItemNumber() {
		return backUpItemNumber;
	}

	public BackUpTask getBackUpTask() {
		return backUpTask;
	}

	public Path getSourceClosestExistingPath() {
		return sourceClosestExistingPath;
	}

}
