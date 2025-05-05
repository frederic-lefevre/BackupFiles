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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.BackUpItemGroup;
import org.fl.backupFiles.directoryGroup.DirectoryPermanenceLevel;
import org.fl.backupFiles.directoryGroup.GroupPolicy;

public class DirectoryGroupSub extends DirectoryGroup {

	private final Map<Path, DirectoryGroup> subDirectoryGroupMap;
	
	protected DirectoryGroupSub(Path path, DirectoryPermanenceLevel permanenceLevel, GroupPolicy groupPolicy) {
		super(path, permanenceLevel, groupPolicy);
		if (groupPolicy != GroupPolicy.GROUP_SUB_ITEMS) {
			throw new IllegalArgumentException(DirectoryGroupSub.class.getName() + " must be called with a GROUP_SUB_ITEMS group policy. It was called with " + Objects.toString(groupPolicy));
		}
		subDirectoryGroupMap = new HashMap<>();
	}

	// Return the BackUpItemGroup if a new one has been created. Null otherwise
	@Override
	public BackUpItemGroup addBackUpItem(BackUpItem item) {
		// TODO Auto-generated method stub
		
		Path subDirectoryOfItem = getSubDirectoryPath(item);
		DirectoryGroup directoryGroupOfSubDir = subDirectoryGroupMap.get(subDirectoryOfItem);
		if (directoryGroupOfSubDir == null) {
			// no directory group yet. Create one to group all the BackupItems of this sub directory
			DirectoryGroup directoryGroupAll = DirectoryGroupBuilder.build(subDirectoryOfItem, getPermanenceLevel(), GroupPolicy.GROUP_ALL);
			subDirectoryGroupMap.put(subDirectoryOfItem, directoryGroupAll);
			return directoryGroupAll.addBackUpItem(item);

		} else {
			return directoryGroupOfSubDir.addBackUpItem(item);
		}
	}
	
	@Override
	public void clear() {
		subDirectoryGroupMap.clear();
	}
	
	private Path getSubDirectoryPath(BackUpItem item) {
		
		Path itemPath = item.getSourcePath();
		if (itemPath == null) {
			itemPath = item.getSourceClosestExistingPath();
		}
		
		if (itemPath.getNameCount() < getDirectoryGroupPathNameCount() + 2) {
			// the item path is directly under the DirectoryGroup path. It does not belong to a subpath
			// this case should not happen as it is checked before calling addBackUpItem to avoid creating a group
			return getPath();
		} else {
			return getPath().resolve(itemPath.getName(getDirectoryGroupPathNameCount()));
		}
	}
}
