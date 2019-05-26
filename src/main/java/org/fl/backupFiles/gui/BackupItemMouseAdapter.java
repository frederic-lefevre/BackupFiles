package org.fl.backupFiles.gui;

import java.awt.Desktop;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.OsAction;
import org.fl.backupFiles.BackUpItem.BackupAction;
import org.fl.backupFiles.gui.BackUpItemActionListener.CustomAction;
import org.fl.backupFiles.gui.BackUpItemActionListener.FileElement;

public class BackupItemMouseAdapter extends MouseAdapter {

	private JPopupMenu   localJPopupMenu ;
	private BackUpJTable backUpJTable ;
	
	// Menu items
	private ArrayList<JMenuItem> sourceMenuItems ;
	private ArrayList<JMenuItem> targetMenuItems ;
	private ArrayList<JMenuItem> bothMenuItems ;
	private ArrayList<JMenuItem> anyMenuItems ;
	
	public BackupItemMouseAdapter(BackUpJTable bkt, ArrayList<OsAction> osActions, Logger bLog) {
		super() ;
		backUpJTable 	= bkt ;
		localJPopupMenu = new JPopupMenu();

		sourceMenuItems = new ArrayList<JMenuItem>() ;
		targetMenuItems = new ArrayList<JMenuItem>() ;
		bothMenuItems   = new ArrayList<JMenuItem>() ;
		anyMenuItems    = new ArrayList<JMenuItem>() ;
		
		// Actions 
		Desktop desktop = Desktop.getDesktop() ;
		if (desktop.isSupported(Desktop.Action.EDIT)) {
			BackUpItemActionListener editSourceListener  = new BackUpItemActionListener(bkt, Desktop.Action.EDIT,  FileElement.Source, bLog) ;
			BackUpItemActionListener editCibleListener   = new BackUpItemActionListener(bkt, Desktop.Action.EDIT,  FileElement.Cible,  bLog) ;
			sourceMenuItems.add(addMenuItem("Editer la source",  editSourceListener)) ;
			targetMenuItems.add(addMenuItem("Editer la cible",   editCibleListener)) ;
		}
		if (desktop.isSupported(Desktop.Action.OPEN)) {
			BackUpItemActionListener openSourceListener  = new BackUpItemActionListener(bkt, Desktop.Action.OPEN,  FileElement.Source, bLog) ;
			BackUpItemActionListener openCibleListener   = new BackUpItemActionListener(bkt, Desktop.Action.OPEN,  FileElement.Cible,  bLog) ;
			sourceMenuItems.add(addMenuItem("Ouvrir la source",  openSourceListener)) ;
			targetMenuItems.add(addMenuItem("Ouvrir la cible",   openCibleListener)) ;
		}
		if (desktop.isSupported(Desktop.Action.PRINT)) {
			BackUpItemActionListener printSourceListener = new BackUpItemActionListener(bkt, Desktop.Action.PRINT, FileElement.Source, bLog) ;
			BackUpItemActionListener printCibleListener  = new BackUpItemActionListener(bkt, Desktop.Action.PRINT, FileElement.Cible,  bLog) ;
			sourceMenuItems.add(addMenuItem("Print source", printSourceListener)) ;
			targetMenuItems.add(addMenuItem("Print cible",  printCibleListener)) ;
		}

		for (OsAction osAction : osActions) {

			if (osAction.paramSeparated()) {
				BackUpItemActionListener actionSourceListener  = new BackUpItemActionListener(bkt, osAction.getActionCommand(),  FileElement.Source, bLog) ;
				BackUpItemActionListener actionCibleListener   = new BackUpItemActionListener(bkt, osAction.getActionCommand(),  FileElement.Cible,  bLog) ;

				sourceMenuItems.add(addMenuItem(osAction.getActionTitle() + " source",  actionSourceListener)) ;
				targetMenuItems.add(addMenuItem(osAction.getActionTitle() + " cible",   actionCibleListener)) ;
			} else {
				BackUpItemActionListener actionBothListener   = new BackUpItemActionListener(bkt, osAction.getActionCommand(),  FileElement.Both,  bLog) ;

				bothMenuItems.add(addMenuItem(osAction.getActionTitle(),  actionBothListener)) ;
			}
		}
		
		BackUpItemActionListener infosActionListener = new BackUpItemActionListener(bkt, CustomAction.Compare, FileElement.Both, bLog) ;
		JMenuItem infosMenuItem = addMenuItem("Afficher des informations sur la source et la cible dont le résultat de la comparaison binaire", infosActionListener) ;
		anyMenuItems.add(infosMenuItem) ;
		
		BackUpItemActionListener srcParentActionListener = new BackUpItemActionListener(bkt, CustomAction.ShowParentDir, FileElement.Source, bLog) ;
		sourceMenuItems.add(addMenuItem("Afficher le dossier parent de la source", srcParentActionListener)) ;
		BackUpItemActionListener tgtParentActionListener = new BackUpItemActionListener(bkt, CustomAction.ShowParentDir, FileElement.Cible, bLog) ;
		targetMenuItems.add(addMenuItem("Afficher le dossier parent de la cible", tgtParentActionListener)) ;

	}

    public void mousePressed(MouseEvent evt)  {
      if (evt.isPopupTrigger()) {
    	  enableMenuItems() ;
    	  localJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
      }
    }
    
    public void mouseReleased(MouseEvent evt)  {
      if (evt.isPopupTrigger()) {
    	  enableMenuItems() ;
        localJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
      }
    }
    
	private JMenuItem addMenuItem(String title, ActionListener act) {
		JMenuItem localJMenuItem = new JMenuItem(title);
	     localJMenuItem.addActionListener(act);
	     localJPopupMenu.add(localJMenuItem);
	     return localJMenuItem ;
	}	
	
	private void enableMenuItems() {
		
		ArrayList<BackUpItem> selectedEntries = backUpJTable.getSelectedBackUpItems() ;
		
		boolean sourcePresent = false ;
		boolean targetPresent = false ;
		for (BackUpItem localEntry : selectedEntries) {
			
			BackupAction backupAction = localEntry.getBackupAction() ;
			if ( backupAction.equals(BackupAction.COPY_REPLACE) ||
				 backupAction.equals(BackupAction.AMBIGUOUS)) {
			
				sourcePresent = true ;
				targetPresent = true ;
			} else if ( backupAction.equals(BackupAction.COPY_NEW) ||
					 backupAction.equals(BackupAction.COPY_TREE)) {
				sourcePresent = true ;
			} else if ( backupAction.equals(BackupAction.DELETE) ||
					 backupAction.equals(BackupAction.DELETE_DIR)) {
				targetPresent = true ;
			}
		}
		
		for (JMenuItem menuItem : sourceMenuItems) {
			menuItem.setEnabled(sourcePresent);
		}
		for (JMenuItem menuItem : targetMenuItems) {
			menuItem.setEnabled(targetPresent);
		}
		for (JMenuItem menuItem : bothMenuItems) {
			menuItem.setEnabled(sourcePresent && targetPresent);
		}
		for (JMenuItem menuItem : anyMenuItems) {
			menuItem.setEnabled(sourcePresent || targetPresent);
		}
	}
	
}
