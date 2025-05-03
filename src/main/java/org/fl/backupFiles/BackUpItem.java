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

import java.io.File;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.directoryGroup.DirectoryPermanenceLevel;
import org.fl.backupFiles.scanner.PathPairBasicAttributes;
import org.fl.util.file.FileComparator;
import org.fl.util.file.FilesSecurityUtils;
import org.fl.util.file.FilesUtils;

public class BackUpItem extends AbstractBackUpItem {

	private static final Logger bLog = Logger.getLogger(BackUpItem.class.getName());
	
	private static final String SRC_NOT_EXISTS = "Source path parameter is null or the path does not exist";
	private static final String TGT_NOT_EXISTS = "Target path parameter is null or the path does not exist";
	private static final String TGT_SHOULD_NOT_EXISTS = "Target path parameter should not exist";
	private static final String EXIST_SRC_NOT_EXISTS = "Closest existing source path parameter is null or the path does not exist";

	private final PathPairBasicAttributes pathPairBasicAttributes;
	private final Path sourceClosestExistingPath;
	
	// A back up item is :
	// * a source path (file or directory) to back up 
	// * a destination path (file or directory) to back up
	// * the closest existing source path (it is used to get the ACL of source path and try to set the target path writable, if delete or copy fails
	// * a back up action that may take the following values:
	//		COPY_NEW 	 : if  the target file does not exists
	//		COPY_REPLACE : if the source file is newer than the target 
	//		COPY_TREE 	 : if the target directory does not exists
	//		DELETE       : if the source file does not exists (and the target exists)
	//		DELETE_DIR   : if the source directory does not exists (and the target exists)
	//		AMBIGUOUS    : Abnormal case - if the target is newer than the source
	// * a back up status that may take the following values:
	//		DIFFERENT	 	: the source and target are different by attributes (size, dates...)
	//		DIFF_BY_CONTENT : the source and target are different by content (this information is only available when a content comparison is done)
	//		SAME_CONTENT 	: the source and target are different by attributes but have same content (and target is newer)
	//		DONE	 	 	: the item has been backed-up
	//		FAILED	 	 	: the back up has failed
	
	public BackUpItem(PathPairBasicAttributes pathPairBasicAttributes, 
			BackupAction backUpAction, 
			BackupStatus backUpStatus, 
			BackUpCounters backUpCounters,
			BackUpTask backUpTask) {
		
		super(pathPairBasicAttributes.getSourcePath(), pathPairBasicAttributes.getTargetPath(), backUpAction, backUpStatus, backUpTask);
		backUpItemNumber = 1;
		this.pathPairBasicAttributes = pathPairBasicAttributes;
		sourceClosestExistingPath = sourcePath;
		checkPathExistenceCondition(pathPairBasicAttributes.sourceExists(), sourcePath, SRC_NOT_EXISTS);
		
		// Update counters		
		if (backupAction.equals(BackupAction.COPY_REPLACE)) {
			checkPathExistenceCondition(pathPairBasicAttributes.targetExists(), targetPath, TGT_NOT_EXISTS);
			sizeDifference = pathPairBasicAttributes.getSourceSize() - pathPairBasicAttributes.getTargetSize();
			backUpCounters.copyReplaceNb++;
		} else if (backupAction.equals(BackupAction.COPY_NEW)) {
			checkPathExistenceCondition(!pathPairBasicAttributes.targetExists(), targetPath, TGT_SHOULD_NOT_EXISTS);
			sizeDifference = pathPairBasicAttributes.getSourceSize();
			backUpCounters.copyNewNb++;
		} else if (backupAction.equals(BackupAction.COPY_TREE)) {
			checkPathExistenceCondition(!pathPairBasicAttributes.targetExists(), targetPath, TGT_SHOULD_NOT_EXISTS);
			sizeDifference = FilesUtils.folderSize(sourcePath, bLog);
			backUpCounters.copyTreeNb++;
		} else if (backupAction.equals(BackupAction.AMBIGUOUS)) {
			checkPathExistenceCondition(pathPairBasicAttributes.targetExists(), targetPath, TGT_NOT_EXISTS);
			sizeDifference = pathPairBasicAttributes.getSourceSize() - pathPairBasicAttributes.getTargetSize();
			backUpCounters.ambiguousNb++;
		} else if (backupAction.equals(BackupAction.COPY_TARGET)) {
			checkPathExistenceCondition(pathPairBasicAttributes.targetExists(), targetPath, TGT_NOT_EXISTS);
			sizeDifference = pathPairBasicAttributes.getSourceSize() - pathPairBasicAttributes.getTargetSize();
			backUpCounters.copyTargetNb++;
		} else if (backupAction.equals(BackupAction.ADJUST_TIME)) {
			checkPathExistenceCondition(pathPairBasicAttributes.targetExists(), targetPath, TGT_NOT_EXISTS);
			sizeDifference = 0;
			backUpCounters.adjustTimeNb++;
		} else {
			throw new IllegalBackupActionException("Illegal backup action", backupAction);
		}
		updateLimitsCounters(backUpCounters);
	}

	// For delete actions
	public BackUpItem(PathPairBasicAttributes pathPairBasicAttributes, 
			BackupAction backUpAction, 
			PathPairBasicAttributes parentPathPairBasicAttributes, 
			BackUpCounters backUpCounters,
			BackUpTask backUpTask) {
		
		super(pathPairBasicAttributes.getSourcePath(), pathPairBasicAttributes.getTargetPath(), backUpAction, BackupStatus.DIFFERENT, backUpTask);
		backUpItemNumber = 1;
		this.pathPairBasicAttributes = pathPairBasicAttributes;
		this.sourceClosestExistingPath = parentPathPairBasicAttributes.getSourcePath();
		checkPathExistenceCondition(parentPathPairBasicAttributes.sourceExists(), sourceClosestExistingPath, EXIST_SRC_NOT_EXISTS);
		checkPathExistenceCondition(pathPairBasicAttributes.targetExists(), targetPath, TGT_NOT_EXISTS);
		
		// Update counters
		if (backupAction.equals(BackupAction.DELETE)) {
			sizeDifference = 0 - pathPairBasicAttributes.getTargetSize();
			backUpCounters.deleteNb++;
		} else if (backupAction.equals(BackupAction.DELETE_DIR)) {
			sizeDifference = 0 - FilesUtils.folderSize(pathPairBasicAttributes.getTargetPath(), bLog);
			backUpCounters.deleteDirNb++;
		} else {
			throw new IllegalBackupActionException("Illegal backup action (should be a delete action)", backupAction);
		}
		updateLimitsCounters(backUpCounters);
	}
	
	private void checkPathExistenceCondition(boolean condition, Path path, String exceptionMessage) {
		if (! condition) {
			throw new IllegalBackUpItemException(exceptionMessage, path);
		}
	}

	private void updateLimitsCounters(BackUpCounters backUpCounters) {
		backUpCounters.recordPotentialSizeChange(targetFileStore, sizeDifference);
		if (sizeDifference > fileSizeWarningThreshold)
			backUpCounters.backupWithSizeAboveThreshold++;
		if (directoryGroup.getPermanenceLevel().equals(DirectoryPermanenceLevel.HIGH)) {
			backUpCounters.nbHighPermanencePath++;
		} else if (directoryGroup.getPermanenceLevel().equals(DirectoryPermanenceLevel.MEDIUM)) {
			backUpCounters.nbMediumPermanencePath++;
		}
	}
	
	public PathPairBasicAttributes getPathPairBasicAttributes() {
		return pathPairBasicAttributes;
	}
	
	public File getSourceFile() {
		if (sourcePath != null) {
			return sourcePath.toFile();
		} else {
			return null;
		}
	}

	public File getTargetFile() {
		if (targetPath != null) {
			return targetPath.toFile();
		} else {
			return null;
		}
	}

	public boolean isSourcePresent() {
		return pathPairBasicAttributes.sourceExists();
	}

	public boolean isTargetPresent() {
		return pathPairBasicAttributes.targetExists();
	}

	@Override
	public boolean execute(BackUpCounters backUpCounters) {
		
		try {
			if (executeAction(backUpCounters)) {
				backupStatus = BackupStatus.DONE;
				updateLimitsCounters(backUpCounters);
			} else {
				backupStatus = BackupStatus.FAILED;
			}
		} catch (AccessDeniedException e) {
			// try to set the file writable
			bLog.log(Level.FINE, e, 
					() -> "AccessDeniedException on sourcePath=" + Objects.toString(sourcePath) + " targetPath=" + Objects.toString(targetPath)+ " action=" + backupAction);
			try {
				FilesSecurityUtils.setWritable(targetPath, sourceClosestExistingPath);
				if (executeAction(backUpCounters)) {
					backupStatus = BackupStatus.DONE;
				} else {
					backupStatus = BackupStatus.FAILED;
				}
			} catch (Exception e1) {
				bLog.log(Level.SEVERE,
						"Exception trying to set file writable and execute action : " + targetPath + " " + backupAction,
						e1);
				backupStatus = BackupStatus.FAILED;
				if ((backupAction.equals(BackupAction.DELETE)) || (backupAction.equals(BackupAction.DELETE_DIR))) {
					backUpCounters.nbTargetFilesFailed++;
				} else {
					backUpCounters.nbSourceFilesFailed++;
				}
			}

		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception executing action : " + targetPath + " " + backupAction, e);
			backupStatus = BackupStatus.FAILED;
			if ((backupAction.equals(BackupAction.DELETE)) || (backupAction.equals(BackupAction.DELETE_DIR))) {
				backUpCounters.nbTargetFilesFailed++;
			} else {
				backUpCounters.nbSourceFilesFailed++;
			}
		}
		return backupStatus == BackupStatus.DONE;
	}
	
	private boolean executeAction(BackUpCounters backUpCounters) throws Exception {
		
		boolean success = true;
		if (backupAction.equals(BackupAction.COPY_REPLACE)) {
			Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES,
					LinkOption.NOFOLLOW_LINKS);
			backUpCounters.copyReplaceNb++;
			backUpCounters.nbSourceFilesProcessed++;
		} else if (backupAction.equals(BackupAction.COPY_NEW)) {
			Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES,
					LinkOption.NOFOLLOW_LINKS);
			backUpCounters.copyNewNb++;
			backUpCounters.nbSourceFilesProcessed++;
		} else if (backupAction.equals(BackupAction.DELETE)) {
			Files.delete(targetPath);
			backUpCounters.deleteNb++;
			backUpCounters.nbTargetFilesProcessed++;
		} else if (backupAction.equals(BackupAction.COPY_TREE)) {
			success = FilesUtils.copyDirectoryTree(sourcePath, targetPath, bLog);
			if (success) {
				backUpCounters.copyTreeNb++;
				backUpCounters.nbSourceFilesProcessed++;
			} else {
				backUpCounters.nbSourceFilesFailed++;
			}
		} else if (backupAction.equals(BackupAction.DELETE_DIR)) {
			success = FilesUtils.deleteDirectoryTree(targetPath, true, bLog);
			if (success) {
				backUpCounters.deleteDirNb++;
				backUpCounters.nbTargetFilesProcessed++;
			} else {
				backUpCounters.nbTargetFilesFailed++;
			}
		} else if (backupAction.equals(BackupAction.AMBIGUOUS)) {
			Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES,
					LinkOption.NOFOLLOW_LINKS);
			backUpCounters.ambiguousNb++;
			backUpCounters.nbSourceFilesProcessed++;
		} else if (backupAction.equals(BackupAction.COPY_TARGET)) {
			Files.copy(targetPath, sourcePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES,
					LinkOption.NOFOLLOW_LINKS);
			backUpCounters.copyTargetNb++;
			backUpCounters.nbSourceFilesProcessed++;
		} else if (backupAction.equals(BackupAction.ADJUST_TIME)) {
			FileTime sourceLastModifiedTime = pathPairBasicAttributes.getSourceBasicAttributes().lastModifiedTime();
			Files.setLastModifiedTime(targetPath, sourceLastModifiedTime);
			
			if (Files.getLastModifiedTime(targetPath).compareTo(sourceLastModifiedTime) == 0) {
				// Last modified time of target has been successfully set to the source one
				backUpCounters.adjustTimeNb++;
			} else {
				// Fail to modify last modified time of target
				// It is not possible on some external drive on windows
				// Then copy target to source is the only solution
				bLog.info(() -> "Fail to adjust time for " + targetPath.getFileName());
				Files.copy(targetPath, sourcePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES,
						LinkOption.NOFOLLOW_LINKS);
				backUpCounters.copyTargetNb++;
			}
			backUpCounters.nbSourceFilesProcessed++;			
		} else {
			throw new IllegalBackupActionException("Invalid backup action: ", backupAction);
		}
		return success;
	}
	
	public void getInformation(StringBuilder infos) {

		try {

			infos.append("Source file : ");
			BasicFileAttributes sourceBasicAttributes = FilesUtils.appendFileInformations(sourcePath, infos, bLog);

			infos.append("\nTarget file : ");
			BasicFileAttributes targetBasicAttributes = FilesUtils.appendFileInformations(targetPath, infos, bLog);

			if ((sourceBasicAttributes != null) && (targetBasicAttributes != null)
					&& (sourceBasicAttributes.isRegularFile()) && (targetBasicAttributes.isRegularFile())) {
				// compare files

				FileComparator fileCompare = new FileComparator(bLog);
				if (fileCompare.haveSameContent(sourcePath, targetPath)) {
					infos.append("\nThe contents of source and target files are the same\n");
				} else if (fileCompare.isOnError()) {
					infos.append("\nThe files comparaison raised an error\n");
				} else {
					infos.append("\nThe contents of source and target files are different\n");
				}
			}

		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception when getting information on backup item.\nSource Path=" + sourcePath
					+ "\nTargetpath=" + targetPath, e);
		}
	}

	@Override
	public boolean isAboveFileSizeLimitThreshold() {
		return sizeDifference > fileSizeWarningThreshold;
	}

}
