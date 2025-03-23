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
import java.util.Objects;
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

public class BackUpScannerThread {

	private static final Logger pLog = Logger.getLogger(BackUpScannerThread.class.getName());
	
	private boolean stopAsked;

	private List<Path> filesVisitFailed;

	private BackUpItemList backUpItemList;

	private BackUpCounters backUpCounters;

	private final BackUpTask backUpTask;

	private final FileComparator fileComparator;
	private final BackUpItem.BackupAction acionOnSameTargetContentButNewer;
	
	private Path currentFile;	
	private String status;	
	private boolean done;
	private final int maxDepth;
	private int	currDepth;

	public BackUpScannerThread(BackUpTask but) {
		
		stopAsked = false;
		backUpTask = but;
		maxDepth = Config.getMaxDepth();

		backUpCounters = new BackUpCounters();
		done = false;

		status = backUpTask.toString() + " ";
		fileComparator = new FileComparator(pLog);
		acionOnSameTargetContentButNewer = Config.getAcionOnSameTargetContentButNewer();
	}

	public void stopAsked(boolean b) {
		stopAsked = b;
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

		Path sourcePath = backUpTask.getSource();
		Path targetPath = backUpTask.getTarget();

		currentFile = sourcePath;
		
		backUpCounters.reset();

		filesVisitFailed = new ArrayList<Path>();

		backUpItemList = new BackUpItemList();
		
		try {
			PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(sourcePath, targetPath);
			if (pathPairBasicAttributes.sourceIsDirectory()) {
				currDepth = 0;
				directoryCompare(pathPairBasicAttributes);
			} else {
				topLevelFileCompare(pathPairBasicAttributes);
			}
		} catch (Exception e) {
			pLog.log(Level.SEVERE, "Exception when comparing directory " + sourcePath + " with " + targetPath, e);
		}
		backUpCounters.nbSourceFilesProcessed++;

		long nbFilesProcessed = backUpCounters.nbSourceFilesProcessed + backUpCounters.nbTargetFilesProcessed;
		status = status + "| Scan done ";
		done = true;
		if (backUpTask.compareContent()) {
			status = status + "with content compare ";
		} else if (backUpTask.compareContentOnAmbiguous()) {
			status = status + "with content compare on ambiguous files ";
		}
		status = status + "| Number of files processed: " + nbFilesProcessed;
		ScannerThreadResponse resp = new ScannerThreadResponse(backUpTask, backUpItemList, backUpCounters, filesVisitFailed, status);
		return resp ;
	}
		
	 // Walk directory tree without using SimpleFileVisitor class (much faster)
	private void directoryCompare(PathPairBasicAttributes pathPairBasicAttributes) {
		
		boolean targetIsDirectory = true;
		Path sourceDirectory = pathPairBasicAttributes.getSourcePath();
		Path targetDirectory = pathPairBasicAttributes.getTargetPath();
		
		HashMap<Path,PathPairBasicAttributes> filesBasicAttributes = new HashMap<Path, PathPairBasicAttributes>();
		
		// Get source directory files attributes
		if (! stopAsked) {
			
			 try (DirectoryStream<Path> sourceFileStream = Files.newDirectoryStream(sourceDirectory)) {
				 
				 for (Path sourceFile : sourceFileStream) {				 
					 filesBasicAttributes.put(sourceFile.getFileName(), new PathPairBasicAttributes(sourceFile, null));
				 }
			 } catch (Exception e) {
				 backUpCounters.nbSourceFilesFailed++ ;
				 filesVisitFailed.add(sourceDirectory) ;
				pLog.log(Level.SEVERE, "Exception when scanning directory " + Objects.toString(sourceDirectory), e);
			}
		}
		
		// Get target directory files attributes
		if (! stopAsked) {

			if (pathPairBasicAttributes.targetIsDirectory()) {
				try (DirectoryStream<Path> targetFileStream = Files.newDirectoryStream(targetDirectory)) {

					for (Path targetFile : targetFileStream) {

						Path targetFileName = targetFile.getFileName();

						PathPairBasicAttributes pairFiles = filesBasicAttributes.get(targetFileName);
						if (pairFiles == null) {
							// no corresponding source file : target is to be deleted

							PathPairBasicAttributes onlyTargetNotNull = new PathPairBasicAttributes(null, targetFile);
							BackupAction action;
							if (onlyTargetNotNull.targetIsDirectory()) {
								action = BackupAction.DELETE_DIR ;
							} else {
								action = BackupAction.DELETE ;
							}
							backUpItemList.add(new BackUpItem(onlyTargetNotNull, action, sourceDirectory, backUpCounters));

						} else {
							pairFiles.setTargetPath(targetFile);
						}
						backUpCounters.nbTargetFilesProcessed++ ; 
					}
				} catch (IOException e) {
					backUpCounters.nbTargetFilesFailed++ ; 
					filesVisitFailed.add(targetDirectory) ;
					pLog.log(Level.SEVERE, "Exception when scanning directory " + Objects.toString(targetDirectory), e);
				}
			} else {
				
				targetIsDirectory = false ;
				if (pathPairBasicAttributes.targetExists()) {
				// source is a directory but target is not : delete target and copy source tree
					
					pLog.warning("Source " + sourceDirectory + " is a directory\n" + "but target is not " + targetDirectory);
					backUpItemList.add(new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE, sourceDirectory, backUpCounters));
				} else {
					// source is a directory but target does not exists : copy source tree				
					pLog.warning("Source " + sourceDirectory + " is a directory\n" + "but target does not exists " + targetDirectory);
				}
				backUpItemList.add(new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_TREE, BackupStatus.DIFFERENT, backUpCounters)) ;
			}
		}
		
		// Compare source and target
		if ((! stopAsked) && (targetIsDirectory)) {
			for (Map.Entry<Path, PathPairBasicAttributes> entry : filesBasicAttributes.entrySet()) {
				
				PathPairBasicAttributes pairBasicAttributes = entry.getValue();
				Path srcPath = pairBasicAttributes.getSourcePath();
				BasicFileAttributes sourceAttributes = pairBasicAttributes.getSourceBasicAttributes();			
				currentFile = srcPath;
				if (sourceAttributes != null) {
					if (pairBasicAttributes.noTargetPath()) {
						// no target file, copy source
										
						pairBasicAttributes.setTargetPath(targetDirectory.resolve(sourceDirectory.relativize(srcPath)));
						
						if (sourceAttributes.isDirectory()) {
							// source is a directory
							backUpItemList.add(new BackUpItem(pairBasicAttributes, BackupAction.COPY_TREE, BackupStatus.DIFFERENT, backUpCounters));							
						} else {
							// source is a file						
							backUpItemList.add(new BackUpItem(pairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, backUpCounters)) ;						
						}
					} else {
						
						Path tgtPath = pairBasicAttributes.getTargetPath() ;
						
						if (sourceAttributes.isDirectory()) {
							// source is a directory
							
							// recursively call directoryCompare
							if (currDepth < maxDepth) {
								currDepth++ ;
								directoryCompare(pairBasicAttributes) ;
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
									backUpItemList.add(new BackUpItem(pairBasicAttributes, BackupAction.DELETE_DIR, sourceDirectory, backUpCounters));
									backUpItemList.add(new BackUpItem(pairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, backUpCounters));
								} else {
									compareFile(pairBasicAttributes) ;	
								}
							} else {
								pLog.severe("Failed to get target file attributes for " + Objects.toString(tgtPath)) ;
								filesVisitFailed.add(tgtPath) ;
							}
						}
					}
					backUpCounters.nbSourceFilesProcessed++ ;
				} else {
					pLog.severe("Failed to get source file attributes for " + Objects.toString(srcPath)) ;
					filesVisitFailed.add(srcPath) ;
				}
			}
		}
	}
	
	private void compareFileContent(
			PathPairBasicAttributes pathPairBasicAttributes, 
			BackupAction backupActionOnDifferent,
			BackupAction backupActionOnEqual) {
		
		if (! fileComparator.haveSameContent(pathPairBasicAttributes.getSourcePath(), pathPairBasicAttributes.getTargetPath())) {
			// file content are not the same (or there has been an error)
			if (fileComparator.isOnError()) {
				filesVisitFailed.add(pathPairBasicAttributes.getTargetPath());
				backUpCounters.nbTargetFilesFailed++; 
			} else if (backupActionOnDifferent != null) {
				// content are not the same
				backUpItemList.add( new BackUpItem(pathPairBasicAttributes, backupActionOnDifferent, BackupStatus.DIFF_BY_CONTENT, backUpCounters));
				backUpCounters.contentDifferentNb++;
			}
		} else if (backupActionOnEqual != null) {
			backUpItemList.add(new BackUpItem(pathPairBasicAttributes, backupActionOnEqual, BackupStatus.SAME_CONTENT, backUpCounters));
		}
		
	}
	
	private void compareFile(PathPairBasicAttributes pathPairBasicAttributes) {

		try {

			if (backUpTask.compareContent()) {
				// Content comparison is asked to be sure

				compareFileContent(pathPairBasicAttributes, BackupAction.COPY_REPLACE, null);

			} else {

				// Check if files seems to be the same or no
				// Compare last modified time
				BasicFileAttributes sourceAttributes = pathPairBasicAttributes.getSourceBasicAttributes();
				BasicFileAttributes targetAttributes = pathPairBasicAttributes.getTargetBasicAttributes();
				FileTime f1t = sourceAttributes.lastModifiedTime();
				FileTime f2t = targetAttributes.lastModifiedTime();
				int compareFile = f1t.compareTo(f2t);
				long sizeDiff = sourceAttributes.size() - targetAttributes.size();

				if (compareFile == 0) {
					// Same last modified time

					if (sizeDiff != 0) {
						// different size
						backUpItemList.add(new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_REPLACE, BackupStatus.DIFFERENT, backUpCounters));
					}					
				} else if (compareFile > 0) {
					// Source file is newer
					
					backUpItemList.add(new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_REPLACE, BackupStatus.DIFFERENT, backUpCounters));
					
				} else if (compareFile < 0) {
					// target file is newer
					
					if (backUpTask.compareContentOnAmbiguous()) {
						compareFileContent(pathPairBasicAttributes, BackupAction.COPY_REPLACE, acionOnSameTargetContentButNewer);
					} else {
						backUpItemList.add(new BackUpItem(pathPairBasicAttributes, BackupAction.AMBIGUOUS, BackupStatus.DIFFERENT, backUpCounters));
					}
				} 
			}
		} catch (Exception e) {
			filesVisitFailed.add(pathPairBasicAttributes.getTargetPath());
			backUpCounters.nbTargetFilesFailed++;
			pLog.log(Level.SEVERE, "Exception when comparing file " + pathPairBasicAttributes.getSourcePath() + " and " + pathPairBasicAttributes.getTargetPath(), e);

		}		
	}
	
	// This method is only called if the top level source path is not a directory (so is a file or does not exists)
	private void topLevelFileCompare(PathPairBasicAttributes pathPairBasicAttributes) {

		Path srcPath = pathPairBasicAttributes.getSourcePath();
		Path tgtPath = pathPairBasicAttributes.getTargetPath();
		try {
			
			if (pathPairBasicAttributes.sourceExists()) {
				
				if (pathPairBasicAttributes.targetExists()) {
					if (pathPairBasicAttributes.targetIsDirectory()) {
						// source is a file but target is a directory : delete target dir, copy source file 
						pLog.warning("Source " + srcPath + " is a file\n" + "but target is a directory " + tgtPath);
						backUpItemList.add(new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE_DIR, srcPath, backUpCounters));
						backUpItemList.add(new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, backUpCounters));
					}  else {
						compareFile(pathPairBasicAttributes);
					}
				} else {
					backUpItemList.add(new BackUpItem(pathPairBasicAttributes, BackupAction.COPY_NEW, BackupStatus.DIFFERENT, backUpCounters)) ;
				}
			} else {
				// Source path does not exist : delete target
				pLog.info("Source path does not exist: " + srcPath);
				if (pathPairBasicAttributes.targetExists()) {
					if (pathPairBasicAttributes.targetIsDirectory()) {
						backUpItemList.add(new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE_DIR, srcPath.getParent(), backUpCounters));
					}  else {
						backUpItemList.add(new BackUpItem(pathPairBasicAttributes, BackupAction.DELETE, srcPath.getParent(), backUpCounters));
					}
				} 
			}
		} catch (Exception e) {
			filesVisitFailed.add(pathPairBasicAttributes.getTargetPath());
			backUpCounters.nbTargetFilesFailed++;
			pLog.log(Level.SEVERE, "Exception when comparing top level file " + srcPath + " and " + tgtPath, e);

		}	
	}
	
}
