/*
 * MIT License

Copyright (c) 2017, 2025 Frederic Lefevre

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

import java.awt.Desktop;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import org.fl.backupFiles.AbstractBackUpItem;
import org.fl.backupFiles.BackUpItem;
import org.fl.backupFiles.OsAction;
import org.fl.backupFiles.gui.BackUpItemCustomActionListener.CustomAction;

public class BackupItemMouseAdapter extends MouseAdapter {

	private JPopupMenu localJPopupMenu;
	private BackUpJTable backUpJTable;

	// Menu items
	private List<JMenuItem> sourceMenuItems;
	private List<JMenuItem> targetMenuItems;
	private List<JMenuItem> bothMenuItems;
	private List<JMenuItem> anyMenuItems;
	
	public BackupItemMouseAdapter(BackUpJTable bkt, List<OsAction> osActions) {
		super();
		backUpJTable = bkt;
		localJPopupMenu = new JPopupMenu();

		sourceMenuItems = new ArrayList<JMenuItem>();
		targetMenuItems = new ArrayList<JMenuItem>();
		bothMenuItems = new ArrayList<JMenuItem>();
		anyMenuItems = new ArrayList<JMenuItem>();
		
		// Actions
		Desktop desktop = Desktop.getDesktop();
		if (desktop.isSupported(Desktop.Action.EDIT)) {
			ActionListener editSourceListener = new BackUpItemActionListener(bkt, Desktop.Action.EDIT, FileElement.Source);
			ActionListener editCibleListener = new BackUpItemActionListener(bkt, Desktop.Action.EDIT, FileElement.Cible);
			sourceMenuItems.add(addMenuItem("Editer la source", editSourceListener));
			targetMenuItems.add(addMenuItem("Editer la cible", editCibleListener));
		}
		if (desktop.isSupported(Desktop.Action.OPEN)) {
			ActionListener openSourceListener = new BackUpItemActionListener(bkt, Desktop.Action.OPEN, FileElement.Source);
			ActionListener openCibleListener = new BackUpItemActionListener(bkt, Desktop.Action.OPEN, FileElement.Cible);
			sourceMenuItems.add(addMenuItem("Ouvrir la source", openSourceListener));
			targetMenuItems.add(addMenuItem("Ouvrir la cible", openCibleListener));
		}
		if (desktop.isSupported(Desktop.Action.PRINT)) {
			ActionListener printSourceListener = new BackUpItemActionListener(bkt, Desktop.Action.PRINT, FileElement.Source);
			ActionListener printCibleListener = new BackUpItemActionListener(bkt, Desktop.Action.PRINT, FileElement.Cible);
			sourceMenuItems.add(addMenuItem("Imprimer la source", printSourceListener));
			targetMenuItems.add(addMenuItem("Imprimer la cible", printCibleListener));
		}

		for (OsAction osAction : osActions) {

			if (osAction.paramSeparated()) {
				ActionListener actionSourceListener = new BackUpItemCommandListener(bkt, osAction.getActionCommand(), FileElement.Source);
				ActionListener actionCibleListener = new BackUpItemCommandListener(bkt, osAction.getActionCommand(), FileElement.Cible);

				sourceMenuItems.add(addMenuItem(osAction.getActionTitle() + " source", actionSourceListener));
				targetMenuItems.add(addMenuItem(osAction.getActionTitle() + " cible", actionCibleListener));
			} else {
				ActionListener actionBothListener = new BackUpItemCommandListener(bkt, osAction.getActionCommand(), FileElement.Both);

				bothMenuItems.add(addMenuItem(osAction.getActionTitle(), actionBothListener));
			}
		}

		ActionListener infosActionListener = new BackUpItemCustomActionListener(bkt, CustomAction.Compare, FileElement.Both);
		JMenuItem infosMenuItem = addMenuItem("Afficher des informations sur la source et la cible dont le résultat de la comparaison binaire", infosActionListener);
		anyMenuItems.add(infosMenuItem);

		ActionListener srcParentActionListener = new BackUpItemCustomActionListener(bkt, CustomAction.ShowParentDir, FileElement.Source);
		sourceMenuItems.add(addMenuItem("Afficher le dossier parent de la source", srcParentActionListener));
		ActionListener tgtParentActionListener = new BackUpItemCustomActionListener(bkt, CustomAction.ShowParentDir, FileElement.Cible);
		targetMenuItems.add(addMenuItem("Afficher le dossier parent de la cible", tgtParentActionListener));
	}

	public void mousePressed(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			enableMenuItems();
			localJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}

	public void mouseReleased(MouseEvent evt) {
		if (evt.isPopupTrigger()) {
			enableMenuItems();
			localJPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
		}
	}

	private JMenuItem addMenuItem(String title, ActionListener act) {
		JMenuItem localJMenuItem = new JMenuItem(title);
		localJMenuItem.addActionListener(act);
		localJPopupMenu.add(localJMenuItem);
		return localJMenuItem;
	}
	
	private void enableMenuItems() {
		
		AbstractBackUpItem selectedEntry = backUpJTable.getSelectedBackUpItem() ;
		
		if (selectedEntry != null)  {
			
			if (selectedEntry instanceof BackUpItem backUpItem) {
		
				sourceMenuItems
					.forEach(menuItem -> menuItem.setEnabled(backUpItem.isSourcePresent()));
	
				targetMenuItems
					.forEach(menuItem -> menuItem.setEnabled(backUpItem.isTargetPresent()));
	
				bothMenuItems
					.forEach(menuItem -> menuItem.setEnabled(backUpItem.isSourcePresent() && backUpItem.isTargetPresent()));
				
				anyMenuItems
					.forEach(menuItem -> menuItem.setEnabled(backUpItem.isSourcePresent() || backUpItem.isTargetPresent()));
			
			} else {
				sourceMenuItems.forEach(menuItem -> menuItem.setEnabled(false));
				targetMenuItems.forEach(menuItem -> menuItem.setEnabled(false));
				bothMenuItems.forEach(menuItem -> menuItem.setEnabled(false));		
				anyMenuItems.forEach(menuItem -> menuItem.setEnabled(false));
			}
		}
	}
	
}
