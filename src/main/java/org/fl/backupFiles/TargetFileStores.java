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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.util.file.FilesUtils;


public class TargetFileStores {

	private static final Logger tLog = Logger.getLogger(TargetFileStores.class.getName());
	
	private final Map<FileStore, TargetFileStore> targetFileStores;
	
	public TargetFileStores() {
		targetFileStores = new HashMap<FileStore, TargetFileStore>();
	}
	
	public TargetFileStore addTargetFileStore(Path path, long sizeWarningThreshold) {
		
		if (path != null) {
			try {
				FileStore fileStore = FilesUtils.findFileStore(path, tLog);

				if (fileStore == null) {
					return null;
				} else if (! targetFileStores.containsKey(fileStore)) {
					Path mountPoint = FilesUtils.findMountPoint(path, tLog);
					TargetFileStore targetFileStore = new TargetFileStore(fileStore, mountPoint, sizeWarningThreshold);
					targetFileStores.put(fileStore, targetFileStore);
					return targetFileStore;
				} else {
					return targetFileStores.get(fileStore);
				}
			} catch (Exception e) {	
				tLog.log(Level.SEVERE, "Exception when getting filestore and mount point for " + path, e);
			}
		}
		return  null;
	}
	
	public TargetFileStore getTargetFileStore(FileStore fileStore) {
		return targetFileStores.get(fileStore);
	}
	
	public long getPotentialSizeChange(FileStore fileStore) {
		
		if (targetFileStores.containsKey(fileStore)) {
			return targetFileStores.get(fileStore).getPotentialSizeChange();
		} else {
			tLog.warning("Trying to record FileStore potential size change for an unrecorded FileStore: " + getFileStoreNameAndType(fileStore));
			return 0;
		}
	}
	
	public long recordPotentialSizeChange(FileStore fileStore, long sizeDifference) {
		
		if (targetFileStores.containsKey(fileStore)) {
			return targetFileStores.get(fileStore).recordPotentialSizeChange(sizeDifference);
		} else {
			tLog.warning("Trying to record FileStore potential size change for an unrecorded FileStore: " + getFileStoreNameAndType(fileStore));
			return 0;
		}
	}
	
	public long getTotalPotentialSizeChange() {
		return targetFileStores.values().stream()
				.mapToLong(targetFileStore -> targetFileStore.getPotentialSizeChange())
				.sum();
	}
	
	private String getFileStoreNameAndType(FileStore fileStore) {
		if (fileStore == null) {
			return "null FileStore";
		} else {
			return "name=" + fileStore.name() + " type=" + fileStore.type();
		}
	}
	
	public void mergeWith(TargetFileStores otherTargetFileStores) {
		
		otherTargetFileStores.getTargetFileStoresMap().forEach((fileStore, otherTargetFileStore) -> {
			TargetFileStore targetFileStore = targetFileStores.get(fileStore);
			if (targetFileStore == null) {
				targetFileStores.put(fileStore, otherTargetFileStore);
			} else {
				recordPotentialSizeChange(fileStore, otherTargetFileStore.getPotentialSizeChange());
			}
		});
	}
	
	public void reset() {
		targetFileStores.values().forEach(targetFileStore -> targetFileStore.reset());
	}
	
	private Map<FileStore, TargetFileStore> getTargetFileStoresMap() {
		return targetFileStores;
	}
	
	public String getTargetRemainigSpace(boolean inHtml) {
		
		StringBuilder spaceEvol = new StringBuilder() ;
		if (inHtml) {
			spaceEvol.append("<p>") ;
		}
		spaceEvol.append("Stockage de fichiers, espace restant utilisable:") ;
		if (inHtml) {
			spaceEvol.append("<ul>") ;
		} else {
			spaceEvol.append("\n") ;
		}
		for (TargetFileStore targetFileStore : targetFileStores.values()) {
			if (inHtml) {
				spaceEvol.append("<li>") ;
			} else {
				spaceEvol.append("- ") ;
			}
			targetFileStore.getSpaceEvolution(spaceEvol) ;
			if (inHtml) {
				spaceEvol.append("</li>") ;
			} else {
				spaceEvol.append("\n") ;
			}
		}
		return spaceEvol.toString() ;
	}
}
