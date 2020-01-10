package org.fl.backupFiles.gui;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URI;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

import org.fl.backupFiles.Config;
import org.fl.backupFiles.BackUpJob.JobTaskType;

import com.ibm.lge.fl.util.AdvancedProperties;
import com.ibm.lge.fl.util.RunningContext;
import com.ibm.lge.fl.util.swing.ApplicationTabbedPane;

// Main class for the back up files application
public class BackupFilesGui  extends JFrame {

	private static final String DEFAULT_PROP_FILE = "file:///FredericPersonnel/PortableApps/BackUpFiles/backupFiles.properties";

	private static final long serialVersionUID = -2691160306708075667L;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BackupFilesGui window = new BackupFilesGui(DEFAULT_PROP_FILE);
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private Logger bLog ;
	
	public BackupFilesGui(String propertiesUri) {

		RunningContext runningContext = null ;
		Path 		   configFileDir  = null ;
		try {
			// Get context, properties, logger
			runningContext = new RunningContext("BackupFiles", null, new URI(propertiesUri));
			bLog = runningContext.getpLog() ;
			AdvancedProperties backupProperty = runningContext.getProps() ;
			
			// Get the different config files
			configFileDir = backupProperty.getPathFromURI("backupFiles.configFileDir") ;
			
			// Init config
			Config.initConfig(backupProperty, bLog) ;
			
		} catch (Exception e) {
			System.out.println("Exception caught in Main (see default prop file processing)") ;
			e.printStackTrace() ;
		}
		
		if ((runningContext != null) && (configFileDir != null)) {
		// Display GUI
			
			setBounds(10, 10, 1880, 1000);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setTitle("Sauvegarde de fichiers") ;
			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));		
					
			BackUpJobInfoTableModel jobInformationTable = new BackUpJobInfoTableModel() ;
			// Tabbed Panel for configuration, tables and controls, and history
			ApplicationTabbedPane bkpTablesPanel = new ApplicationTabbedPane(runningContext) ;
				
			ArrayList<BackUpPane> backUpPanes = new ArrayList<BackUpPane>() ;
			int tabIndex = 0 ;
			for (JobTaskType jtt : JobTaskType.values()) {
				BackUpPane taskTypePane = new BackUpPane(jtt, jobInformationTable, bLog) ;
				backUpPanes.add(taskTypePane) ;
				bkpTablesPanel.add(taskTypePane, jtt.toString(), tabIndex) ;
				tabIndex++ ;
			}
						
			//  Tabbed Panel to choose back up configuration. Add it in the first position
			BackUpConfigChoicePane configChoicePane = new BackUpConfigChoicePane(configFileDir, backUpPanes, bLog) ;
			bkpTablesPanel.add(configChoicePane, "Configuration", 0) ;
	
			// Tabbed Panel to display a summary of operations done. Add it before the standard tabs provided by ApplicationTabbedPane
			BackUpJobInfoPanel historiqueTab = new BackUpJobInfoPanel(jobInformationTable) ;
			bkpTablesPanel.add(historiqueTab, "Historique", tabIndex+1);
			
			bkpTablesPanel.setSelectedIndex(0) ;
			
			getContentPane().add(bkpTablesPanel) ;
			
			addWindowListener(new ShutdownAppli()) ;
		}
	}
	
	private class ShutdownAppli extends WindowAdapter {
		
        @Override
        public void windowClosing(WindowEvent e)
        {
        	ExecutorService backUpExecutor = Config.getScanExecutorService() ;
        	
        	backUpExecutor.shutdown();
        	
        	try {
    			// Wait a while for existing tasks to terminate
        		
        		if (! backUpExecutor.awaitTermination(5,TimeUnit.SECONDS)) {
    				// Cancel currently executing tasks
        			backUpExecutor.shutdownNow() ;
        		} else {
        			bLog.fine("shutdown normal") ;
        		}
        		if (! backUpExecutor.awaitTermination(5,TimeUnit.SECONDS)) {
        			bLog.severe("ExecutorService not able to shutdown");
        		}
    		} catch (InterruptedException ie) {
    			// (Re-)Cancel if current thread also interrupted
    			backUpExecutor.shutdownNow();

    			// Preserve interrupt status
    			Thread.currentThread().interrupt();
    		}
        }
	}
}
