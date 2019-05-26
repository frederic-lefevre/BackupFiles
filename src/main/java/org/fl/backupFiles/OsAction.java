package org.fl.backupFiles;

public class OsAction {

	private String  actionTitle ;
	private String  actionCommand ;
	private boolean separateParam ;
	
	public OsAction(String t, String c, boolean s) {
		actionTitle   = t ;
		actionCommand = c ;
		separateParam = s ;
	}

	public String getActionTitle() {
		return actionTitle;
	}

	public String getActionCommand() {
		return actionCommand;
	}

	public boolean paramSeparated() {
		return separateParam ;
	}
}
