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

package org.fl.backupFiles.directoryGroup;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.fl.backupFiles.directoryGroup.core.DirectoryGroup;
import org.fl.backupFiles.directoryGroup.core.DirectoryGroupBuilder;

public class DirectoryGroupMap {
	
	public static final DirectoryPermanenceLevel DEFAULT_PERMANENCE_LEVEL = DirectoryPermanenceLevel.HIGH;
	public static final GroupPolicy DEFAULT_GROUP_POLICY = GroupPolicy.DO_NOT_GROUP;

	private final Map<Path, DirectoryGroup> directoryGroupMap;
	private final Set<Path> pathKeys;
	private final DirectoryGroup defaultDirectoryGroup;

	public DirectoryGroupMap(Path originSourcePath, Path actualSourcePath, DirectoryGroupConfiguration directoryGroupConfiguration) {
		super();
		defaultDirectoryGroup = DirectoryGroupBuilder.build(Path.of("/"), DEFAULT_PERMANENCE_LEVEL, DEFAULT_GROUP_POLICY);
		directoryGroupMap = new TreeMap<Path, DirectoryGroup>(new DeeperPathComparator());

		directoryGroupConfiguration.getDirectoryGroupList().forEach(directoryGroup -> {
			Path sPath = directoryGroup.getPath();
			if (sPath.startsWith(originSourcePath)) {
				Path directoryGroupPathRelativeToActualSource = actualSourcePath.resolve(originSourcePath.relativize(sPath));
				directoryGroupMap.put(directoryGroupPathRelativeToActualSource, 
						DirectoryGroupBuilder.build(directoryGroupPathRelativeToActualSource, directoryGroup.getPermanenceLevel(), directoryGroup.getGroupPolicy()));
			} else if (originSourcePath.startsWith(sPath)) {
				directoryGroupMap.put(actualSourcePath, 
						DirectoryGroupBuilder.build(actualSourcePath, directoryGroup.getPermanenceLevel(), directoryGroup.getGroupPolicy()));
			}
			
		});
		pathKeys = directoryGroupMap.keySet();
	}

	public DirectoryGroup getDirectoryGroup(Path dir) {

		for (Path pathKey : pathKeys) {
			if (dir.startsWith(pathKey)) {
				return directoryGroupMap.get(pathKey);
			}
		}
		return defaultDirectoryGroup;
	}
	
	public void clearBackUpItemsInDirectoryGroup() {
		directoryGroupMap.values().forEach(DirectoryGroup::clear);
	}
}
