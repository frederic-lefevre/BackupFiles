package org.fl.backupFiles.scanner;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.fl.backupFiles.BackUpCounters;
import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpTask;
import org.fl.backupFiles.Config;
import org.fl.backupFiles.BackUpItem.BackupAction;

import com.ibm.lge.fl.util.file.FileComparator;

public class BackUpScannerThread {

	private boolean stopAsked ;

	private ArrayList<Path> filesVisitFailed ;
	
	private BackUpItemList 	backUpItemList ;
	
	private BackUpCounters backUpCounters ;
	
	private final BackUpTask backUpTask ;
	
	private Logger pLog ;
	
	private FileComparator fileComparator ;
	
	private Path 	currentFile ;	
	private String  status ;	
	private boolean done ;
	private int 	maxDepth ;
	private int		currDepth ;
	
	public BackUpScannerThread(BackUpTask but, Logger l) {
		
		stopAsked	= false ;
		backUpTask  = but ;
		maxDepth	= Config.getMaxDepth() ;
		pLog = l ;
		
		backUpCounters = new BackUpCounters() ;
		done = false ;

		status = backUpTask.toString() + " " ;
	}

	public String getCurrentStatus(boolean b) {
		
		stopAsked = b ;
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
		
		boolean compareContent = backUpTask.compareContent() ;
		if (compareContent) {
			fileComparator = new FileComparator(pLog) ;
		} else {
			fileComparator = null ;
		}

		currentFile = sourcePath ;
		
		backUpCounters.reset();

		filesVisitFailed = new ArrayList<Path>();

		backUpItemList = new BackUpItemList();
		
		if (Files.isDirectory(sourcePath)) {
			currDepth = 0 ;
			directoryCompare(sourcePath, targetPath, compareContent) ;
		} else {
			topLevelFileCompare(sourcePath, targetPath, compareContent) ;
		}
		backUpCounters.nbSourceFilesProcessed++ ;

		long nbFilesProcessed = backUpCounters.nbSourceFilesProcessed + backUpCounters.nbTargetFilesProcessed ;
		status = status + "| Scan done " ;
		done = true ;
		if (compareContent) {
			status = status + "with content compare " ;
		}
		status = status + "| Number of files processed: " + nbFilesProcessed ;
		ScannerThreadResponse resp = new ScannerThreadResponse(backUpTask, backUpItemList, backUpCounters, filesVisitFailed) ;
		return resp ;
	}
		
	 // Walk directory tree without using SimpleFileVisitor class (much faster)
	private void directoryCompare(Path sourceDirectory, Path targetDirectory, boolean compareContent) {
		
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
							if (Files.isDirectory(targetFile)) {
								action = BackupAction.DELETE_DIR ;
								backUpCounters.deleteDirNb++ ;
							} else {
								action = BackupAction.DELETE ;
								backUpCounters.deleteNb++ ;
							}
							backUpItemList.add(new BackUpItem(null, targetFile, sourceDirectory, action, 0, pLog)) ;

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
					backUpItemList.add(new BackUpItem(null, targetDirectory, sourceDirectory, BackupAction.DELETE, 0, pLog)) ;
				} else {
					// source is a directory but target does not exists : copy source tree				
					pLog.warning("Source " + sourceDirectory + " is a directory\n" + "but target does not exists " + targetDirectory);
				}
				
				backUpItemList.add(new BackUpItem(sourceDirectory, targetDirectory, sourceDirectory, BackupAction.COPY_TREE, 0, pLog)) ;
				backUpCounters.nbSourceFilesProcessed++ ;
			}
		}
		
		// Compare source and target
		if ((! stopAsked) && (targetIsDirectory)) {
			for (Map.Entry<Path, PathPairBasicAttributes> entry : filesBasicAttributes.entrySet()) {
				
				PathPairBasicAttributes pairBasicAttributes = entry.getValue() ;
				BasicFileAttributes sourceAttributes = pairBasicAttributes.getSourceBasicAttributes() ;
				Path srcPath = pairBasicAttributes.getSourcePath() ;
				currentFile = srcPath ;
				if (pairBasicAttributes.noTargetPath()) {
					// no target file, copy source
									
					Path tgtPath = targetDirectory.resolve(sourceDirectory.relativize(srcPath)) ;
					
					if (sourceAttributes.isDirectory()) {
						// source is a directory
						
						backUpItemList.add(new BackUpItem(srcPath, tgtPath, srcPath, BackupAction.COPY_TREE, 0, pLog)) ;
						backUpCounters.copyTreeNb++ ;
						
					} else {
						// source is a file
						
						BackUpItem copyNewItem = new BackUpItem(srcPath, tgtPath, srcPath, BackupAction.COPY_NEW, sourceAttributes.size(), pLog) ;
						backUpItemList.add(copyNewItem) ;						
						backUpCounters.copyNewNb++ ;
					}
				} else {
					
					Path tgtPath = pairBasicAttributes.getTargetPath() ;
					
					if (sourceAttributes.isDirectory()) {
						// source is a directory
						
						// recursively call directoryCompare
						if (currDepth < maxDepth) {
							currDepth++ ;
							directoryCompare(srcPath, tgtPath, compareContent) ;
							currDepth-- ;
						} else {
							pLog.severe("Directory max depth reached. Depth=" + currDepth + "\non source path " + srcPath) ;
						}
						
					} else {
						// source is a file
						
						BasicFileAttributes targetAttributes = pairBasicAttributes.getTargetBasicAttributes() ;
						
						if (targetAttributes.isDirectory()) {
							// source is a file but target is a directory : delete target dir, copy source file 
							pLog.warning("Source " + srcPath + " is a file\n" + "but target is a directory " + tgtPath);
							backUpItemList.add(new BackUpItem(null, tgtPath, sourceDirectory, BackupAction.DELETE_DIR, 0, pLog)) ;
							BackUpItem copyNewItem = new BackUpItem(srcPath, tgtPath, srcPath, BackupAction.COPY_NEW, sourceAttributes.size(), pLog) ;
							backUpItemList.add(copyNewItem) ;
						} else {
							compareFile(srcPath, tgtPath, sourceAttributes, targetAttributes) ;	
						}
					}
				}
				backUpCounters.nbSourceFilesProcessed++ ;
			}
		}
	}
	
	private void compareFile(Path srcPath, Path tgtPath, BasicFileAttributes sourceAttributes, BasicFileAttributes targetAttributes) {

		try {

			if (fileComparator != null) {
				// Content comparison is asked to be sure

				if (! fileComparator.haveSameContent(srcPath, tgtPath)) {
					// file content are not the same (or there has been an error)
					if (fileComparator.isOnError()) {
						filesVisitFailed.add(tgtPath) ;
						backUpCounters.nbTargetFilesFailed++ ; 
					} else {
						// content are not the same
						long diffSize = sourceAttributes.size() - targetAttributes.size() ;
						BackUpItem backUpItem = new BackUpItem(srcPath, tgtPath, srcPath, BackupAction.COPY_REPLACE, diffSize, pLog) ;
						backUpItem.setDiffByContent(true) ;
						backUpItemList.add(backUpItem) ;
						backUpCounters.copyReplaceNb++ ;
						backUpCounters.contentDifferentNb++ ;
					}
				}

			} else {

				// Check if files seems to be the same or no
				// Compare last modfied time
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
						BackUpItem backUpItem = new BackUpItem(srcPath, tgtPath, srcPath, BackupAction.COPY_REPLACE, f1s-f2s, pLog) ;
						backUpItemList.add(backUpItem) ;
						backUpCounters.copyReplaceNb++ ;
					}
				} else if (compareFile > 0) {
					long diffSize = sourceAttributes.size() - targetAttributes.size() ;
					BackUpItem backUpItem = new BackUpItem(srcPath, tgtPath, srcPath, BackupAction.COPY_REPLACE, diffSize, pLog) ;
					backUpItemList.add(backUpItem) ;
					backUpCounters.copyReplaceNb++ ;
				} else if (compareFile < 0) {
					long diffSize = sourceAttributes.size() - targetAttributes.size() ;
					BackUpItem backUpItem = new BackUpItem(srcPath, tgtPath, srcPath, BackupAction.AMBIGUOUS, diffSize, pLog) ;
					backUpItemList.add(backUpItem) ;
					backUpCounters.ambiguousNb++ ;
				} 
			}
		} catch (Exception e) {
			filesVisitFailed.add(tgtPath) ;
			backUpCounters.nbTargetFilesFailed++ ;
			pLog.log(Level.SEVERE, "Exception when comparing file " + srcPath + " and " + tgtPath, e);

		}		
	}
	
	// This method is only called if the top level source path is a file
	private void topLevelFileCompare(Path srcPath, Path tgtPath, boolean compareContent) {

		try {
			BasicFileAttributes sourceAttributes = Files.readAttributes(srcPath, BasicFileAttributes.class);
			
			if (! Files.exists(tgtPath)) {
				Files.createFile(tgtPath) ;
			}
			BasicFileAttributes targetAttributes = Files.readAttributes(tgtPath, BasicFileAttributes.class);
			
			if (targetAttributes.isDirectory()) {
				// source is a file but target is a directory : delete target dir, copy source file 
				pLog.warning("Source " + srcPath + " is a file\n" + "but target is a directory " + tgtPath);
				backUpItemList.add(new BackUpItem(null, tgtPath, srcPath, BackupAction.DELETE_DIR, 0, pLog)) ;
				BackUpItem copyNewItem = new BackUpItem(srcPath, tgtPath, srcPath, BackupAction.COPY_NEW, sourceAttributes.size(), pLog) ;
				backUpItemList.add(copyNewItem) ;
			}  else {
				compareFile(srcPath, tgtPath, sourceAttributes, targetAttributes) ;
			}
		} catch (Exception e) {
			filesVisitFailed.add(tgtPath) ;
			backUpCounters.nbTargetFilesFailed++ ;
			pLog.log(Level.SEVERE, "Exception when comparing file " + srcPath + " and " + tgtPath, e);

		}	
	}
	
}
