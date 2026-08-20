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

import java.io.IOException;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.directoryGroup.DirectoryGroupMap;
import org.fl.util.file.FilesUtils;

public class BackUpTask {
	
	private static final Logger logger = Logger.getLogger(BackUpTask.class.getName());
	
	private final Path source;
	private final Path target;
	private final String name;
	private final long sizeWarningLimit;
	private FileStore targetFileStore;
	private final DirectoryGroupMap directoryGroupMap;
	
	private boolean compareContent;
	private boolean compareContentOnAmbiguous;
	
	public enum TaskStatus {
		
		UNEXISTANT_ORIGIN_AND_TARGET("  Attention : les chemins origine et destination n'existent pas"),
		UNEXISTANT_ORIGIN("  Attention : le chemin origine n'existe pas"),
		UNEXISTANT_TARGET("  Attention : le chemin destination n'existe pas"),
		NORMAL("");
		
		String statusWarning;
		
		private TaskStatus(String statusWarning) {
			this.statusWarning = statusWarning;
		}
		
		public String getStatusWarning() {
			return statusWarning;
		}
	}
	
	// A back up task is a source directory or file to back up to a destination directory or file
	public BackUpTask(Path src, Path tgt, DirectoryGroupMap directoryGroupMap, long sizeWarningLimit) {
		
		source = src;
		target = tgt;
		this.sizeWarningLimit = sizeWarningLimit;
		
		if ((source == null) || (target == null)) {
			throw new IllegalArgumentException("Null path argument when creating back up task. sourcePath=" + Objects.toString(source) + " targetPath="  + Objects.toString(source));
		}

		name = source.toString() + " ==> " + target.toString();

		this.directoryGroupMap = directoryGroupMap;

		compareContent = false;
		compareContentOnAmbiguous = true;
	}

	public Path getSource() {
		return source;
	}

	public Path getTarget() {
		return target;
	}

	public long getSizeWarningLimit() {
		return sizeWarningLimit;
	}

	public DirectoryGroupMap getDirectoryGroupMap() {
		return directoryGroupMap;
	}

	public FileStore getTargetFileStore() {
		if (targetFileStore == null) {
			try {
				targetFileStore = FilesUtils.findFileStore(target);
			} catch (IOException e) {
				targetFileStore = null;
				logger.log(Level.SEVERE, "IOException getting FileStore for " + Objects.toString(target), e);
			}
		}
		return targetFileStore;
	}

	public String name() {
		return name;
	}

	public TaskStatus getTaskStatus() {
		
		boolean sourceExists = Files.exists(source);
		boolean targetExists = Files.exists(target);
		if (!sourceExists && !targetExists) {
			return TaskStatus.UNEXISTANT_ORIGIN_AND_TARGET;
		} else if (!sourceExists) {
			return TaskStatus.UNEXISTANT_ORIGIN;
		} else if (!targetExists) {
			return TaskStatus.UNEXISTANT_TARGET;
		} else {
			return TaskStatus.NORMAL;
		}
	}

	public boolean compareContent() {
		return compareContent;
	}

	public void setCompareContent(boolean compareContent) {
		this.compareContent = compareContent;
	}

	public boolean compareContentOnAmbiguous() {
		return compareContentOnAmbiguous;
	}

	public void setCompareContentOnAmbiguous(boolean compareContentOnAmbiguous) {
		this.compareContentOnAmbiguous = compareContentOnAmbiguous;
	}
	
	// Hashcode with lazy init
	private int hashcode = 0;

	@Override
	public int hashCode() {

		int result = hashcode;

		if (result == 0) {
			final int prime = 31;
			result = 1;
			result = prime * result + ((source == null) ? 0 : source.hashCode());
			result = prime * result + ((target == null) ? 0 : target.hashCode());
		}
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (obj instanceof BackUpTask other) {
			if (source == null) {
				if (other.source != null)
					return false;
			} else if (!source.equals(other.source)) {
				return false;
			}
			if (target == null) {
				if (other.target != null)
					return false;
			} else if (!target.equals(other.target)) {
				return false;
			}
		} else {
			return false;
		}
		return true;
	}
}
