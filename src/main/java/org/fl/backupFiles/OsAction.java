package org.fl.backupFiles;

public class OsAction {

	private final String  actionTitle ;
	private final String  actionCommand ;
	private final boolean separateParam ;
	
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
