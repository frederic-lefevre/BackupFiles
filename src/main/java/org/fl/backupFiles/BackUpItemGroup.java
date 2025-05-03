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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BackUpItemGroup extends AbstractBackUpItem {

	private final List<BackUpItem> backUpItems;
	private boolean isAboveFileSizeLimitThreshold;
	
	public BackUpItemGroup(Path sourcePath, Path targetPath, BackupAction backupAction, BackupStatus backupStatus, BackUpTask backUpTask) {
		
		super(sourcePath, targetPath, backupAction, backupStatus, backUpTask);
		this.sizeDifference = 0;
		this.backUpItemNumber = 0;

		this.backUpItems = new ArrayList<>();
		isAboveFileSizeLimitThreshold = false;
	}
	
	public List<BackUpItem> getBackUpItems() {
		return backUpItems;
	}

	public BackUpItemGroup addBackUpItem(BackUpItem backUpItem) {
		
		if (backUpItem.getBackupAction() != backupAction) {
			throw new IllegalArgumentException("backUpItem with action " + Objects.toString(backUpItem.getBackupAction()) + " added to BackUpItemGroup with action " + backupAction);
		}
		if (backUpItem.getBackupStatus() != backupStatus) {
			throw new IllegalArgumentException("backUpItem with status " + Objects.toString(backUpItem.getBackupStatus()) + " added to BackUpItemGroup with status " + backupStatus);
		}
		if (backUpItem.getDirectoryGroup() != directoryGroup) {
			throw new IllegalArgumentException("backUpItem with permanance level " + Objects.toString(backUpItem.getPermanenceLevel()) + " added to BackUpItemGroup with permanance level " + directoryGroup.getPermanenceLevel());
		}
		
		sizeDifference = sizeDifference + backUpItem.getSizeDifference();
		backUpItemNumber++;
		backUpItems.add(backUpItem);
		if (backUpItem.isAboveFileSizeLimitThreshold()) {
			isAboveFileSizeLimitThreshold = true;
		}
		return this;
	}

	@Override
	public boolean execute(BackUpCounters backUpCounters) {
		
		boolean result = true;
		for (BackUpItem backUpItem : backUpItems) {
			result &= backUpItem.execute(backUpCounters);
		}
		return result;
	}

	@Override
	public boolean isAboveFileSizeLimitThreshold() {
		return isAboveFileSizeLimitThreshold;
	}
}
