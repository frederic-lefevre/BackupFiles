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

import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.BackUpItemGroup;
import org.fl.backupFiles.directoryGroup.DirectoryPermanenceLevel;
import org.fl.backupFiles.directoryGroup.GroupPolicy;

public class DirectoryGroup {

	private final Path path;
	private final DirectoryPermanenceLevel permanenceLevel;
	private final GroupPolicy groupPolicy;
	
	protected DirectoryGroup(Path path, DirectoryPermanenceLevel permanenceLevel, GroupPolicy groupPolicy) {
		this.path = path;
		this.permanenceLevel = permanenceLevel;
		this.groupPolicy = groupPolicy;
	}

	// Implemented by subclasses that memorize BackUpItem
	public BackUpItemGroup addBackUpItem(BackUpItem item) {
		if (item == null) {
			throw new IllegalStateException("DirectoryGroup.addBackUpItem should not be called. Trying to add a null BackUpItem");
		} else {
			throw new IllegalStateException("DirectoryGroup.addBackUpItem should not be called. Trying to add a BackUpItem");
		}
	}
	
	public Path getPath() {
		return path;
	}

	public DirectoryPermanenceLevel getPermanenceLevel() {
		return permanenceLevel;
	}

	public GroupPolicy getGroupPolicy() {
		return groupPolicy;
	}
	
	public void clear() {
		// Nothing to clear
	}
}
