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

import java.util.LinkedList;

import org.fl.backupFiles.directoryGroup.GroupPolicy;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroup;

public class BackUpItemList extends LinkedList<AbstractBackUpItem> {

	private static final long serialVersionUID = 1L;
	
	private BackUpItemList() {
		super();
	}
	
	public static BackUpItemList build() {
		return new BackUpItemList();
	}
	
	@Override
	public boolean add(AbstractBackUpItem item) {

		if (item instanceof BackUpItem backUpItem) {
			DirectoryGroup directoryGroup = backUpItem.getDirectoryGroup();
			GroupPolicy groupPolicy = directoryGroup.getGroupPolicy();
			return switch (groupPolicy) {
				   	case DO_NOT_GROUP -> super.add(backUpItem);
					case GROUP_SUB_ITEMS -> super.add(backUpItem);
					case GROUP_ALL -> {
						BackUpItemGroup backUpItemGroup = directoryGroup.addBackUpItem(backUpItem);
						if (backUpItemGroup != null) {
							// new BackUpItemGroup created, so not yet in the BackUpItemList
							super.add(backUpItemGroup);
						}
						yield true;
					}
			};
		} else {
			throw new IllegalArgumentException("Trying to call BackUpItemList.add with a BackUpItemGroup argument");
		}

	}
	
	public void removeItemsDone() {
		removeIf(i -> i.getBackupStatus().equals(BackupStatus.DONE));
	}

}
