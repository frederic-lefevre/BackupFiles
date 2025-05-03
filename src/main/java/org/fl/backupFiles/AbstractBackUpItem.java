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

import java.nio.file.Path;

import org.fl.backupFiles.directoryGroup.DirectoryPermanenceLevel;

public abstract class AbstractBackUpItem {

	protected final Path sourcePath;
	protected final Path targetPath;
	protected final BackupAction backupAction;
	protected long sizeDifference;
	protected BackupStatus backupStatus;
	protected final DirectoryPermanenceLevel permanenceLevel;
	protected long backUpItemNumber;
	
	protected AbstractBackUpItem(Path sourcePath, Path targetPath, BackupAction backupAction, BackupStatus backupStatus) {
		
		super();
		
		if (backupAction == null) {
			throw new IllegalBackupActionException("Illegal null backup action");
		}
		if (backupStatus == null) {
			throw new IllegalArgumentException("Illegal null backup status");
		}
		this.sourcePath = sourcePath;
		this.targetPath = targetPath;
		this.backupAction = backupAction;
		this.backupStatus = backupStatus;
		
		if (sourcePath != null) {
			permanenceLevel = Config.getDirectoryPermanence().getPermanenceLevel(sourcePath);
		} else {
			permanenceLevel = Config.getDirectoryPermanence().getPermanenceLevel(targetPath);
		}
	}
	
	public abstract boolean execute(BackUpCounters backUpCounters);
	
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

	public DirectoryPermanenceLevel getPermanenceLevel() {
		return permanenceLevel;
	}

	public long getSizeDifference() {
		return sizeDifference;
	}

	public long getBackUpItemNumber() {
		return backUpItemNumber;
	}

}
