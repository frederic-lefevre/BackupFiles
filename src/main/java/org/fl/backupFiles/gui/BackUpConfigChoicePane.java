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
		BackUpJobList backUpJobs = new BackUpJobList(configFileDir) ;
		
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
		
		// Each time a back up job is chosen, the backUpTasks are updated accordingly
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
