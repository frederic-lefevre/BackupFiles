package org.fl.backupFiles.gui;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.fl.backupFiles.BackUpJob;
import org.fl.backupFiles.BackUpJobList;
import org.fl.backupFiles.JobsChoice;
import org.fl.backupFiles.BackUpJob.JobTaskType;

public class BackUpConfigChoicePane extends JPanel {

	private static final long serialVersionUID = 1L;

	private Logger bLog ;
	
	private JList<BackUpJob> backUpJobChoice ;
	
	private List<BackUpPane> backUpPanes ;
	
	private Map<JobTaskType, JobConfigTableModel> jobConfigTablesModel ;
	
	public BackUpConfigChoicePane(Path configFileDir, List<BackUpPane> bps, Logger l) {
		super();
		
		backUpPanes = bps ;
		bLog = l ;
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)) ;
		
		// Backup configuration choice
		JPanel bkpChoicePanel = new JPanel() ;
		bkpChoicePanel.setLayout(new BoxLayout(bkpChoicePanel, BoxLayout.X_AXIS));
		
		// List of all possible back up jobs
		// The back up jobs are defined in JSON files (one file per back up job)
		// The first user action is to choose the back up job to execute
		BackUpJobList backUpJobs = new BackUpJobList(configFileDir, bLog) ;
		
		JLabel choiceLbl = new JLabel("Choix de la configuration de sauvegarde") ;
		choiceLbl.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		bkpChoicePanel.add(choiceLbl);
		backUpJobChoice = new JList<BackUpJob>(backUpJobs) ;
		backUpJobChoice.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
		bkpChoicePanel.add(backUpJobChoice) ;
			
		add(bkpChoicePanel) ;
		
		JPanel resultPanel = new JPanel() ;
		resultPanel.setLayout(new BoxLayout(resultPanel, BoxLayout.Y_AXIS));

		jobConfigTablesModel = new HashMap<JobTaskType, JobConfigTableModel>() ;
		for (JobTaskType jtt : JobTaskType.values()) {
			JLabel jttLab = new JLabel(jtt.toString()) ;
			resultPanel.add(jttLab) ;
			
			JobConfigTableModel jctm = new JobConfigTableModel() ;
			jobConfigTablesModel.put(jtt, jctm) ;
			JTable tasksTable = new JTable(jctm) ;
			resultPanel.add(tasksTable.getTableHeader()) ;
			resultPanel.add(tasksTable) ;
		}
		
		JScrollPane resultScrollPane = new JScrollPane(resultPanel) ;
		
		add(resultScrollPane) ;
		
		// Each time a back up job is choosen, the backUpTasks are updated accordingly
		backUpJobChoice.addListSelectionListener(new ChooseJobs());
	}

	// Each time back up jobs are chosen, the back up panes are informed
	private class ChooseJobs  implements ListSelectionListener {

		@Override
		public void valueChanged(ListSelectionEvent arg0) {
			
			if (arg0.getValueIsAdjusting() == false) {
							
				List<BackUpJob> jobsChoiceList = backUpJobChoice.getSelectedValuesList() ;
				JobsChoice jobsChoice = new JobsChoice(jobsChoiceList, bLog) ;
			
				for (JobTaskType jtt : JobTaskType.values()) {
					
					JobConfigTableModel configTableModel = jobConfigTablesModel.get(jtt) ;
					configTableModel.setBackUpTasks(jobsChoice.getTasks(jtt));
					configTableModel.fireTableDataChanged() ;
				}
				
				for (BackUpPane backUpPane : backUpPanes) {
					backUpPane.setJobChoice(jobsChoice) ;
				}
			}
		}
	}
	

}
