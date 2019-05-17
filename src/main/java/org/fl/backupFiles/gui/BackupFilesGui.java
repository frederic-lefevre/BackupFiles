package org.fl.backupFiles.gui;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JTabbedPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.fl.backupFiles.Config;
import org.fl.backupFiles.BackUpJob.JobTaskType;

import com.ibm.lge.fl.util.AdvancedProperties;
import com.ibm.lge.fl.util.RunningContext;

// Main class for the back up files application
public class BackupFilesGui  extends JFrame {

	private static final String DEFAULT_PROP_FILE = "backupFiles.properties";
	
	private static final long serialVersionUID = -2691160306708075667L;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BackupFilesGui window = new BackupFilesGui();
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	private Logger bLog ;
	
	private JTabbedPane bkpTablesPanel ;
	
	private ApplicationInfoPane appInfoPane ;
	
	public BackupFilesGui() {

		// Get context, properties, logger
		RunningContext runningContext = new RunningContext("BackupFiles", null, DEFAULT_PROP_FILE);
		bLog = runningContext.getpLog() ;
		AdvancedProperties backupProperty = runningContext.getProps() ;
		
		// Get the different config files
		Path configFileDir = backupProperty.getPathFromURI("backupFiles.configFileDir") ;
		
		// Init config
		Config.initConfig(backupProperty) ;
		
		// Display GUI
		setBounds(10, 10, 1880, 1000);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Sauvegarde de fichiers") ;
		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));		
				
		BackUpJobInfoTableModel jobInformationTable = new BackUpJobInfoTableModel() ;
		// Tabbed Panel for configuration, tables and controls, and history
		bkpTablesPanel = new JTabbedPane() ;
			
		ArrayList<BackUpPane> backUpPanes = new ArrayList<BackUpPane>() ;
		for (JobTaskType jtt : JobTaskType.values()) {
			BackUpPane taskTypePane = new BackUpPane(jtt, jobInformationTable, bLog) ;
			backUpPanes.add(taskTypePane) ;
			bkpTablesPanel.add(jtt.toString(), taskTypePane) ;
		}
		
		BackUpConfigChoicePane configChoicePane = new BackUpConfigChoicePane(configFileDir, backUpPanes, bLog) ;	
		
		//  Tabbed Panel to choose back up configuration
		bkpTablesPanel.add(configChoicePane, "Configuration", 0) ;

		// Tabbed Panel to display a summary of oprations done
		BackUpJobInfoPanel historiqueTab = new BackUpJobInfoPanel(jobInformationTable) ;
		bkpTablesPanel.add("Historique", historiqueTab);
		
		// Tabbed Panel for application information
		appInfoPane = new ApplicationInfoPane(runningContext) ;
		bkpTablesPanel.add("Informations", appInfoPane) ;
		
		bkpTablesPanel.setSelectedIndex(0) ;
		
		bkpTablesPanel.addChangeListener(new BackUpTabChangeListener());
		
		getContentPane().add(bkpTablesPanel) ;
		
		addWindowListener(new ShutdownAppli()) ;
		
	}
	
	private class BackUpTabChangeListener implements ChangeListener {

		@Override
		public void stateChanged(ChangeEvent arg0) {
			
			if (bkpTablesPanel.getSelectedComponent().equals(appInfoPane)) {
				appInfoPane.setInfos();
			}			
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
