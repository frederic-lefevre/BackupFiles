/*
 * MIT License

Copyright (c) 2017, 2026 Frederic Lefevre

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

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.fl.backupFiles.directoryGroup.GroupPolicy;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroup;

public class BackUpItemList {
	
	private final LinkedList<AbstractBackUpItem> backUpItemList;
	
	private BackUpItemList() {
		backUpItemList = new LinkedList<AbstractBackUpItem>();
	}
	
	public static BackUpItemList build() {
		return new BackUpItemList();
	}
	
	public boolean add(AbstractBackUpItem item) {

		if (item instanceof BackUpItem backUpItem) {
			DirectoryGroup directoryGroup = backUpItem.getDirectoryGroup();
			GroupPolicy groupPolicy = directoryGroup.getGroupPolicy();
			return switch (groupPolicy) {
				   	case DO_NOT_GROUP -> backUpItemList.add(backUpItem);
					case GROUP_SUB_ITEMS -> {
						if (getBackUpItemSourceClosestExistingPathLength(backUpItem) < directoryGroup.getDirectoryGroupPathNameCount() + 2) {
							// the item path (reported to DirectoryGroup) is directly under the DirectoryGroup path. It does not belong to a subpath
							backUpItemList.add(backUpItem);
						} else {
							BackUpItemGroup backUpItemGroup = directoryGroup.addBackUpItem(backUpItem);
							if (backUpItemGroup != null) {
								// new BackUpItemGroup created, so not yet in the BackUpItemList
								backUpItemList.add(backUpItemGroup);
							}
						}					
						yield true;
					}
					case GROUP_ALL -> {
						BackUpItemGroup backUpItemGroup = directoryGroup.addBackUpItem(backUpItem);
						if (backUpItemGroup != null) {
							// new BackUpItemGroup created, so not yet in the BackUpItemList
							backUpItemList.add(backUpItemGroup);
						}
						yield true;
					}
			};
		} else {
			throw new IllegalArgumentException("Trying to call BackUpItemList.add with a BackUpItemGroup argument");
		}
	}
	
	public void removeItemsDone() {
		backUpItemList.removeIf(i -> i.getBackupStatus().equals(BackupStatus.DONE));
	}
	
	private int getBackUpItemSourceClosestExistingPathLength(BackUpItem item) {
		return item.getSourceClosestExistingPath().getNameCount();
	}
	
	public int size() {
		return backUpItemList.size();
	}
	
	public void clear() {
		backUpItemList.clear();
	}
	
	public <T extends AbstractBackUpItem> void addAll(Collection<T> items) {
		backUpItemList.addAll(items);
	}
	
	public void addAll(BackUpItemList backUpItemList2) {
		backUpItemList.addAll(backUpItemList2.backUpItemList);
	}
	
	public List<AbstractBackUpItem> getBackUpItems() {
		return backUpItemList;
	}
	
	public BackUpCounters sumIndividualCounters() {
		BackUpCounters backUpCounters = new BackUpCounters(new TargetFileStores(), OperationType.SCAN);
		backUpItemList.forEach(backUpItem -> backUpItem.sumIndividualCounters(backUpCounters));
		return backUpCounters;
	}
}
