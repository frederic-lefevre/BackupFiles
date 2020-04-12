package org.fl.backupFiles.gui.workers;

import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingWorker;

import org.fl.backupFiles.BackUpCounters;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpJobInformation;
import org.fl.backupFiles.Config;
import org.fl.backupFiles.JobsChoice;
import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.fl.backupFiles.gui.BackUpJobInfoTableModel;
import org.fl.backupFiles.gui.BackUpTableModel;
import org.fl.backupFiles.gui.ProgressInformationPanel;
import org.fl.backupFiles.gui.UiControl;

public class FilesBackUpProcessor extends SwingWorker<BackUpProcessorResult,BackupFilesInformation>  {

	private final Logger pLog ;
	
	private final UiControl uiControl ;
	
	private final BackUpTableModel 		  backUpTableModel ;
	private final BackUpItemList 	 	  backUpItemList ;
	private final BackUpJobInfoTableModel backUpJobInfoTableModel ;
	
	private final ProgressInformationPanel progressPanel;
	
	private final JobsChoice  jobsChoice ;
	private final JobTaskType jobTaskType ;

	private final int  refreshRate ;
	private final long maxRefreshInterval ;
	
	private final BackUpCounters backUpCounters ;
	
	private final static String NB_ELEM 		= "Nombre d'éléments restant à traiter: "  ;
	private final static String PROCESSED_ELEM 	= "<br/>Eléments déjà traités : "  ;
		
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
	protected BackUpProcessorResult doInBackground() throws Exception {
		
		long startTime = System.currentTimeMillis() ;
		pLog.info("Back up triggered for " + jobsChoice.getTitleAsString()) ;

		boolean backupSuccess = true;
		jobsChoice.initTargetFileStores(jobTaskType) ;
		backUpCounters.reset() ;
		int idx = 0 ;
		long lastRefreshTime = System.currentTimeMillis() ;
		StringBuilder infos = new StringBuilder(1024) ;
		while ((idx < backUpItemList.size() ) && (! uiControl.isStopAsked())) {
			if (((idx % refreshRate) == 0) || (System.currentTimeMillis() - lastRefreshTime > maxRefreshInterval)) {
				infos.setLength(0) ;
				infos.append(HTML_BEGIN) ;
				infos.append(NB_ELEM).append(backUpItemList.size() - idx) ;
				infos.append(PROCESSED_ELEM) ;
				backUpCounters.appendHtmlFragment(infos) ;
				infos.append(HTML_END) ;
				publish(new BackupFilesInformation( infos.toString(), idx)) ;
				lastRefreshTime = System.currentTimeMillis() ;
			}
			backupSuccess &= backUpItemList.get(idx).execute(backUpCounters) ;			
			idx++ ;
		}
		
		long duration = System.currentTimeMillis() - startTime ;				
		return new BackUpProcessorResult(duration);
	}
	
	 @Override
     protected void process(java.util.List<BackupFilesInformation> chunks) {
		 
		 // Get the latest result from the list
		 BackupFilesInformation latestResult = chunks.get(chunks.size() - 1);

		 backUpTableModel.fireTableDataChanged() ;

		 progressPanel.setStepInfos(latestResult.getInformation(), latestResult.getNbFilesProcessed());       
     }
	 
	@Override
	protected void done() {

		try {
			BackUpProcessorResult result = get();
		
			// Update progress info panel
			StringBuilder finalStatus = new StringBuilder(256) ;
			finalStatus.append("Sauvegarde de fichiers terminée (") ;
			finalStatus.append(jobTaskType.toString()) ;
			finalStatus.append(" - ") ;
			finalStatus.append(jobsChoice.getTitleAsString()) ;
			finalStatus.append(")") ;
			long nbFilesProcessed = backUpCounters.nbSourceFilesProcessed + backUpCounters.nbTargetFilesProcessed ;
			progressPanel.setStepInfos(backUpCounters.toHtmlString(), nbFilesProcessed);
			progressPanel.setProcessStatus(finalStatus.toString());

			// Log info
			pLog.info(getProcessorInfoText(result.getDuration())) ;
		
			// Update history tab
			String scanResult = getProcessorInfoHtml(result.getDuration()) ;
			BackUpJobInformation jobInfo = new BackUpJobInformation( jobsChoice.getTitleAsHtml(), System.currentTimeMillis(), scanResult, "Sauvegarde", jobTaskType.toString()) ;
			backUpJobInfoTableModel.add(jobInfo) ;
			
			backUpItemList.removeItemsDone();
			
		} catch (InterruptedException | ExecutionException e) {
			pLog.log(Level.SEVERE, "Exception when getting FileBackupProcessor result", e) ;
		}
		
		backUpTableModel.fireTableDataChanged();
		backUpJobInfoTableModel.fireTableDataChanged();

		uiControl.setIsRunning(false);
		uiControl.setStopAsked(false);
	}
	 
	private static final String HTML_BEGIN = "<html><body>" ;
	private static final String HTML_END   = "</body></html>" ;

	 private String getProcessorInfoHtml(long duration) {
		 
		 StringBuilder procInfo = new StringBuilder(1024) ;
		 procInfo.append(HTML_BEGIN) ;
		 backUpCounters.appendHtmlFragment(procInfo) ;
		 procInfo.append("<p>Durée sauvegarde (ms)= ").append(duration) ;
		 procInfo.append(jobsChoice.getTargetRemainigSpace(true)) ;
		 procInfo.append(HTML_END) ;
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
