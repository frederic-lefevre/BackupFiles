/*
 * MIT License

Copyright (c) 2017, 2023 Frederic Lefevre

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

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.fl.backupFiles.BackUpTask;
import org.fl.backupFiles.Config;
import org.fl.backupFiles.JobsChoice;
import org.fl.backupFiles.gui.workers.FilesBackUpProcessor;
import org.fl.backupFiles.gui.workers.FilesBackUpScanner;

public class UiControl extends JPanel {

	private static final Logger bLog = Config.getLogger();
	
	private static final long serialVersionUID = 1L;
	

	private final JButton scanButton;
	private final JButton bckpUpButton;
	private final JButton stopButton;

	private final JCheckBox compareContentSelect;
	private final JCheckBox compareContentOnAmbiguousSelect;
	
	private boolean isRunning ;
	private boolean stopAsked ;
	
	private final ProgressInformationPanel progressPanel;
	private final JobTaskType 			   jobTaskType;
	
	private JobsChoice				 	   jobsChoice;
	
	private final BackUpTableModel 		  backUpTableModel ;
	private final BackUpJobInfoTableModel backUpJobInfoTableModel;
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public void setIsRunning(boolean b) {
		isRunning = b ;
		if (isRunning) {
			bckpUpButton.setBackground(Color.ORANGE);
			scanButton.setBackground(Color.ORANGE);
		} else {
			bckpUpButton.setBackground(Color.GREEN);
			scanButton.setBackground(Color.GREEN);
		}
		bckpUpButton.setEnabled(!isRunning);
		scanButton.setEnabled(!isRunning);
		stopButton.setEnabled(isRunning);
	}
	
	public boolean isStopAsked() {
		return stopAsked;
	}

	public void setStopAsked(boolean stopAsked) {
		this.stopAsked = stopAsked;
	}

	public UiControl(JobTaskType jtt, BackUpTableModel b, ProgressInformationPanel pip, BackUpJobInfoTableModel bj) {
		
		super() ;
		isRunning 	  	  = false ;
		stopAsked 	  	  = false ;
		jobsChoice   	  = null ;
		jobTaskType		  = jtt ;
		progressPanel 	  = pip ;
		
		backUpTableModel  = b ;
		
		backUpJobInfoTableModel = bj ;
		
		int width = BackupFilesGui.WINDOW_WIDTH - 20 ;
		setMaximumSize(new Dimension(width, 100)) ;
		setBorder(BorderFactory.createMatteBorder(10,10,10,10,Color.WHITE)) ;
		
		GridBagLayout gLayout = new GridBagLayout() ;
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0,100,0,100) ;
		setLayout(gLayout);
		
		// Compare content check box
		compareContentSelect = new JCheckBox("Compare files content") ;
		c.gridx = 0;
		c.gridy = 0;
		add(compareContentSelect, c);
		
		// Compare content check box
		compareContentOnAmbiguousSelect = new JCheckBox("Compare files content on ambiguous files", true) ;
		c.gridx = 0;
		c.gridy = 1;
		add(compareContentOnAmbiguousSelect, c);
		
		// Scan and Back up button
		bckpUpButton = new JButton("Sauvegarde") ;
		scanButton   = new JButton("Comparaison") ;
		stopButton	 = new JButton("Stop") ;
		Font buttonFont = new Font("Verdana", Font.BOLD, 24);
		bckpUpButton.setFont(buttonFont) ;
		scanButton.setFont(buttonFont) ;
		stopButton.setFont(buttonFont) ;
		setButtonForEmpyTasks();
		
		ControlAction controlAction = new ControlAction(this);
		bckpUpButton.addActionListener(controlAction);
		scanButton.addActionListener(controlAction);
		stopButton.addActionListener(controlAction);
		
		c.gridx = 0;
		c.gridy = 2;
		c.ipadx = 200;
		add(scanButton, c);
		c.gridx = 1;
		c.ipadx = 200;
		add(stopButton, c);
		c.gridx = 3;
		c.ipadx = 200;
		add(bckpUpButton, c);
	}

	public void setJobChoice(JobsChoice jc) {
		
		List<BackUpTask> backupTaks = jc.getTasks(jobTaskType);
		if ((backupTaks == null) || (backupTaks.isEmpty())) {
			setButtonForEmpyTasks();
		} else {
			setIsRunning(false);
		}
		jobsChoice = jc;
		backUpTableModel.getBackUpItems().clear();
	}

	private void setButtonForEmpyTasks() {
		bckpUpButton.setBackground(Color.GREEN);
		bckpUpButton.setEnabled(false);
		scanButton.setBackground(Color.GREEN);
		scanButton.setEnabled(false);
		stopButton.setBackground(Color.RED);
		stopButton.setEnabled(false);
	}
	
	private class ControlAction implements ActionListener {

		private UiControl uiControl;
		
		public ControlAction(UiControl u) {
			uiControl = u;
		}
		
		private final static String COMPARAISON_EN_COURS = "Comparaison de fichiers en cours";
		private final static String SAUVEGARDE_EN_COURS  = "Sauvegarde de fichiers en cours";
		
		@Override
		public void actionPerformed(ActionEvent ae) {
			
			if (ae.getSource() == bckpUpButton) {
				bLog.fine("Back up action launched");
				uiControl.setIsRunning(true);
				
				FilesBackUpProcessor fProcess = new FilesBackUpProcessor(uiControl, jobTaskType, jobsChoice, backUpTableModel, progressPanel, backUpJobInfoTableModel);
				progressPanel.setProcessStatus(SAUVEGARDE_EN_COURS) ;
				fProcess.execute() ;
				
			} else if (ae.getSource() == scanButton) {				
				bLog.fine("Scan action launched");
				uiControl.setIsRunning(true);
				
				jobsChoice.setCompareContent(jobTaskType, compareContentSelect.isSelected());
				jobsChoice.setCompareContentOnAmbiguous(jobTaskType, compareContentOnAmbiguousSelect.isSelected());
				
				FilesBackUpScanner fScan = new FilesBackUpScanner(uiControl, jobTaskType, jobsChoice, backUpTableModel, progressPanel, backUpJobInfoTableModel);				
				progressPanel.setProcessStatus(COMPARAISON_EN_COURS);
				fScan.execute();
			}  else if (ae.getSource() == stopButton) {	
			 	stopAsked = true;
			}
		
		}
		
	}
	

}
