/*
 * MIT License

Copyright (c) 2017, 2026 Frederic Lefevre

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

package org.fl.backupFiles.gui;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import org.fl.backupFiles.BackUpJobList;
import org.fl.backupFiles.Config;
import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.fl.util.RunningContext;
import org.fl.util.swing.ApplicationTabbedPane;

// Main class for the back up files application
public class BackupFilesGui extends JFrame {
	
	private static final long serialVersionUID = -2691160306708075667L;

	private static final String DEFAULT_PROP_FILE = "file:///FredericPersonnel/Program/PortableApps/BackUpFiles/backupFiles.properties";
	
	private static final Logger bLog = Logger.getLogger(BackupFilesGui.class.getName());

	public static final int WINDOW_WIDTH = 1880;
	public static final int WINDOW_HEIGHT = 1000;
	
	private static RunningContext runningContext;
	
	private static String PROPERTY_FILE_ARG_PREFIX = "-props=";
	
	private static String propertyFileUriString = DEFAULT_PROP_FILE;
	
	public static void main(String[] args) {
		
		String propertyFileArgument = RunningContext.getProgramArgWithPrefix(PROPERTY_FILE_ARG_PREFIX, args);
		if (propertyFileArgument != null) {
			propertyFileUriString = propertyFileArgument;
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					BackupFilesGui window = new BackupFilesGui();
					window.setVisible(true);
				} catch (Exception e) {
					bLog.log(Level.SEVERE, "Exception in main", e);
				}
			}
		});
	}
	
	public static RunningContext getRunningContext() {
		if (runningContext == null) {
			runningContext = new RunningContext("org.fl.backupFiles", getPropertyFile());
		}
		return runningContext;
	}
	
	private static String getPropertyFile() {
		return propertyFileUriString;
	}
	
	private BackupFilesGui() {
		
		// Tabbed Panel for configuration, tables and controls, and history
		ApplicationTabbedPane mainApplicationTabbedPanel = new ApplicationTabbedPane(Config.getRunningContext());
		
		setBounds(10, 10, WINDOW_WIDTH, WINDOW_HEIGHT);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setTitle("Sauvegarde de fichiers") ;
		
		try {
			Path configFileDir = Config.getConfigFileDir();

			if (configFileDir != null) {
				// Display GUI

				BackUpJobInfoTableModel jobInformationTable = new BackUpJobInfoTableModel();

				// List of all possible back up jobs
				// The back up jobs are defined in JSON files (one file per back up job)
				// The first user action is to choose the back up job to execute
				BackUpJobList backUpJobs = new BackUpJobList(configFileDir);
				Set<JobTaskType> jobTaskTypes = backUpJobs.getJobTaskTypes();

				ArrayList<BackUpPane> backUpPanes = new ArrayList<BackUpPane>();
				int tabIndex = 0;
				for (JobTaskType jtt : JobTaskType.values()) {
					if (jobTaskTypes.contains(jtt)) {
						BackUpPane taskTypePane = new BackUpPane(jtt, jobInformationTable);
						backUpPanes.add(taskTypePane);
						mainApplicationTabbedPanel.add(taskTypePane, jtt.toString(), tabIndex++);
					}
				}

				//  Tabbed Panel to choose back up configuration. Add it in the first position
				BackUpConfigChoicePane configChoicePane = new BackUpConfigChoicePane(backUpJobs, backUpPanes) ;
				mainApplicationTabbedPanel.add(configChoicePane, "Configuration", 0);
				tabIndex++;

				// Tabbed Panel to display a summary of operations done. Add it before the standard tabs provided by ApplicationTabbedPane
				BackUpJobInfoPanel historiqueTab = new BackUpJobInfoPanel(jobInformationTable);
				mainApplicationTabbedPanel.add(historiqueTab, "Historique", tabIndex++);

				mainApplicationTabbedPanel.add(new CaptionPane(), "Légende", tabIndex++);

				mainApplicationTabbedPanel.setSelectedIndex(0);

			} else {
				bLog.severe("Config files directory is null. Backup property file: " + Objects.toString(Config.getRunningContext().getPropertiesLocation()));
			}
		} catch (Exception e) {
			bLog.log(Level.SEVERE, "Exception in application startup", e);
		}
		
		getContentPane().add(mainApplicationTabbedPanel);
		addWindowListener(new ShutdownAppli());
	}
	
	private class ShutdownAppli extends WindowAdapter {
		
        @Override
        public void windowClosing(WindowEvent e)
        {
        	terminateExecutor(Config.getScanExecutorService(), "executor for scan");
        	terminateExecutor(Config.getScheduler(), "scheduled executor for information refresh");
        }
        
        private void terminateExecutor(ExecutorService execSvc, String executorType) {

    		execSvc.shutdown();
    		try {
    			// Wait a while for existing tasks to terminate
    			if (! execSvc.awaitTermination(10, TimeUnit.SECONDS)) {
    				// Cancel currently executing tasks
    				execSvc.shutdownNow();
    				bLog.fine("shutdown NOW " + executorType);
    			} else {
        			bLog.fine("shutdown normal " + executorType);
        		}
    			
    			if (! execSvc.awaitTermination(5, TimeUnit.SECONDS)) {
    				bLog.severe(executorType + " not terminated " + execSvc.isTerminated());
    			} else {
    				bLog.fine("shutdown ok " + executorType);
    			}
    		} catch (InterruptedException ie) {
    			// (Re-)Cancel if current thread also interrupted
    			bLog.warning("Interrupted exception during threads shutdown " + executorType);
    			execSvc.shutdownNow();
    			// Preserve interrupt status
    			Thread.currentThread().interrupt();
    		}
    	}
	}
}
