package org.fl.backupFiles.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.fl.backupFiles.JobsChoice;
import org.fl.backupFiles.gui.workers.FilesBackUpProcessor;
import org.fl.backupFiles.gui.workers.FilesBackUpScanner;

public class UiControl extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JButton scanButton ;
	private JButton bckpUpButton ;
	private JButton stopButton ;

	private JCheckBox compareContentSelect ;
	
	private boolean isRunning ;
	private boolean stopAsked ;
	private Logger 	bLog ;
	
	private ProgressInformationPanel progressPanel;
	private JobsChoice				 jobsChoice ;
	private JobTaskType 			 jobTaskType ;
	
	private BackUpTableModel backUpTableModel  ;
	private BackUpJobInfoTableModel backUpJobInfoTableModel ;
	
	public boolean isRunning() {
		return isRunning;
	}
	
	public void setIsRunning(boolean b) {
		isRunning = b ;
		if (isRunning) {
			bckpUpButton.setBackground(Color.ORANGE) ;
			scanButton.setBackground(Color.ORANGE) ;
		} else {
			bckpUpButton.setBackground(Color.GREEN) ;
			scanButton.setBackground(Color.GREEN) ;
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

	public UiControl(JobTaskType jtt, BackUpTableModel b, ProgressInformationPanel pip, BackUpJobInfoTableModel bj, Logger l) {
		
		super() ;
		isRunning 	  	  = false ;
		stopAsked 	  	  = false ;
		bLog 		  	  = l ;
		jobsChoice   	  = null ;
		jobTaskType		  = jtt ;
		progressPanel 	  = pip ;
		
		backUpTableModel  = b ;
		
		backUpJobInfoTableModel = bj ;
		
		GridLayout gLayout = new GridLayout(2, 7) ;
		setLayout(gLayout);
		
		// Compare content check box
		compareContentSelect = new JCheckBox("Compare files content") ;	
		add(getEmptyPane()) ;
		add(compareContentSelect) ;
		add(getEmptyPane()) ;
		add(getEmptyPane()) ;
		add(getEmptyPane()) ;
		add(getEmptyPane()) ;
		add(getEmptyPane()) ;
		
		// Scan and Back up button		
		bckpUpButton = new JButton("Sauvegarde") ;
		scanButton   = new JButton("Comparaison") ;
		stopButton	 = new JButton("Stop") ;
		Font buttonFont = new Font("Verdana", Font.BOLD, 18);
		bckpUpButton.setFont(buttonFont) ;
		bckpUpButton.setBackground(Color.GREEN) ;
		bckpUpButton.setEnabled(false) ;
		scanButton.setFont(buttonFont) ;
		scanButton.setBackground(Color.GREEN) ;
		scanButton.setEnabled(false) ;
		stopButton.setFont(buttonFont) ;
		stopButton.setBackground(Color.RED) ;
		stopButton.setEnabled(false) ;
		
		ControlAction controlAction = new ControlAction(this) ;
		bckpUpButton.addActionListener(controlAction);
		scanButton.addActionListener(controlAction);
		stopButton.addActionListener(controlAction);
		
		add(getEmptyPane()) ;
		add(scanButton) ;
		add(getEmptyPane()) ;
		add(stopButton) ;
		add(getEmptyPane()) ;
		add(bckpUpButton) ;
		add(getEmptyPane()) ;
		
	}

	public void setJobChoice(JobsChoice jc) {
		setIsRunning(false) ;
		jobsChoice = jc ;
		backUpTableModel.getBackUpItems().clear() ;
	}

	private JPanel getEmptyPane() {
		return new JPanel() ;
	}
	
	private class ControlAction implements ActionListener {

		private UiControl uiControl ;
		
		public ControlAction(UiControl u) {
			uiControl = u ;
		}
		
		@Override
		public void actionPerformed(ActionEvent ae) {
			
			if (ae.getSource() == bckpUpButton) {
				bLog.fine("Back up action launched") ;
				uiControl.setIsRunning(true);
				
				FilesBackUpProcessor fProcess = new FilesBackUpProcessor(uiControl, jobTaskType, jobsChoice, backUpTableModel, progressPanel, backUpJobInfoTableModel, bLog) ;
				fProcess.execute() ;
				
			} else if (ae.getSource() == scanButton) {				
				bLog.fine("Scan action launched");
				uiControl.setIsRunning(true);
				
				jobsChoice.setCompareContent(jobTaskType, compareContentSelect.isSelected()) ;
				
				FilesBackUpScanner   fScan 	 = new FilesBackUpScanner(uiControl, jobTaskType, jobsChoice, backUpTableModel, progressPanel, backUpJobInfoTableModel, bLog) ;				
				fScan.execute() ;
			}  else if (ae.getSource() == stopButton) {	
			 	stopAsked = true   ;
			}
		
		}
		
	}
	

}
