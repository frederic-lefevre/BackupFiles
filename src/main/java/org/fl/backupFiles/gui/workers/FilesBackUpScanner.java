package org.fl.backupFiles.gui.workers;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.swing.SwingWorker;

import org.fl.backupFiles.BackUpCounters;
import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.fl.backupFiles.gui.BackUpJobInfoTableModel;
import org.fl.backupFiles.gui.BackUpTableModel;
import org.fl.backupFiles.gui.ProgressInformationPanel;
import org.fl.backupFiles.gui.UiControl;
import org.fl.backupFiles.scanner.BackUpScannerTask;
import org.fl.backupFiles.scanner.BackUpScannerThread;
import org.fl.backupFiles.scanner.ScannerThreadResponse;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpJobInformation;
import org.fl.backupFiles.BackUpTask;
import org.fl.backupFiles.Config;
import org.fl.backupFiles.JobsChoice;

public class FilesBackUpScanner extends SwingWorker<BackUpScannerResult,BackupScannerInformation> {

	private final Logger pLog ;
	
	private final UiControl uiControl ;
	
	private final BackUpTableModel 		   backUpTableModel ;
	private final BackUpItemList 		   backUpItemList ;
	private final BackUpJobInfoTableModel  backUpJobInfoTableModel ;	
	private final ProgressInformationPanel progressPanel;
	
	private final JobsChoice	jobsChoice ;
	private final JobTaskType 	jobTaskType ;
	
	private ArrayList<Path> filesVisitFailed ;
	
	private final long 	refreshRate ;
	private final BackUpCounters backUpCounters ;
	
	public FilesBackUpScanner(UiControl u, JobTaskType jtt, JobsChoice jc, BackUpTableModel b, ProgressInformationPanel pip, BackUpJobInfoTableModel bj, Logger l)  {
		super();
		pLog 			 = l ;
		uiControl		 = u ;
		backUpTableModel = b ;	
		refreshRate 	 = Config.getScanRefreshRate() ;
		progressPanel	 = pip ;
		
		// back up items
		backUpItemList = backUpTableModel.getBackUpItems() ;
		
		jobsChoice 				= jc ;
		jobTaskType				= jtt ;		
		backUpJobInfoTableModel = bj ;
		
		backUpCounters = new BackUpCounters() ;
	}

	@Override
	protected BackUpScannerResult doInBackground() throws Exception {
		// The worker itself is multi-threaded
		
		pLog.info("Scan triggered for " + jobsChoice.getTitleAsString()) ;
		backUpItemList.clear() ;
		
		backUpCounters.reset();
		
		filesVisitFailed = new ArrayList<Path>() ;	

		long 				  startTime   = System.currentTimeMillis() ;
		Path 				  sourcePath  = null ;
		ArrayList<BackUpTask> backUpTasks = jobsChoice.getTasks(jobTaskType) ;
		StringBuilder 		  jobProgress = new StringBuilder(1024) ;
	
		ArrayList<BackUpScannerTask> results = new ArrayList<BackUpScannerTask>() ;

		try {
			if (backUpTasks != null) {
				
				ExecutorService scannerExecutor = Config.getScanExecutorService() ;
								
				// Launch one thread per backUpTask
				for (BackUpTask backUpTask : backUpTasks) {

						BackUpScannerThread backUpScannerThread = new BackUpScannerThread(backUpTask, pLog) ;
						CompletableFuture<ScannerThreadResponse> backUpRes  = 
							CompletableFuture.supplyAsync(backUpScannerThread::scan, scannerExecutor) ;
						
						results.add(new BackUpScannerTask(backUpScannerThread, backUpRes)) ;
				}
				
				// Get results from backUpTask threads as they completes
				int nbActiveTasks = results.size() ;
				while (nbActiveTasks > 0) {
										
					jobProgress.setLength(0) ;
					jobProgress.append(HTML_BEGIN) ;
					for (BackUpScannerTask oneResult : results) {
						
						if ( (! oneResult.isResultRecorded()) &&
							 (oneResult.getFutureResponse().isDone())) {
							// one backUpTask has finished
								
							nbActiveTasks-- ;								
							oneResult.setResultRecorded(true) ;
							
							// publish task result
							publish(new BackupScannerInformation(null, oneResult.getFutureResponse().get())) ;

						}
						oneResult.getBackUpScannerThread().stopAsked(uiControl.isStopAsked());
						jobProgress.append(oneResult.getBackUpScannerThread().getCurrentStatus()).append("<br/>") ;
					}
					jobProgress.append(HTML_END) ;
					
					// Refresh progress information
					publish(new BackupScannerInformation(jobProgress.toString(), null)) ;

					if (nbActiveTasks > 0) {
						try {
							Thread.sleep(refreshRate) ;
						} catch (InterruptedException e) {
						}
					}
				}
			}					
		} catch (Exception e) {
			pLog.log(Level.SEVERE, "IOException when walking file tree " + sourcePath, e) ;
		}
		long duration = System.currentTimeMillis() - startTime ;
				
		return new BackUpScannerResult(results, duration);
	}
	
	@Override
	protected void process(java.util.List<BackupScannerInformation> chunks) {

		for (BackupScannerInformation scannerInfo : chunks) {
			
			ScannerThreadResponse scannerResp = scannerInfo.getScannerThreadResponse() ;
			if ((scannerResp != null) && (scannerResp.hasNotBeenProcessed())) {
				// One scanner thread has ended
				// It is necessary to test if the result has not been processed
				// because done() may have been called before
				processScannerThreadResponse(scannerResp) ;
			}
		}
		
		// Get the latest result from the list
		BackupScannerInformation latestResult = chunks.get(chunks.size() - 1);

		backUpTableModel.fireTableDataChanged();
		String lastInfo = latestResult.getInformation();
		if ((lastInfo != null) && (!lastInfo.isEmpty())) {
			long nbFilesProcessed = backUpCounters.nbSourceFilesProcessed + backUpCounters.nbTargetFilesProcessed ;
			progressPanel.setStepInfos(lastInfo, nbFilesProcessed);
		}
	}
	
	@Override
	protected void done() {

		try {
			BackUpScannerResult results = get();
			ArrayList<BackUpScannerTask> taskResults = results.getTaskResults() ;
			
			if ((taskResults == null) || (taskResults.isEmpty())) {
				pLog.warning("back up tasks is null") ;							
				progressPanel.setStepInfos(backUpCounters.toHtmlString(), 0);
				progressPanel.setProcessStatus("Aucune taches à effectuer");
			} else {
				
				// Get scanner results
				List<ScannerThreadResponse> scannerResults = taskResults.stream()
					.map(taskResult -> {
						try {
							return taskResult.getFutureResponse().get();
						} catch (InterruptedException | ExecutionException e) {
							pLog.log(Level.SEVERE, "Echec du processing des résultats de scanner", e) ;
							return null;
						}
					})
					.collect(Collectors.toList()) ;
				
				// Process the response that may have not been processed
				scannerResults.stream()
						.filter(scannerResp -> { return scannerResp.hasNotBeenProcessed();	})
						.forEach(scannerResp -> { processScannerThreadResponse(scannerResp) ; });
									
				// Check number of backup items
				int sumOfRes = scannerResults.stream()
					.mapToInt(backRes -> { return backRes.getBackUpItemList().size(); })
					.sum() ;
				if (sumOfRes != backUpItemList.size()) {
					pLog.severe("Erreur, nombre de résultats de scan recalculé =" + sumOfRes + " différent du nombre stocké =" + backUpItemList.size());
				}
				
				// Update progress info panel
				long nbFilesProcessed = backUpCounters.nbSourceFilesProcessed + backUpCounters.nbTargetFilesProcessed ;
				StringBuilder finalStatus = new StringBuilder(1024) ;
				finalStatus.append("Comparaison de fichiers terminée (") ;				
				finalStatus.append(jobTaskType.toString()) ;
				finalStatus.append(" - ") ;
				finalStatus.append(jobsChoice.getTitleAsString()) ;
				finalStatus.append(")") ;
				progressPanel.setStepInfos(backUpCounters.toHtmlString(), nbFilesProcessed);
				progressPanel.setProcessStatus(finalStatus.toString());
				
				long duration = results.getDuration() ;
				
				// Log info
				StringBuilder infoScanner = new StringBuilder(1024) ;		
				infoScanner.append(jobsChoice.getTitleAsString()).append("\n") ;
				for (BackUpScannerTask oneResult : taskResults) {
					infoScanner.append(oneResult.getBackUpScannerThread().getCurrentStatus()).append("\n") ;
				}
				pLog.info(getScanInfoText(infoScanner, duration)) ;
				
				// Update history tab
				StringBuilder scanInfo = new StringBuilder(1024) ;
				getScanInfoHtml(scanInfo, duration) ;
				String compareType = "Comparaison" ;
				if (jobsChoice.compareContent()) {
					compareType = compareType + " avec comparaison du contenu" ;
				}
				BackUpJobInformation jobInfo = new BackUpJobInformation( jobsChoice.getTitleAsHtml(), System.currentTimeMillis(), scanInfo.toString(), compareType, jobTaskType.toString()) ;
				backUpJobInfoTableModel.add(jobInfo) ;
			}
		} catch (InterruptedException | ExecutionException e) {
			pLog.log(Level.SEVERE, "Exception when getting FileBackupScanner result", e) ;
		} 

		backUpTableModel.fireTableDataChanged() ;
		backUpJobInfoTableModel.fireTableDataChanged() ;
		
		uiControl.setStopAsked(false) ;
		uiControl.setIsRunning(false) ;
	}
	  
	private void processScannerThreadResponse(ScannerThreadResponse scannerResp) {
		backUpCounters.add(scannerResp.getBackUpCounters());
		filesVisitFailed.addAll(scannerResp.getFilesVisitFailed()) ;
		backUpItemList.addAll(scannerResp.getBackUpItemList()) ;
		
		scannerResp.setHasNotBeenProcessed(false) ;
	}
	
	private static final String HTML_BEGIN = "<html><body>\n" ;
	private static final String HTML_END   = "</body></html>\n" ;

	 private void getScanInfoHtml(StringBuilder scanInfo, long duration) {
		 		
		 scanInfo.append(HTML_BEGIN) ;
		 backUpCounters.appendHtmlFragment(scanInfo) ;
		 scanInfo.append("<p>Durée scan (ms)= ").append(duration) ;
		 if ((filesVisitFailed != null) && (! filesVisitFailed.isEmpty())) {
			 scanInfo.append("<br>Fichiers visités en erreur:") ;
			 for (Path fileOnError : filesVisitFailed) {
				 scanInfo.append("<br>").append(fileOnError) ;
			 }
		 }
		 scanInfo.append(HTML_END) ;
	 }
	 
	 private String getScanInfoText(StringBuilder scanInfo, long duration) {

		 scanInfo.append(backUpCounters.toString()).append("\nScan duration (ms)= ").append(duration) ;
		 if ((filesVisitFailed != null) && (! filesVisitFailed.isEmpty())) {
			 scanInfo.append("\nFichiers visités en erreur:") ;
			 for (Path fileOnError : filesVisitFailed) {
				 scanInfo.append("\n").append(fileOnError) ;
			 }
		 }
		 return scanInfo.toString() ;
	 }

	protected BackUpItemList getBackUpItemList() {
		return backUpItemList;
	}

	protected BackUpCounters getBackUpCounters() {
		return backUpCounters;
	}	 
}
