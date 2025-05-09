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

package org.fl.backupFiles.directoryGroup.core;

import java.nio.file.Path;
import java.util.Objects;

import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.BackUpItemGroup;
import org.fl.backupFiles.BackUpTask;
import org.fl.backupFiles.BackupAction;
import org.fl.backupFiles.BackupStatus;
import org.fl.backupFiles.directoryGroup.DirectoryPermanenceLevel;
import org.fl.backupFiles.directoryGroup.GroupPolicy;

public class DirectoryGroupAll extends DirectoryGroup {

	private BackUpItemGroup[][] backUpItemGroupLists;
	
	protected DirectoryGroupAll(Path path, DirectoryPermanenceLevel permanenceLevel, GroupPolicy groupPolicy) {
		super(path, permanenceLevel, groupPolicy);
		if (groupPolicy != GroupPolicy.GROUP_ALL) {
			throw new IllegalArgumentException(DirectoryGroupAll.class.getName() + " must be called with a GROUP_ALL group policy. It was called with " + Objects.toString(groupPolicy));
		}
		backUpItemGroupLists = new BackUpItemGroup[BackupAction.values().length][BackupStatus.values().length];
	}

	// Return the BackUpItemGroup if a new one has been created. Null otherwise
	@Override
	public BackUpItemGroup addBackUpItem(BackUpItem item) {
		
		BackUpItemGroup backupItemGroup = backUpItemGroupLists[item.getBackupAction().ordinal()][item.getBackupStatus().ordinal()];
		if (backupItemGroup == null) {
			BackUpTask backUpTask = item.getBackUpTask();
			Path targetPathForGroup = backUpTask.getTarget().resolve(backUpTask.getSource().relativize(getPath()));
			backupItemGroup = new BackUpItemGroup(getPath(),targetPathForGroup, getPath(), item.getBackupAction(), item.getBackupStatus(), backUpTask);
			backUpItemGroupLists[item.getBackupAction().ordinal()][item.getBackupStatus().ordinal()] = backupItemGroup;
			backupItemGroup.addBackUpItem(item);
			return backupItemGroup;
		} else {
			backupItemGroup.addBackUpItem(item);
			return null;
		}
	}
	
	@Override
	public void clear() {
		backUpItemGroupLists = new BackUpItemGroup[BackupAction.values().length][BackupStatus.values().length];
	}
}
