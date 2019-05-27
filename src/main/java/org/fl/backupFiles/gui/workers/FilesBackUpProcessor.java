package org.fl.backupFiles.gui.workers;

import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.fl.backupFiles.BackUpCounters;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpJobInformation;
import org.fl.backupFiles.BackupFilesInformation;
import org.fl.backupFiles.Config;
import org.fl.backupFiles.JobsChoice;
import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.fl.backupFiles.gui.BackUpJobInfoTableModel;
import org.fl.backupFiles.gui.BackUpTableModel;
import org.fl.backupFiles.gui.ProgressInformationPanel;
import org.fl.backupFiles.gui.UiControl;

public class FilesBackUpProcessor extends SwingWorker<String,BackupFilesInformation>  {

	private Logger pLog ;
	
	private final UiControl uiControl ;
	
	private final BackUpTableModel backUpTableModel ;
	private BackUpItemList 	 backUpItemList ;
	private BackUpJobInfoTableModel backUpJobInfoTableModel ;
	
	private ProgressInformationPanel progressPanel;
	
	private final JobsChoice  jobsChoice ;
	private final JobTaskType jobTaskType ;

	private final int  refreshRate ;
	private final long maxRefreshInterval ;
	
	private BackUpCounters backUpCounters ;
	
	private final static String SAUVEGARDE_EN_COURS = "Sauvegarde de fichiers en cours" ;
	private final static String NB_ELEM 			= "Nombre d'éléments restant à traiter: "  ;
	private final static String PROCESSED_ELEM 		= "\nEléments déjà traités : \n"  ;
	
	public FilesBackUpProcessor(UiControl u,  JobTaskType jtt, JobsChoice jc, BackUpTableModel b, ProgressInformationPanel pip, BackUpJobInfoTableModel bj, Logger l) {

		super();
		pLog 			   = l ;
		uiControl		   = u ;
		backUpTableModel   = b ;		
		refreshRate 	   = Config.getBackUpRefreshRate() ;
		maxRefreshInterval = Config.getBackUpMaxRefreshInterval() ;
		progressPanel	   = pip ;
		
		// back up items
		backUpItemList = backUpTableModel.getBackUpItems() ;

		jobsChoice 	= jc ;
		jobTaskType	= jtt ;

		backUpJobInfoTableModel = bj ;
		
		backUpCounters = new BackUpCounters() ;
	}

	@Override
	protected String doInBackground() throws Exception {
		
		long startTime = System.currentTimeMillis() ;
		pLog.info("Back up triggered for " + jobsChoice.getTitleAsString()) ;

		jobsChoice.initTargetFileStores(jobTaskType) ;
		backUpCounters.reset() ;
		int idx = 0 ;
		long lastRefreshTime = System.currentTimeMillis() ;
		StringBuilder status = new StringBuilder(1024) ;
		while ((idx < backUpItemList.size() ) && (! uiControl.isStopAsked())) {
			if (((idx % refreshRate) == 0) || (System.currentTimeMillis() - lastRefreshTime > maxRefreshInterval)) {
				status.setLength(0) ;
				status.append(NB_ELEM).append(backUpItemList.size() - idx) ;
				status.append(PROCESSED_ELEM).append( backUpCounters.toString()) ;
				publish(new BackupFilesInformation(SAUVEGARDE_EN_COURS,status.toString(), idx)) ;
				lastRefreshTime = System.currentTimeMillis() ;
			}
			backUpItemList.get(idx).execute(backUpCounters);			
			idx++ ;
		} 
		publish(new BackupFilesInformation("Sauvegarde de fichiers terminée (" + jobTaskType.toString() + " - " + jobsChoice.getTitleAsString() + ")", backUpCounters.toString(), backUpCounters.nbSourceFilesProcessed + backUpCounters.nbTargetFilesProcessed)) ;
		
		long endTime = System.currentTimeMillis() ;
		long duration = endTime - startTime ;
		
		pLog.info(getProcessorInfoText(duration)) ;
		
		String scanResult = getProcessorInfoHtml(duration) ;
		BackUpJobInformation jobInfo = new BackUpJobInformation( jobsChoice.getTitleAsHtml(), endTime, scanResult, "Sauvegarde", jobTaskType.toString()) ;
		backUpJobInfoTableModel.add(jobInfo) ;
		
		uiControl.setIsRunning(false) ;
		uiControl.setStopAsked(false) ;
		return null;
	}
	
	 @Override
     protected void process(java.util.List<BackupFilesInformation> chunks) {
		 
		 // Get the latest result from the list
		 BackupFilesInformation latestResult = chunks.get(chunks.size() - 1);

		 backUpTableModel.fireTableDataChanged() ;

		 progressPanel.setStepInfos(latestResult.getInformation(), latestResult.getNbFilesProcessed());
		 progressPanel.setProcessStatus(latestResult.getStatus());
       
     }
	 
	 @Override
	 protected void done() {
		 
		 backUpItemList.clearDone() ;
		 backUpTableModel.fireTableDataChanged() ;
		 backUpJobInfoTableModel.fireTableDataChanged() ;
	 }
	 
	 private String getProcessorInfoHtml(long duration) {
		 
		 StringBuilder procInfo = new StringBuilder(1024) ;
		 procInfo.append("<html><body>") ;
		 backUpCounters.appendHtmlFragment(procInfo) ;
		 procInfo.append("<p>Durée sauvegarde (ms)= ").append(duration) ;
		 procInfo.append(jobsChoice.getTargetRemainigSpace(true)) ;
		 procInfo.append("</body></html>") ;
		 return procInfo.toString() ;
	 }
	 
	 private String getProcessorInfoText(long duration) {
		 
		 StringBuilder procInfo = new StringBuilder() ;
		 procInfo.append(jobsChoice.getTitleAsString()).append(jobTaskType.toString()).append("\n") ;
		 procInfo.append(backUpCounters.toString()).append("\nProcess duration (ms)= ").append(duration).append("\n") ;
		 procInfo.append(jobsChoice.getTargetRemainigSpace(false)) ;
		 return procInfo.toString() ;
	 }
}
