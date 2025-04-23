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

package org.fl.backupFiles;

public enum BackupAction {
	COPY_NEW("Copier nouveau", "Copier le fichier source dans la destination"), 
	COPY_REPLACE("Remplacer", "Remplacer le fichier destination par la source"), 
	COPY_TREE("Copier arbre", "Copier l'arbre source dans la destination"), 
	DELETE("Effacer", "Effacer le fichier destination"), 
	DELETE_DIR("Effacer arbre", "Effacer l'arbre destination"), 
	AMBIGUOUS("Ambigu", "Le fichier destination est plus récent, remplacer le fichier destination par la source"), 
	COPY_TARGET("Copier cible", "Remplacer le fichier source par la destination (ils ont un contenu identique)"), 
	ADJUST_TIME("Ajuster temps", "Remplacer le temps de modification du fichier destination avec celui de la source (ils ont un contenu identique)");
	
	private final String actionName;
	private final String actionDetails;
	
	private BackupAction(String actionName, String actionDetails) {
		this.actionName = actionName;
		this.actionDetails = actionDetails;
	}
	
	public String getActionName() {
		return actionName;
	}
	
	public String getActionDetails() {
		return actionDetails;
	}
}