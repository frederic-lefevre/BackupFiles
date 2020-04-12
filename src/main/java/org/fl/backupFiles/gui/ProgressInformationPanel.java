package org.fl.backupFiles.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class ProgressInformationPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private final JLabel 			lblStepInfo;
	private final JScrollPane 		stepInfoScroll ;
	private final JLabel 			lblStatus ;
	private final JLabel 			lblStatusDate ;
	private final JLabel 			lblNum ;
	private final DateTimeFormatter dateFranceFormat;
	
	private final static String dateFrancePattern = " EEEE dd MMMM uuuu à HH:mm:ss" ;
	
	public ProgressInformationPanel() {
		
		super() ;
		dateFranceFormat = DateTimeFormatter.ofPattern(dateFrancePattern) ;
		Font statusFont = new Font("Verdana", Font.BOLD, 14);
		Font progressTitleFont = new Font("Verdana", Font.BOLD, 14);
		Font progressInfoFont = new Font("monospaced", Font.PLAIN, 11);
		Font nbElemFont = new Font("Verdana", Font.BOLD, 14);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

		setBackground(Color.WHITE) ;
		setBorder(BorderFactory.createMatteBorder(10,10,10,10,Color.WHITE)) ;
		setAlignmentX(Component.LEFT_ALIGNMENT) ;
		
		// Status
		JPanel statusPane = new JPanel() ;
		statusPane.setLayout(new BoxLayout(statusPane, BoxLayout.X_AXIS)) ;
		JLabel lblStatusTitle = new JLabel("Etat: ");	
		lblStatus = new JLabel("");
		lblStatusTitle.setFont(statusFont) ;
		lblStatusTitle.setAlignmentX(Component.LEFT_ALIGNMENT) ;
		lblStatusTitle.setBackground(Color.WHITE) ;
		lblStatus.setFont(statusFont) ;
		lblStatus.setAlignmentX(Component.LEFT_ALIGNMENT) ;
		lblStatus.setBackground(Color.WHITE) ;
		lblStatusDate = new JLabel("") ;
		lblStatusDate.setFont(statusFont) ;
		lblStatusDate.setAlignmentX(Component.LEFT_ALIGNMENT) ;
		lblStatusDate.setBackground(Color.WHITE) ;
		statusPane.add(lblStatusTitle) ;
		statusPane.add(lblStatus) ;
		statusPane.add(lblStatusDate) ;
		statusPane.setBackground(Color.WHITE) ;
		statusPane.setAlignmentX(Component.LEFT_ALIGNMENT) ;
		add(statusPane) ;
		
		JPanel infoStep = new JPanel() ;
		infoStep.setLayout(new BoxLayout(infoStep, BoxLayout.Y_AXIS)) ;
		
		// Progression
		JLabel lblStep = new JLabel("Progression: ");
		lblStepInfo = new JLabel();
		lblStep.setAlignmentX(Component.LEFT_ALIGNMENT) ;
		lblStep.setFont(progressTitleFont) ;
		lblStepInfo.setFont(progressInfoFont) ;
		lblStepInfo.setBackground(Color.WHITE) ;
		
		lblStep.setBackground(Color.WHITE) ;
		infoStep.add(lblStep) ;
		stepInfoScroll = new JScrollPane(lblStepInfo) ;
		infoStep.add(stepInfoScroll) ;
		infoStep.setAlignmentX(Component.LEFT_ALIGNMENT) ;
		infoStep.setBackground(Color.WHITE) ;
		JPanel numPane = new JPanel() ;
		numPane.setLayout(new BoxLayout(numPane, BoxLayout.X_AXIS)) ;
		
		// Nombre d'éléments traités
		JLabel lblNumTitle = new JLabel("Nombre d'éléments traités: ") ;
		lblNumTitle.setFont(nbElemFont);
		lblNum = new JLabel() ;
		lblNum.setFont(nbElemFont);
		numPane.add(lblNumTitle) ;
		numPane.add(lblNum) ;
		infoStep.add(numPane) ;
		add(infoStep) ;
	}

	public void setStepInfos(String info, long num) {
		 lblStepInfo.setText(info);
		 lblNum.setText(Long.toString(num));
	}
	
	public void setProcessStatus(String st) {
		lblStatus.setText(st) ;
		lblStatusDate.setText(dateFranceFormat.format(LocalDateTime.now()));
	}
}
