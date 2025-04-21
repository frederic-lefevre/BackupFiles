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

public enum BackupStatus {
	DIFFERENT("Différent", "Les 2 éléments sont différents"), 
	DIFF_BY_CONTENT("Contenu différent", "Les 2 éléments ont la même date de modification mais un contenu différent"), 
	SAME_CONTENT("Même contenu", "Les 2 éléments ont un contenu identique mais la destination a une date de modification plus récente que l'origine"),
	DONE("Fait", "L'opération de sauvegarde a réussi"), 
	FAILED("Erreur", "L'opération de sauvegarde a échoué");
	
	private final String statusName;
	private final String statusDetail;
	
	private BackupStatus(String statusName, String statusDetail) {
		this.statusName = statusName;
		this.statusDetail = statusDetail;
	}

	public String getStatusName() {
		return statusName;
	}

	public String getStatusDetail() {
		return statusDetail;
	}
}