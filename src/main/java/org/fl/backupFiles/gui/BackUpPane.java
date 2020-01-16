package org.fl.backupFiles.gui;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpJob.JobTaskType;
import org.fl.backupFiles.JobsChoice;

public class BackUpPane extends JPanel {

	private static final long serialVersionUID = 1L;

	private UiControl 	backUpControl ;
	private JLabel	  	title ;
	private JobTaskType jobTaskType ;
	
	private final static String NO_CONFIG = "Aucune" ;
	private final static String SEPARATOR = " - Configuration actuellement sélectionnée: " ;
	
	public BackUpPane(JobTaskType jtt, BackUpJobInfoTableModel backUpJobInfoTableModel, Logger bLog) {
		super() ;
		
		jobTaskType = jtt ;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)) ;
		setBorder(BorderFactory.createLineBorder(Color.BLACK,5,true)) ;
		
		BackUpItemList backUpItemList = new BackUpItemList() ;
		BackUpTableModel backUpTableModel= new BackUpTableModel(backUpItemList) ;
		
		BackUpJTable backUpItemTable = new BackUpJTable(backUpTableModel, bLog) ;

		// Tables labels
		title = new JLabel(jobTaskType.toString() + SEPARATOR + NO_CONFIG) ;
		Font font = new Font("Verdana", Font.BOLD, 18);
		title.setFont(font);
		title.setAlignmentX(CENTER_ALIGNMENT);
		add(title) ;
		
		// Status and progress information
		ProgressInformationPanel pip = new ProgressInformationPanel() ;
		pip.setAlignmentX(CENTER_ALIGNMENT);
		add(pip) ;
		pip.setProcessStatus("Initial") ;
		pip.setStepInfos("Aucune action effectuée", 0) ;
		
		// Scan and Back up buttons
		backUpControl = new UiControl(jobTaskType, backUpTableModel, pip, backUpJobInfoTableModel, bLog) ;
		backUpControl.setMaximumSize(new Dimension(1700, 100));
		add(backUpControl) ;
		
		// Table headers
		add(backUpItemTable.getTableHeader()) ;
		
		// Scroll pane to contain the tables
		JScrollPane bufferScrollTable = new JScrollPane(backUpItemTable) ;
		
		add(bufferScrollTable) ;
	}

	public void setJobChoice(JobsChoice jobChoice) {
		title.setText(jobTaskType.toString()  + SEPARATOR + jobChoice.getTitleAsString());
		backUpControl.setJobChoice(jobChoice) ;
	}


}
