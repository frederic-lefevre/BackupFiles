package org.fl.backupFiles.gui;

import java.awt.Color;
import java.awt.Point;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JViewport;
import javax.swing.text.DefaultCaret;

import com.google.gson.JsonObject;
import com.ibm.lge.fl.util.RunningContext;
import com.ibm.lge.fl.util.json.JsonUtils;

public class ApplicationInfoPane extends JPanel {

	private static final long serialVersionUID = 1L;

	private RunningContext runningContext ;
	
	private JTextArea infosText ;
	private final JScrollPane scrollInfos ;
	
	public ApplicationInfoPane(RunningContext rc) {
		super() ;
		
		runningContext = rc ;
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS)) ;
		setBorder(BorderFactory.createLineBorder(Color.BLACK,5,true)) ;
		
		infosText = new JTextArea() ;
		
		// to always be on the top of the scrollPane
		DefaultCaret caret = (DefaultCaret) infosText.getCaret();
		caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
		
		scrollInfos = new JScrollPane(infosText) ;	
		setInfos() ;
		
		add(scrollInfos) ;	
	}
	
	public void setInfos() {
		
		JsonObject infosJson = runningContext.getApplicationInfo() ;		
		infosText.setText(JsonUtils.jsonPrettyPrint(infosJson)) ;
		
		scrollInfos.getVerticalScrollBar().setValue(0);
		JViewport vp = scrollInfos.getViewport() ;
		vp.setViewPosition(new Point(0,0));
	}
}
