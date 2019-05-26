package org.fl.backupFiles.gui.workers;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.fl.backupFiles.BackUpCounters;
import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.fl.backupFiles.gui.BackUpJobInfoTableModel;
import org.fl.backupFiles.gui.BackUpTableModel;
import org.fl.backupFiles.gui.ProgressInformationPanel;
import org.fl.backupFiles.gui.UiControl;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpJobInformation;
import org.fl.backupFiles.BackUpTask;
import org.fl.backupFiles.BackupFilesInformation;
import org.fl.backupFiles.Config;
import org.fl.backupFiles.JobsChoice;


public class FilesBackUpScanner extends SwingWorker<String,BackupFilesInformation> {

	private Logger pLog ;
	
	private UiControl uiControl ;
	
	private BackUpTableModel 		backUpTableModel ;
	private BackUpItemList 			backUpItemList ;
	private BackUpJobInfoTableModel backUpJobInfoTableModel ;
	
	private ProgressInformationPanel progressPanel;
	
	private JobsChoice		jobsChoice ;
	private JobTaskType 	jobTaskType ;
	private ArrayList<Path> filesVisitFailed ;
	
	private long 	refreshRate ;
	private BackUpCounters backUpCounters ;
	
	public FilesBackUpScanner(UiControl u, JobTaskType jtt, JobsChoice jc, BackUpTableModel b, ProgressInformationPanel pip, BackUpJobInfoTableModel bj, Logger l)  {
		super();
		pLog 			 = l ;
		uiControl		 = u ;
		backUpTableModel = b ;	
		refreshRate 	 = Config.getScanRefreshRate() ;
		progressPanel	 = pip ;
		
		// back up items
		backUpItemList = backUpTableModel.getBackUpItems() ;
		
		jobsChoice 	= jc ;
		jobTaskType	= jtt ;
		
		backUpJobInfoTableModel = bj ;
		
		backUpCounters = new BackUpCounters() ;
	}

	@Override
	protected String doInBackground() throws Exception {
		
		pLog.info("Scan triggered for " + jobsChoice.getTitleAsString()) ;
		backUpItemList.clear() ;
		
		backUpCounters.reset();
		
		filesVisitFailed = new ArrayList<Path>() ;	

		long 				  startTime   = System.currentTimeMillis() ;
		Path 				  sourcePath  = null ;
		ArrayList<BackUpTask> backUpTasks = jobsChoice.getTasks(jobTaskType) ;
		StringBuilder 		  jobProgress = new StringBuilder(1024) ;
		
		try {
			if (backUpTasks != null) {
				
				ExecutorService scannerExecutor = Config.getScanExecutorService() ;
				
				ArrayList<BackUpScannerTask> results = new ArrayList<BackUpScannerTask>() ;
				
				// Launch one thread per backUpTask
				for (BackUpTask backUpTask : backUpTasks) {

					sourcePath = backUpTask.getSource() ;
					
					if (Files.exists(sourcePath)) {
						
						BackUpScannerThread backUpScannerThread = new BackUpScannerThread(backUpTask, pLog) ;
						Future<ScannerThreadResponse> backUpRes =  scannerExecutor.submit(backUpScannerThread) ;
						results.add(new BackUpScannerTask(backUpScannerThread, backUpRes)) ;
						
					} else {
						pLog.warning("Source path does not exist: " + sourcePath) ;
					}
				}

				int nbActiveTasks = results.size() ;
				
				// Get results from backUpTask threads as they completes
				while (nbActiveTasks > 0) {
										
					jobProgress.setLength(0) ;
					for (BackUpScannerTask oneResult : results) {
						
						if ( (! oneResult.isResultRecorded()) &&
							 (oneResult.getFutureResponse().isDone())) {
							// one backUpTask has finished
								
							nbActiveTasks-- ;								
							try {
								ScannerThreadResponse scannerResp = oneResult.getFutureResponse().get() ;
								backUpCounters.add(scannerResp.getBackUpCounters());
								filesVisitFailed.addAll(scannerResp.getFilesVisitFailed()) ;
								backUpItemList.addAll(scannerResp.getBackUpItemList());
							} catch (Exception e) {
								pLog.log(Level.SEVERE, "Exception in scanner thread " + oneResult.getBackUpScannerThread().getCurrentStatus(uiControl.isStopAsked()));
							}
							oneResult.setResultRecorded(true) ;
						
						} 
						jobProgress.append(oneResult.getBackUpScannerThread().getCurrentStatus(uiControl.isStopAsked())).append("\n") ;
					}
					
					// Refresh progress information
					publish(new BackupFilesInformation("Comparaison de fichiers en cours", jobProgress.toString(), backUpCounters.nbSourceFilesProcessed + backUpCounters.nbTargetFilesProcessed)) ;

					if (nbActiveTasks > 0) {
						try {
							Thread.sleep(refreshRate) ;
						} catch (InterruptedException e) {
						}
					}

				}
								
				publish(new BackupFilesInformation("Comparaison de fichiers terminée (" + jobTaskType.toString() + " - " + jobsChoice.getTitleAsString() + ")", backUpCounters.toString(), backUpCounters.nbSourceFilesProcessed + backUpCounters.nbTargetFilesProcessed)) ;
			} else {
				pLog.warning("back up tasks is null") ;
				publish(new BackupFilesInformation("Aucune taches à effectuer", backUpCounters.toString(), backUpCounters.nbSourceFilesProcessed + backUpCounters.nbTargetFilesProcessed)) ;
			}
					
		} catch (Exception e) {
			pLog.log(Level.SEVERE, "IOException when walking file tree " + sourcePath, e) ;
		}
		
		long endTime = System.currentTimeMillis() ;
		long duration = endTime - startTime ;
		
		pLog.info(getScanInfoText(jobProgress, duration)) ;
		
		String scanResult = getScanInfoHtml(duration) ;
		String compareType = "Comparaison" ;
		if (jobsChoice.compareContent()) {
			compareType = compareType + " avec comparaison du contenu" ;
		}
		BackUpJobInformation jobInfo = new BackUpJobInformation( jobsChoice.getTitleAsHtml(), endTime, scanResult, compareType, jobTaskType.toString()) ;
		backUpJobInfoTableModel.add(jobInfo) ;
		
		uiControl.setIsRunning(false) ;
		uiControl.setStopAsked(false) ;
		return null;
	}


	 @Override
     protected void process(java.util.List<BackupFilesInformation> chunks) {
		 
        // Get the latest result from the list
		 BackupFilesInformation latestResult = chunks.get(chunks.size() - 1);
		 
		 backUpTableModel.fireTableDataChanged();
		 		 
        progressPanel.setStepInfos(latestResult.getInformation(), latestResult.getNbFilesProcessed());
	    progressPanel.setProcessStatus(latestResult.getStatus());
       
     }
	
	 @Override
	 protected void done() {
		 
		 backUpTableModel.fireTableDataChanged() ;
		 backUpJobInfoTableModel.fireTableDataChanged() ;
	 }
	 
 
	 private String getScanInfoHtml(long duration) {
		 
		 StringBuilder scanInfo = new StringBuilder(1024) ;
		 scanInfo.append("<html><body>") ;
		 backUpCounters.appendHtmlFragment(scanInfo) ;
		 scanInfo.append("<p>Durée scan (ms)= ").append(duration) ;
		 if ((filesVisitFailed != null) && (! filesVisitFailed.isEmpty())) {
			 scanInfo.append("<br>Fichiers visités en erreur:") ;
			 for (Path fileOnError : filesVisitFailed) {
				 scanInfo.append("<br>").append(fileOnError) ;
			 }
		 }
		 scanInfo.append("</body></html>") ;
		 return scanInfo.toString() ;
	 }
	 
	 private String getScanInfoText(StringBuilder scanInfo, long duration) {

		 scanInfo.append(backUpCounters.toString()).append("\nScan duration (ms)= ").append(duration) ;
		 if ((filesVisitFailed != null) && (! filesVisitFailed.isEmpty())) {
			 scanInfo.append("\nFichiers visités en erreur:") ;
			 for (Path fileOnError : filesVisitFailed) {
				 scanInfo.append("\n").append(fileOnError) ;
			 }
		 }
		 return jobsChoice.getTitleAsString() + "\n" + scanInfo.toString() ;
	 }
	 
}
