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

package org.fl.backupFiles.gui;

import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BoxLayout;
import javax.swing.JFrame;

import org.fl.backupFiles.Config;
import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.fl.util.swing.ApplicationTabbedPane;

// Main class for the back up files application
public class BackupFilesGui  extends JFrame {
	
	private static final long serialVersionUID = -2691160306708075667L;

	private static final String DEFAULT_PROP_FILE = "file:///FredericPersonnel/Program/PortableApps/BackUpFiles/backupFiles.properties";
	
	private static final Logger bLog = Logger.getLogger(BackupFilesGui.class.getName());

	public static final int WINDOW_WIDTH = 1880;
	public static final int WINDOW_HEIGHT = 1000;
	
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {

				try {
					
					// Init config
					Config.initConfig(getPropertyFile());
					
					try {
						BackupFilesGui window = new BackupFilesGui();
						window.setVisible(true);
					} catch (Exception e) {
						bLog.log(Level.SEVERE, "Exception in main", e);
					}
					
				} catch (Exception e) {
					bLog.log(Level.SEVERE, "Exception caught in Main (see default prop file processing) ", e);
					System.out.println("Exception caught in Main (see default prop file processing) " + e.getMessage());
					e.printStackTrace();
				}				
			}
		});
	}
	
	public static String getPropertyFile() {
		return DEFAULT_PROP_FILE;
	}
	
	private BackupFilesGui() {

		Path configFileDir = Config.getConfigFileDir();
		if (configFileDir != null) {
		// Display GUI
			
			setBounds(10, 10, WINDOW_WIDTH, WINDOW_HEIGHT);
			setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			setTitle("Sauvegarde de fichiers") ;
			getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));		
					
			BackUpJobInfoTableModel jobInformationTable = new BackUpJobInfoTableModel() ;
			// Tabbed Panel for configuration, tables and controls, and history
			ApplicationTabbedPane bkpTablesPanel = new ApplicationTabbedPane(Config.getRunningContext()) ;
				
			ArrayList<BackUpPane> backUpPanes = new ArrayList<BackUpPane>() ;
			int tabIndex = 0;
			for (JobTaskType jtt : JobTaskType.values()) {
				BackUpPane taskTypePane = new BackUpPane(jtt, jobInformationTable);
				backUpPanes.add(taskTypePane) ;
				bkpTablesPanel.add(taskTypePane, jtt.toString(), tabIndex);
				tabIndex++;
			}
						
			//  Tabbed Panel to choose back up configuration. Add it in the first position
			BackUpConfigChoicePane configChoicePane = new BackUpConfigChoicePane(configFileDir, backUpPanes) ;
			bkpTablesPanel.add(configChoicePane, "Configuration", 0);
	
			// Tabbed Panel to display a summary of operations done. Add it before the standard tabs provided by ApplicationTabbedPane
			BackUpJobInfoPanel historiqueTab = new BackUpJobInfoPanel(jobInformationTable);
			bkpTablesPanel.add(historiqueTab, "Historique", tabIndex+1);
			
			bkpTablesPanel.setSelectedIndex(0);
			
			getContentPane().add(bkpTablesPanel);
			
			addWindowListener(new ShutdownAppli());
		} else {
			bLog.severe("Config files directory is null");
		}
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
