/*
 * MIT License

Copyright (c) 2017, 2023 Frederic Lefevre

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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.directoryPermanence.DirectoryPermanence;
import org.fl.backupFiles.directoryPermanence.DirectoryPermanenceLevel;
import org.fl.util.file.FileComparator;
import org.fl.util.file.FilesSecurityUtils;
import org.fl.util.file.FilesUtils;

public class BackUpItem {

	private static final Logger bLog = Config.getLogger();
	
	private static final String SRC_NOT_EXISTS 		  = "Source path parameter is null or the path does not exist";
	private static final String TGT_NOT_EXISTS 		  = "Target path parameter is null or the path does not exist";
	private static final String TGT_SHOULD_NOT_EXISTS = "Target path parameter should not exist";
	private static final String EXIST_SRC_NOT_EXISTS  = "Existing source path parameter is null or the path does not exist";

	
	private static long fileSizeWarningThreshold ;
	public static void setFileSizeWarningThreshold(long fileSizeWarningThreshold) {
		BackUpItem.fileSizeWarningThreshold = fileSizeWarningThreshold;
	}
	
	public enum BackupAction { COPY_NEW, COPY_REPLACE, COPY_TREE, DELETE, DELETE_DIR, AMBIGUOUS, COPY_TARGET };
	
	public enum BackupStatus { DIFFERENT, DIFF_BY_CONTENT, SAME_CONTENT, DONE, FAILED } ;

	private final Path	 				   sourcePath ;
	private final Path 		 			   sourceClosestExistingPath ;
	private final Path 		 			   targetPath ;
	private final BackupAction 			   backupAction ;
	private final long					   sizeDifference ;
	private BackupStatus 				   backupStatus ;
	private final DirectoryPermanenceLevel permanenceLevel ;
	private final boolean				   sourcePresent;
	private final boolean				   targetPresent;
	
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
	
	public BackUpItem(Path src, Path tgt, BackupAction bst, BackupStatus bStatus, long sd, BackUpCounters backUpCounters) {
		
		sourcePath 	 	 		  = src ;
		sourceClosestExistingPath = src ;
		targetPath 	 	 		  = tgt ;
		backupAction 	 		  = bst ;
		backupStatus 	 		  = bStatus;
		sizeDifference			  = sd ;
		if (targetPath != null) {
			permanenceLevel		  = Config.getDirectoryPermanence().getPermanenceLevel(targetPath) ;
		} else if (src != null) {
			permanenceLevel		  = Config.getDirectoryPermanence().getPermanenceLevel(src) ;
		} else {
			permanenceLevel		  = DirectoryPermanence.DEFAULT_PERMANENCE_LEVEL ;
		}
		
		// Update counters
		checkPathExists(src, SRC_NOT_EXISTS);
		sourcePresent = true;
		if (backupAction.equals(BackupAction.COPY_REPLACE)) {
			checkPathExists(tgt, TGT_NOT_EXISTS);
			targetPresent = true;
			backUpCounters.copyReplaceNb++ ;
		} else if (backupAction.equals(BackupAction.COPY_NEW)) {
			checkPathDoesNotExist(tgt, TGT_SHOULD_NOT_EXISTS);
			targetPresent = false;
			backUpCounters.copyNewNb++ ;
		} else if (backupAction.equals(BackupAction.COPY_TREE)) {
			checkPathDoesNotExist(tgt, TGT_SHOULD_NOT_EXISTS);
			targetPresent = false;
			backUpCounters.copyTreeNb++ ;
		} else if (backupAction.equals(BackupAction.AMBIGUOUS)) {
			checkPathExists(tgt, TGT_NOT_EXISTS);
			targetPresent = true;
			backUpCounters.ambiguousNb++ ;
		} else if (backupAction.equals(BackupAction.COPY_TARGET)) {
			checkPathExists(tgt, TGT_NOT_EXISTS);
			targetPresent = true;
			backUpCounters.copyTargetNb++ ;
		} else {
			throw new IllegalBackupActionException("Illegal backup action", backupAction) ;
		}
		updateLimtsCounters(backUpCounters) ;
	}

	// For delete actions
	public BackUpItem(Path tgt, BackupAction bst, Path srcExisting, long sd, BackUpCounters backUpCounters) {
		sourcePath 	 	 		  = null ;
		sourceClosestExistingPath = srcExisting ;
		targetPath 	 	 		  = tgt ;
		backupAction 	 		  = bst ;
		backupStatus 	 		  = BackupStatus.DIFFERENT ;
		sizeDifference			  = sd ;
		if (targetPath != null) {
			permanenceLevel		  = Config.getDirectoryPermanence().getPermanenceLevel(targetPath) ;
		} else if (srcExisting != null) {
			permanenceLevel		  = Config.getDirectoryPermanence().getPermanenceLevel(srcExisting) ;
		} else {
			permanenceLevel		  = DirectoryPermanence.DEFAULT_PERMANENCE_LEVEL ;
		}
		
		checkPathExists(srcExisting, EXIST_SRC_NOT_EXISTS);
		checkPathExists(tgt, TGT_NOT_EXISTS);
		sourcePresent = false;
		targetPresent = true;
		
		// Update counters
		if (backupAction.equals(BackupAction.DELETE)) {
			backUpCounters.deleteNb++ ;
		} else if (backupAction.equals(BackupAction.DELETE_DIR)) {
			backUpCounters.deleteDirNb++ ;
		} else {
			throw new IllegalBackupActionException("Illegal backup action (should be a delete action)", backupAction) ;
		}
		updateLimtsCounters(backUpCounters) ;
	}
	
	private void checkPathExists(Path path, String exceptionMessage) {
		if ((path == null) || (Files.notExists(path))) {
			throw new IllegalBackUpItemException(exceptionMessage, path) ;
		}
	}
	
	private void checkPathDoesNotExist(Path path, String exceptionMessage) {
		if ((path != null) && (Files.exists(path))) {
			throw new IllegalBackUpItemException(exceptionMessage, path) ;
		}
	}
	
	private void updateLimtsCounters(BackUpCounters backUpCounters) {
		backUpCounters.totalSizeDifference = backUpCounters.totalSizeDifference + sizeDifference ;
		if (sizeDifference > fileSizeWarningThreshold) backUpCounters.backupWithSizeAboveThreshold++ ;
		if (permanenceLevel.equals(DirectoryPermanenceLevel.HIGH)) {
			backUpCounters.nbHighPermanencePath++ ;
		} else if (permanenceLevel.equals(DirectoryPermanenceLevel.MEDIUM)) {
			backUpCounters.nbMediumPermanencePath++ ;
		}
	}
	
	public Path getSourcePath() {
		return sourcePath;
	}

	public Path getTargetPath() {
		return targetPath;
	}

	public File getSourceFile() {
		if (sourcePath != null) {
			return sourcePath.toFile();
		} else {
			return null ;
		}
	}

	public File getTargetFile() {
		if (targetPath != null) {
			return targetPath.toFile();
		} else {
			return null ;
		}
	}
	
	public BackupAction getBackupAction() {
		return backupAction;
	}

	public BackupStatus getBackupStatus() {
		return backupStatus;
	}

	public DirectoryPermanenceLevel getPermanenceLevel() {
		return permanenceLevel;
	}

	public long getSizeDifference() {
		return sizeDifference;
	}

	public boolean isSourcePresent() {
		return sourcePresent;
	}

	public boolean isTargetPresent() {
		return targetPresent;
	}

	public boolean execute(BackUpCounters backUpCounters) {
		
		try {
			if (executeAction(backUpCounters)) {
				backupStatus = BackupStatus.DONE ;
				updateLimtsCounters(backUpCounters) ;
			} else {
				backupStatus = BackupStatus.FAILED ;
			}
		} catch (AccessDeniedException e) {
			// try to set the file writable
			try {
				FilesSecurityUtils.setWritable(targetPath, sourceClosestExistingPath) ;
				if (executeAction(backUpCounters)) {
					backupStatus = BackupStatus.DONE ;
				} else {
					backupStatus = BackupStatus.FAILED ;
				}
			} catch (Exception e1) {
				bLog.log(Level.SEVERE, "Exception trying to set file writable and execute action : " + targetPath + " " + backupAction, e1) ;
				backupStatus = BackupStatus.FAILED ;
				if ((backupAction.equals(BackupAction.DELETE)) || 
						(backupAction.equals(BackupAction.DELETE_DIR)) ) {
					backUpCounters.nbTargetFilesFailed++ ;
				} else {
					backUpCounters.nbSourceFilesFailed++ ;
				}
			}

		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception executing action : " + targetPath + " " + backupAction, e) ;
			backupStatus = BackupStatus.FAILED ;
			if ((backupAction.equals(BackupAction.DELETE)) || 
				(backupAction.equals(BackupAction.DELETE_DIR)) ) {
				backUpCounters.nbTargetFilesFailed++ ;
			} else {
				backUpCounters.nbSourceFilesFailed++ ;
			}
		}
		return backupStatus == BackupStatus.DONE;
	}
	
	private boolean executeAction(BackUpCounters backUpCounters) throws Exception {
		
		boolean success = true ;
		if (backupAction.equals(BackupAction.COPY_REPLACE)) {
			Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES, LinkOption.NOFOLLOW_LINKS) ;
			backUpCounters.copyReplaceNb++ ;
			backUpCounters.nbSourceFilesProcessed++ ;
		} else if (backupAction.equals(BackupAction.COPY_NEW)) {
			Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES, LinkOption.NOFOLLOW_LINKS) ;
			backUpCounters.copyNewNb++ ;
			backUpCounters.nbSourceFilesProcessed++ ;
		} else if (backupAction.equals(BackupAction.DELETE)) {
			Files.delete(targetPath);
			backUpCounters.deleteNb++ ;
			backUpCounters.nbTargetFilesProcessed++ ;
		} else if (backupAction.equals(BackupAction.COPY_TREE)) {
			success = FilesUtils.copyDirectoryTree(sourcePath, targetPath, bLog) ;
			if (success) {
				backUpCounters.copyTreeNb++ ;
				backUpCounters.nbSourceFilesProcessed++ ;
			} else {
				backUpCounters.nbSourceFilesFailed++ ;
			}
		} else if (backupAction.equals(BackupAction.DELETE_DIR)) {				
			success = FilesUtils.deleteDirectoryTree(targetPath, true, bLog) ;
			if (success) {
				backUpCounters.deleteDirNb++ ;
				backUpCounters.nbTargetFilesProcessed++ ;
			} else {
				backUpCounters.nbTargetFilesFailed++ ;
			}
		} else if (backupAction.equals(BackupAction.AMBIGUOUS)) {
			Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES, LinkOption.NOFOLLOW_LINKS) ;
			backUpCounters.ambiguousNb++ ;
			backUpCounters.nbSourceFilesProcessed++ ;
		} else if (backupAction.equals(BackupAction.COPY_TARGET)) {
			Files.copy(targetPath, sourcePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.COPY_ATTRIBUTES, LinkOption.NOFOLLOW_LINKS);
			backUpCounters.copyTargetNb++;
			backUpCounters.nbSourceFilesProcessed++;
		} else {
			throw new IllegalBackupActionException("Invalid backup action: ", backupAction);
		}
		return success ;
	}
	
	public void getInformation(StringBuilder infos) {
	
		try {
			
			infos.append("Source file : ") ;
			BasicFileAttributes sourceBasicAttributes = FilesUtils.appendFileInformations(sourcePath, infos, bLog) ;			
			
			infos.append("\nTarget file : ") ;
			BasicFileAttributes targetBasicAttributes = FilesUtils.appendFileInformations(targetPath, infos, bLog) ;
			
			if ((sourceBasicAttributes != null) && 
				(targetBasicAttributes != null) && 
				(sourceBasicAttributes.isRegularFile()) && 
				(targetBasicAttributes.isRegularFile())) {
				// compare files
				
				 FileComparator fileCompare = new FileComparator(bLog) ;
				 if (fileCompare.haveSameContent(sourcePath, targetPath)) {
					 infos.append("\nThe contents of source and target files are the same\n") ;
				 } else if (fileCompare.isOnError()) {
					 infos.append("\nThe files comparaison raised an error\n") ;					 
				 } else {
					 infos.append("\nThe contents of source and target files are different\n") ;
				 }
			}
						
		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception when getting information on backup item.\nSource Path=" + sourcePath + "\nTargetpath=" + targetPath, e);
		}
	}

}
