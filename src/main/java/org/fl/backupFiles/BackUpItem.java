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

import com.ibm.lge.fl.util.file.FileComparator;
import com.ibm.lge.fl.util.file.FilesSecurityUtils;
import com.ibm.lge.fl.util.file.FilesUtils;

public class BackUpItem {

	private static long fileSizeWarningThreshold ;
	public static void setFileSizeWarningThreshold(long fileSizeWarningThreshold) {
		BackUpItem.fileSizeWarningThreshold = fileSizeWarningThreshold;
	}
	
	public enum BackupAction { COPY_NEW, COPY_REPLACE, COPY_TREE, DELETE, DELETE_DIR, AMBIGUOUS } ;
	
	public enum BackupStatus { DIFFERENT, DIFF_BY_CONTENT, DONE, FAILED } ;

	private final Path	 				   sourcePath ;
	private final Path 		 			   sourceClosestExistingPath ;
	private final Path 		 			   targetPath ;
	private final BackupAction 			   backupAction ;
	private final long					   sizeDifference ;
	private BackupStatus 				   backupStatus ;
	private boolean		 				   diffByContent ;
	private final DirectoryPermanenceLevel permanenceLevel ;
	private final Logger				   bLog ;
	
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
	//		TO_BE_DONE	 : the back up is to be done
	//		DONE	 	 : the back up has been done
	//		FAILED	 	 : the back up has failed
	
	public BackUpItem(Path src, Path tgt, Path srcExisting, BackupAction bst, long sd, BackUpCounters backUpCounters, Logger l) {
		sourcePath 	 	 		  = src ;
		sourceClosestExistingPath = srcExisting ;
		targetPath 	 	 		  = tgt ;
		backupAction 	 		  = bst ;
		backupStatus 	 		  = BackupStatus.DIFFERENT ;
		diffByContent		  	  = false ;
		bLog 		 	 		  = l ;
		sizeDifference			  = sd ;
		if (targetPath != null) {
			permanenceLevel		  = Config.getDirectoryPermanence().getPermanenceLevel(targetPath) ;
		} else if (src != null) {
			permanenceLevel		  = Config.getDirectoryPermanence().getPermanenceLevel(src) ;
		} else if (srcExisting != null) {
			permanenceLevel		  = Config.getDirectoryPermanence().getPermanenceLevel(srcExisting) ;
		} else {
			permanenceLevel		  = DirectoryPermanence.DEFAULT_PERMANENCE_LEVEL ;
		}
		
		// Update counters
		if (backupAction.equals(BackupAction.COPY_REPLACE)) {
			backUpCounters.copyReplaceNb++ ;
		} else if (backupAction.equals(BackupAction.COPY_NEW)) {
			backUpCounters.copyNewNb++ ;
		} else if (backupAction.equals(BackupAction.DELETE)) {
			backUpCounters.deleteNb++ ;
		} else if (backupAction.equals(BackupAction.COPY_TREE)) {
			backUpCounters.copyTreeNb++ ;
		} else if (backupAction.equals(BackupAction.DELETE_DIR)) {
			backUpCounters.deleteDirNb++ ;
		} else if (backupAction.equals(BackupAction.AMBIGUOUS)) {
			backUpCounters.ambiguousNb++ ;
		}
		updateLimtsCounters(backUpCounters) ;
	}

	private void updateLimtsCounters(BackUpCounters backUpCounters) {
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

	public void execute(BackUpCounters backUpCounters) {
		
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

	public boolean diffByContent() {
		return diffByContent;
	}

	public void setDiffByContent(boolean dbc) {
		backupStatus  = BackupStatus.DIFF_BY_CONTENT ;
		diffByContent = dbc;
	}

}
