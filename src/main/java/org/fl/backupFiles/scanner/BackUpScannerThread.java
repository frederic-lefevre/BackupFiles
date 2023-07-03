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

package org.fl.backupFiles.scanner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpCounters;
import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpTask;
import org.fl.backupFiles.Config;
import org.fl.backupFiles.BackUpItem.BackupAction;
import org.fl.backupFiles.BackUpItem.BackupStatus;
import org.fl.util.file.FileComparator;
import org.fl.util.file.FilesUtils;

public class BackUpScannerThread {

	private boolean stopAsked ;

	private List<Path> filesVisitFailed ;
	
	private BackUpItemList 	backUpItemList ;
	
	private BackUpCounters backUpCounters ;
	
	private final BackUpTask backUpTask ;
	
	private final Logger pLog ;
	
	private final FileComparator fileComparator ;
	
	private Path currentFile;	
	private String status;	
	private boolean done;
	private final int maxDepth;
	private int	currDepth;

	public BackUpScannerThread(BackUpTask but, Logger l) {
		
		stopAsked	= false ;
		backUpTask  = but ;
		maxDepth	= Config.getMaxDepth() ;
		pLog = l ;
		
		backUpCounters = new BackUpCounters() ;
		done = false ;

		status = backUpTask.toString() + " ";
		fileComparator = new FileComparator(pLog);
	}

	public void stopAsked(boolean b) {
		stopAsked = b ;
	}
	
	public String getCurrentStatus() {
		
		if (done) {
			return status ;
		} else {
			long nbFilesProcessed = backUpCounters.nbSourceFilesProcessed + backUpCounters.nbTargetFilesProcessed ;
			return status + nbFilesProcessed + " " + currentFile ;
		}
	}

	public ScannerThreadResponse scan() {

		Path sourcePath  = backUpTask.getSource() ; 
		Path targetPath  = backUpTask.getTarget() ;

		currentFile = sourcePath ;
		
		backUpCounters.reset();

		filesVisitFailed = new ArrayList<Path>();

		backUpItemList = new BackUpItemList();
		
		try {
			if ((Files.exists(sourcePath)) && (Files.isDirectory(sourcePath))) {
				currDepth = 0 ;
				directoryCompare(sourcePath, targetPath) ;
			} else {
				topLevelFileCompare(sourcePath, targetPath) ;
			}
		} catch (Exception e) {
			pLog.log(Level.SEVERE, "Exception when comparing directory " + sourcePath + " with " + targetPath, e);
		}
		backUpCounters.nbSourceFilesProcessed++ ;

		long nbFilesProcessed = backUpCounters.nbSourceFilesProcessed + backUpCounters.nbTargetFilesProcessed ;
		status = status + "| Scan done " ;
		done = true ;
		if (backUpTask.compareContent()) {
			status = status + "with content compare " ;
		} else if (backUpTask.compareContentOnAmbiguous()) {
			status = status + "with content compare on ambiguous files " ;
		}
		status = status + "| Number of files processed: " + nbFilesProcessed ;
		ScannerThreadResponse resp = new ScannerThreadResponse(backUpTask, backUpItemList, backUpCounters, filesVisitFailed, status) ;
		return resp ;
	}
		
	 // Walk directory tree without using SimpleFileVisitor class (much faster)
	private void directoryCompare(Path sourceDirectory, Path targetDirectory) {
		
		boolean targetIsDirectory = true ;
		HashMap<Path,PathPairBasicAttributes> filesBasicAttributes = new HashMap<Path, PathPairBasicAttributes>() ;
		
		// Get source directory files attributes
		if (! stopAsked) {
			
			 try (DirectoryStream<Path> sourceFileStream = Files.newDirectoryStream(sourceDirectory)) {
				 
				 for (Path sourceFile : sourceFileStream) {				 
					 filesBasicAttributes.put(sourceFile.getFileName(), new PathPairBasicAttributes(sourceFile, pLog)) ;
				 }
			 } catch (Exception e) {
				 backUpCounters.nbSourceFilesFailed++ ;
				 filesVisitFailed.add(sourceDirectory) ;
				pLog.log(Level.SEVERE, "Exception when scanning directory " + sourceDirectory, e);
			}
		}
		
		// Get target directory files attributes
		if (! stopAsked) {

			if (Files.isDirectory(targetDirectory)) {
				try (DirectoryStream<Path> targetFileStream = Files.newDirectoryStream(targetDirectory)) {

					for (Path targetFile : targetFileStream) {

						Path targetFileName = targetFile.getFileName() ;

						PathPairBasicAttributes pairFiles = filesBasicAttributes.get(targetFileName) ;
						if (pairFiles == null) {
							// no corresponding source file : target is to be deleted

							BackupAction action ;
							long sizeDiff ;
							if (Files.isDirectory(targetFile)) {
								action = BackupAction.DELETE_DIR ;
								sizeDiff = 0 - FilesUtils.folderSize(targetFile, pLog) ;
							} else {
								action = BackupAction.DELETE ;
								sizeDiff = 0 - Files.size(targetFile) ;
							}
							backUpItemList.add(new BackUpItem(targetFile, action, sourceDirectory, sizeDiff, backUpCounters, pLog)) ;

						} else {
							pairFiles.setTargetPath(targetFile);
						}
						backUpCounters.nbTargetFilesProcessed++ ; 
					}
				} catch (IOException e) {
					backUpCounters.nbTargetFilesFailed++ ; 
					filesVisitFailed.add(targetDirectory) ;
					pLog.log(Level.SEVERE, "Exception when scanning directory " + targetDirectory, e);
				}
			} else {
				
				targetIsDirectory = false ;
				if (Files.exists(targetDirectory)) {
				// source is a directory but target is not : delete target and copy source tree
					
					pLog.warning("Source " + sourceDirectory + " is a directory\n" + "but target is not " + targetDirectory);
					long sizeDiff = 0;
					try {
						sizeDiff = 0 - Files.size(targetDirectory);
					} catch (IOException e) {
						pLog.log(Level.SEVERE, "IOException when getting the size of " + targetDirectory, e);
					}
					backUpItemList.add(new BackUpItem(targetDirectory, BackupAction.DELETE, sourceDirectory, sizeDiff, backUpCounters, pLog)) ;
				} else {
					// source is a directory but target does not exists : copy source tree				
					pLog.warning("Source " + sourceDirectory + " is a directory\n" + "but target does not exists " + targetDirectory);
				}
				long sizeDiff = FilesUtils.folderSize(sourceDirectory, pLog) ;
				backUpItemList.add(new BackUpItem(sourceDirectory, targetDirectory, BackupAction.COPY_TREE, BackupStatus.DIFFERENT, sizeDiff, backUpCounters, pLog)) ;
			}
		}
		
		// Compare source and target
		if ((! stopAsked) && (targetIsDirectory)) {
			for (Map.Entry<Path, PathPairBasicAttributes> entry : filesBasicAttributes.entrySet()) {
				
				PathPairBasicAttributes pairBasicAttributes = entry.getValue() ;
				Path srcPath = pairBasicAttributes.getSourcePath() ;
				BasicFileAttributes sourceAttributes = pairBasicAttributes.getSourceBasicAttributes() ;			
				currentFile = srcPath ;
				if (sourceAttributes != null) {
					if (pairBasicAttributes.noTargetPath()) {
						// no target file, copy source
										
						Path tgtPath = targetDirectory.resolve(sourceDirectory.relativize(srcPath)) ;
						
						if (sourceAttributes.isDirectory()) {
							// source is a directory
							long sizeDiff = FilesUtils.folderSize(srcPath, pLog) ;
							backUpItemList.add(new BackUpItem(srcPath, tgtPath, BackupAction.COPY_TREE, BackupStatus.DIFFERENT, sizeDiff, backUpCounters, pLog)) ;
							
						} else {
							// source is a file
							
							BackUpItem copyNewItem = new BackUpItem(srcPath, tgtPath, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, sourceAttributes.size(), backUpCounters, pLog) ;
							backUpItemList.add(copyNewItem) ;						
						}
					} else {
						
						Path tgtPath = pairBasicAttributes.getTargetPath() ;
						
						if (sourceAttributes.isDirectory()) {
							// source is a directory
							
							// recursively call directoryCompare
							if (currDepth < maxDepth) {
								currDepth++ ;
								directoryCompare(srcPath, tgtPath) ;
								currDepth-- ;
							} else {
								pLog.severe("Directory max depth reached. Depth=" + currDepth + "\non source path " + srcPath) ;
							}
							
						} else {
							// source is a file
							
							BasicFileAttributes targetAttributes = pairBasicAttributes.getTargetBasicAttributes() ;
							
							if (targetAttributes != null) {
								if (targetAttributes.isDirectory()) {
									// source is a file but target is a directory : delete target dir, copy source file 
									pLog.warning("Source " + srcPath + " is a file\n" + "but target is a directory " + tgtPath);
									long sizeDiff = 0 - FilesUtils.folderSize(tgtPath, pLog) ;
									backUpItemList.add(new BackUpItem(tgtPath, BackupAction.DELETE_DIR, sourceDirectory, sizeDiff, backUpCounters, pLog)) ;
									BackUpItem copyNewItem = new BackUpItem(srcPath, tgtPath, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, sourceAttributes.size(), backUpCounters, pLog) ;
									backUpItemList.add(copyNewItem) ;
								} else {
									compareFile(srcPath, tgtPath, sourceAttributes, targetAttributes) ;	
								}
							} else {
								pLog.severe("Failed to get target file attributes for " + tgtPath) ;
								filesVisitFailed.add(tgtPath) ;
							}
						}
					}
					backUpCounters.nbSourceFilesProcessed++ ;
				} else {
					pLog.severe("Failed to get source file attributes for " + srcPath) ;
					filesVisitFailed.add(srcPath) ;
				}
			}
		}
	}
	
	private void compareFileContent(
			Path srcPath, 
			Path tgtPath, 
			BasicFileAttributes sourceAttributes, 
			BasicFileAttributes targetAttributes, 
			BackupAction backupActionOnDifferent,
			BackupAction backupActionOnEqual) {
		
		if (! fileComparator.haveSameContent(srcPath, tgtPath)) {
			// file content are not the same (or there has been an error)
			if (fileComparator.isOnError()) {
				filesVisitFailed.add(tgtPath);
				backUpCounters.nbTargetFilesFailed++; 
			} else if (backupActionOnDifferent != null) {
				// content are not the same
				long sizeDiff = sourceAttributes.size() - targetAttributes.size();
				BackUpItem backUpItem = new BackUpItem(srcPath, tgtPath, backupActionOnDifferent, BackupStatus.DIFF_BY_CONTENT, sizeDiff, backUpCounters, pLog);
				backUpItemList.add(backUpItem);
				backUpCounters.contentDifferentNb++;
			}
		} else if (backupActionOnEqual != null) {
			long sizeDiff = sourceAttributes.size() - targetAttributes.size();
			BackUpItem backUpItem = new BackUpItem(srcPath, tgtPath, backupActionOnEqual, BackupStatus.SAME_CONTENT, sizeDiff, backUpCounters, pLog);
			backUpItemList.add(backUpItem);
		}
		
	}
	
	private void compareFile(Path srcPath, Path tgtPath, BasicFileAttributes sourceAttributes, BasicFileAttributes targetAttributes) {

		try {

			if (backUpTask.compareContent()) {
				// Content comparison is asked to be sure

				compareFileContent(srcPath, tgtPath, sourceAttributes, targetAttributes, BackupAction.COPY_REPLACE, null);

			} else {

				// Check if files seems to be the same or no
				// Compare last modified time
				FileTime f1t = sourceAttributes.lastModifiedTime() ;
				FileTime f2t = targetAttributes.lastModifiedTime() ;
				int compareFile = f1t.compareTo(f2t) ;

				if (compareFile == 0) {
					// Same last modified time

					// compare size
					long f1s = sourceAttributes.size() ;
					long f2s = targetAttributes.size() ;
					if (f1s != f2s) {
						// different size
						long sizeDiff = f1s - f2s ;
						BackUpItem backUpItem = new BackUpItem(srcPath, tgtPath, BackupAction.COPY_REPLACE, BackupStatus.DIFFERENT, sizeDiff, backUpCounters, pLog) ;
						backUpItemList.add(backUpItem) ;
					}
					
				} else if (compareFile > 0) {
					// Source file is newer
					
					long sizeDiff = sourceAttributes.size() - targetAttributes.size() ;
					BackUpItem backUpItem = new BackUpItem(srcPath, tgtPath, BackupAction.COPY_REPLACE, BackupStatus.DIFFERENT, sizeDiff, backUpCounters, pLog) ;
					backUpItemList.add(backUpItem);
					
				} else if (compareFile < 0) {
					// target file is newer
					
					if (backUpTask.compareContentOnAmbiguous()) {
						compareFileContent(srcPath, tgtPath, sourceAttributes, targetAttributes, BackupAction.COPY_REPLACE, BackupAction.COPY_REPLACE);
					} else {
						long sizeDiff = sourceAttributes.size() - targetAttributes.size() ;
						BackUpItem backUpItem = new BackUpItem(srcPath, tgtPath, BackupAction.AMBIGUOUS, BackupStatus.DIFFERENT, sizeDiff, backUpCounters, pLog) ;
						backUpItemList.add(backUpItem) ;
					}
				} 
			}
		} catch (Exception e) {
			filesVisitFailed.add(tgtPath) ;
			backUpCounters.nbTargetFilesFailed++ ;
			pLog.log(Level.SEVERE, "Exception when comparing file " + srcPath + " and " + tgtPath, e);

		}		
	}
	
	// This method is only called if the top level source path is not a directory (so is a file or does not exists)
	private void topLevelFileCompare(Path srcPath, Path tgtPath) {

		try {
			
			if (Files.exists(srcPath)) {
				BasicFileAttributes sourceAttributes = Files.readAttributes(srcPath, BasicFileAttributes.class);
				
				if (Files.exists(tgtPath)) {
					BasicFileAttributes targetAttributes = Files.readAttributes(tgtPath, BasicFileAttributes.class);
					if (targetAttributes.isDirectory()) {
						// source is a file but target is a directory : delete target dir, copy source file 
						pLog.warning("Source " + srcPath + " is a file\n" + "but target is a directory " + tgtPath);
						long sizeDiff = 0 - FilesUtils.folderSize(tgtPath, pLog) ;
						backUpItemList.add(new BackUpItem(tgtPath, BackupAction.DELETE_DIR, srcPath, sizeDiff, backUpCounters, pLog)) ;
						BackUpItem copyNewItem = new BackUpItem(srcPath, tgtPath, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, sourceAttributes.size(), backUpCounters, pLog) ;
						backUpItemList.add(copyNewItem) ;
					}  else {
						compareFile(srcPath, tgtPath, sourceAttributes, targetAttributes) ;
					}
				} else {
					BackUpItem copyNewItem = new BackUpItem(srcPath, tgtPath, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, sourceAttributes.size(), backUpCounters, pLog) ;
					backUpItemList.add(copyNewItem) ;
				}
			} else {
				// Source path does not exist : delete target
				pLog.warning("Source path does not exist: " + srcPath) ;
				if (Files.exists(tgtPath)) {
					BasicFileAttributes targetAttributes = Files.readAttributes(tgtPath, BasicFileAttributes.class);
					if (targetAttributes.isDirectory()) {
						long sizeDiff = 0 - FilesUtils.folderSize(tgtPath, pLog) ;
						backUpItemList.add(new BackUpItem(tgtPath, BackupAction.DELETE_DIR, srcPath.getParent(), sizeDiff, backUpCounters, pLog)) ;
					}  else {
						long sizeDiff = 0 - targetAttributes.size() ;
						BackUpItem deleteItem = new BackUpItem(tgtPath, BackupAction.DELETE, srcPath.getParent(), sizeDiff, backUpCounters, pLog) ;
						backUpItemList.add(deleteItem) ;
					}
				} 
			}
		} catch (Exception e) {
			filesVisitFailed.add(tgtPath) ;
			backUpCounters.nbTargetFilesFailed++ ;
			pLog.log(Level.SEVERE, "Exception when comparing file " + srcPath + " and " + tgtPath, e);

		}	
	}
	
}
