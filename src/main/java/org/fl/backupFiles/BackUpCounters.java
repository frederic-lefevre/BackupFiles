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

import java.io.IOException;
import java.nio.file.FileStore;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackUpCounters {

	private static final Logger logger = Logger.getLogger(BackUpCounters.class.getName());
	
	// Locale.FRANCE affiche le séparateur de milliers avec un "Narrow non-breaking space", ce qui pose des problèmes
	// d'affichage avec beaucoup d'outils (console Eclipse, lorsqu'on édite les logs avec Notepad++ par exemple)
	private static final Locale localeForFormat = Locale.CANADA_FRENCH;
	
	private static final NumberFormat numberFormat = NumberFormat.getInstance(localeForFormat);
	
	public long copyNewNb;
	public long copyReplaceNb;
	public long copyTreeNb;
	public long deleteNb;
	public long deleteDirNb;
	public long ambiguousNb;
	public long copyTargetNb;
	public long adjustTimeNb;
	public long contentDifferentNb;
	
	public long nbSourceFilesProcessed;
	public long nbTargetFilesProcessed;
	public long nbSourceFilesFailed;
	public long nbTargetFilesFailed;
	public long backupWithSizeAboveThreshold;
	public long nbHighPermanencePath;
	public long nbMediumPermanencePath;
	
	private final TargetFileStores targetFileStores;
	
	private static final String COPY_NEW_LABEL = BackupAction.COPY_NEW.getActionName() + ": ";
	private static final String COPY_REPLACE_LABEL = BackupAction.COPY_REPLACE.getActionName() + ": ";
	private static final String COPY_TREE_LABEL = BackupAction.COPY_TREE.getActionName() + ": ";
	private static final String DELETE_LABEL = BackupAction.DELETE.getActionName() + ": ";
	private static final String DELETE_DIR_LABEL = BackupAction.DELETE_DIR.getActionName() + ": ";
	private static final String AMBIGUOUS_LABEL = BackupAction.AMBIGUOUS.getActionName() + ": ";
	private static final String COPY_TARGET_LABEL = BackupAction.COPY_TARGET.getActionName() + ": ";
	private static final String ADJUST_TIME_LABEL = BackupAction.ADJUST_TIME.getActionName() + ": ";

	private static final String SOURCE_OK_LABEL = "Eléments source traités: ";
	private static final String SOURCE_KO_LABEL = "Eléments source en erreur: ";
	private static final String TARGET_OK_LABEL = "Eléments cible traités: ";
	private static final String TARGET_KO_LABEL = "Eléments cible en erreur: ";

	private static final String CONTENT_DIFFERENT_LABEL = "Fichiers avec contenu différent:  ";
	private static final String SIZE_ABOVE_LIMIT_LABEL = "Fichiers avec tailles importantes: ";
	private static final String HIGH_PERMANENCE_LABEL = "Fichiers à haute permanence: ";
	private static final String MEDIUM_PERMANENCE_LABEL = "Fichiers à moyenne permanence: ";
	private static final String TOTAL_SIZE_DIFF_LABEL = "Différence totale de taille: ";
	
	public BackUpCounters(TargetFileStores targetFileStores) {
		this.targetFileStores = targetFileStores;
		reset();
	}

	public void reset() {
		copyNewNb = 0;
		copyReplaceNb = 0;
		copyTreeNb = 0;
		deleteNb = 0;
		deleteDirNb = 0;
		ambiguousNb = 0;
		copyTargetNb = 0;
		adjustTimeNb = 0;
		contentDifferentNb = 0;
		nbSourceFilesProcessed = 0;
		nbTargetFilesProcessed = 0;
		nbSourceFilesFailed = 0;
		nbTargetFilesFailed = 0;
		backupWithSizeAboveThreshold = 0;
		nbHighPermanencePath = 0;
		nbMediumPermanencePath = 0;
		targetFileStores.reset();
	}
	
	public TargetFileStores getTargetFileStores() {
		return targetFileStores;
	}

	public long recordPotentialSizeChange(FileStore fileStore, long difference) {	
		return targetFileStores.recordPotentialSizeChange(fileStore, difference);
	}
	
	public void add(BackUpCounters counters) {

		copyNewNb = copyNewNb + counters.copyNewNb;
		copyReplaceNb = copyReplaceNb + counters.copyReplaceNb;
		copyTreeNb = copyTreeNb + counters.copyTreeNb;
		deleteNb = deleteNb + counters.deleteNb;
		deleteDirNb = deleteDirNb + counters.deleteDirNb;
		ambiguousNb = ambiguousNb + counters.ambiguousNb;
		copyTargetNb = copyTargetNb + counters.copyTargetNb;
		adjustTimeNb = adjustTimeNb + counters.adjustTimeNb;
		contentDifferentNb = contentDifferentNb + counters.contentDifferentNb;
		nbSourceFilesProcessed = nbSourceFilesProcessed + counters.nbSourceFilesProcessed;
		nbTargetFilesProcessed = nbTargetFilesProcessed + counters.nbTargetFilesProcessed;
		nbSourceFilesFailed = nbSourceFilesFailed + counters.nbSourceFilesFailed;
		nbTargetFilesFailed = nbTargetFilesFailed + counters.nbTargetFilesFailed;
		backupWithSizeAboveThreshold = backupWithSizeAboveThreshold + counters.backupWithSizeAboveThreshold;
		nbHighPermanencePath = nbHighPermanencePath + counters.nbHighPermanencePath;
		nbMediumPermanencePath = nbMediumPermanencePath + counters.nbMediumPermanencePath;
		targetFileStores.mergeWith(counters.getTargetFileStores());
	}
		
	private long getTotalPotentialSizeChange() {
		return targetFileStores.getTotalPotentialSizeChange();
	}
	
	public void appendInfoText(StringBuilder res) {

		res.append(COPY_NEW_LABEL).append(copyNewNb).append("\n").append(DELETE_LABEL).append(deleteNb).append("\n");
		res.append(COPY_REPLACE_LABEL).append(copyReplaceNb).append("\n").append(DELETE_DIR_LABEL).append(deleteDirNb).append("\n");
		res.append(COPY_TREE_LABEL).append(copyTreeNb).append("\n").append(AMBIGUOUS_LABEL).append(ambiguousNb).append("\n");
		res.append(COPY_TARGET_LABEL).append(copyTargetNb).append("\n").append(ADJUST_TIME_LABEL).append(adjustTimeNb).append("\n");

		res.append(SOURCE_OK_LABEL).append(nbSourceFilesProcessed).append("\n").append(SOURCE_KO_LABEL).append(nbSourceFilesFailed)
				.append("\n");
		res.append(TARGET_OK_LABEL).append(nbTargetFilesProcessed).append("\n").append(TARGET_KO_LABEL).append(nbTargetFilesFailed)
				.append("\n");

		res.append(SIZE_ABOVE_LIMIT_LABEL).append(backupWithSizeAboveThreshold).append("\n");
		res.append(HIGH_PERMANENCE_LABEL).append(nbHighPermanencePath).append("\n").append(MEDIUM_PERMANENCE_LABEL)
				.append(nbMediumPermanencePath).append("\n");

		if (contentDifferentNb != 0) {
			res.append(CONTENT_DIFFERENT_LABEL).append(contentDifferentNb).append("\n");
		}
		
		res.append("Stockage de fichiers, espace restant utilisable:\n");
		targetFileStores.getAllTargetFileStore().forEach(fileStore -> appendFileStoreInfo(res, fileStore, "", false));
	}
	
	public void appendCounterInfoInHtml(StringBuilder res) {
		
		res.append(TABLE_AND_ROW_BEGIN);
		appendCellCouple(res, COPY_TREE_LABEL, copyTreeNb, null);
		appendCellCouple(res, COPY_NEW_LABEL, copyNewNb, null);
		appendCellCouple(res, COPY_REPLACE_LABEL, copyReplaceNb, null);
		appendCellCouple(res, COPY_TARGET_LABEL, copyTargetNb, "red");

		res.append(NEW_ROW);
		appendCellCouple(res, DELETE_LABEL, deleteNb, null);
		appendCellCouple(res, DELETE_DIR_LABEL, deleteDirNb, null);
		appendCellCouple(res, AMBIGUOUS_LABEL, ambiguousNb, "red");
		appendCellCouple(res, ADJUST_TIME_LABEL, adjustTimeNb, "red");

		res.append(NEW_ROW);
		appendCellCouple(res, SOURCE_OK_LABEL, nbSourceFilesProcessed, null);
		appendCellCouple(res, SOURCE_KO_LABEL, nbSourceFilesFailed, "red");

		res.append(NEW_ROW);
		appendCellCouple(res, TARGET_OK_LABEL, nbTargetFilesProcessed, null);
		appendCellCouple(res, TARGET_KO_LABEL, nbTargetFilesFailed, "red");

		res.append(NEW_ROW);
		appendCellCouple(res, TOTAL_SIZE_DIFF_LABEL, getTotalPotentialSizeChange(), null);
		appendCellCouple(res, SIZE_ABOVE_LIMIT_LABEL, backupWithSizeAboveThreshold, "red");

		res.append(NEW_ROW);
		appendCellCouple(res, HIGH_PERMANENCE_LABEL, nbHighPermanencePath, "red");
		appendCellCouple(res, MEDIUM_PERMANENCE_LABEL, nbMediumPermanencePath, "#ff8f00");

		if (contentDifferentNb != 0) {
			res.append(NEW_ROW);
			appendCellCouple(res, CONTENT_DIFFERENT_LABEL, contentDifferentNb, null);
		}
		res.append(ROW_AND_TABLE_END);
	}
	
	public void appendCounterAndFileStoreInfoInHtml(StringBuilder res) {

		appendCounterInfoInHtml(res);
		
		res.append(TABLE_BEGIN);
		appendFileStoreHeaderRow(res);
		targetFileStores.getAllTargetFileStore().forEach(fileStore ->
			appendFileStoreInfo(res, fileStore, "", true)
		);
		res.append(TABLE_END);
	}
	
	private static final String TABLE_BEGIN = "<table border=1>";
	private static final String TABLE_END = "</table>";
	private static final String ROW_BEGIN = "<tr>";
	private static final String ROW_END = "</tr>";
	private static final String NEW_ROW = "</tr><tr>";
	private static final String TABLE_AND_ROW_BEGIN = "<table><tr>";
	private static final String ROW_AND_TABLE_END = "</tr></table>";
	private static final String TWO_EMPTY_CELLS = "<td></td><td></td>";
	private static final String CELL_BEGIN = "<td>";
	private static final String RIGHT_ALIGNED_CELL_BEGIN = "<td style=\"text-align:right\">";
	private static final String RIGHT_ALIGNED_RED_CELL_BEGIN = "<td style=\"text-align:right\" bgcolor=\"red\">";
	private static final String CELL_END = "</td>";
	private static final String CELL_BREAK = "</td><td style=\"text-align:right\">";
	private static final String TAG_END = ">";
	private static final String CELL_WITH_COLOR_BEGIN = "<td bgcolor=";
	private static final String CELL_WITH_COLOR_BREAK = "</td><td style=\"text-align:right\" bgcolor=";
	private static final String IN_ERROR = "En erreur!";

	private void appendCellCouple(StringBuilder res, String label, long value, String color) {
		
		boolean colorPresent = (color != null) && (! color.isEmpty()) && (value > 0);
		String formattedValue = numberFormat.format(value);
		if (label == null) {
			res.append(TWO_EMPTY_CELLS) ;
		} else if (colorPresent) {
			res.append(CELL_WITH_COLOR_BEGIN)
				.append(color)
				.append(TAG_END)
				.append(label)
				.append(CELL_WITH_COLOR_BREAK)
				.append(color)
				.append(TAG_END)
				.append(formattedValue)
				.append(CELL_END);
		} else {
			res.append(CELL_BEGIN)
				.append(label)
				.append(CELL_BREAK)
				.append(formattedValue)
				.append(CELL_END);
		}
	}
	
	private void appendFileStoreHeaderRow(StringBuilder res) {
		res.append(ROW_BEGIN)
			.append(CELL_BEGIN)
			.append("Unité de stockage")
			.append(CELL_END)
			.append(CELL_BEGIN)
			.append("Espace total")
			.append(CELL_END)
			.append(CELL_BEGIN)
			.append("Espace restant")
			.append(CELL_END)
			.append(CELL_BEGIN)
			.append("Evolution<br/> de l'espace restant")
			.append(CELL_END)
			.append(CELL_BEGIN)
			.append("Besoin d'espace<br/> pour la sauvegarde")
			.append(CELL_END)
			.append(ROW_END);
	}
	
	private void appendFileStoreInfo(StringBuilder res, TargetFileStore targetFileStore, String color, boolean complete) {
		
		String targetFileStoreName = targetFileStore.getName();
		String usableSpace;
		String spaceEvolution;
		long usableSize;
		
		try {
			usableSize = targetFileStore.getUsableSpace();
			usableSpace = numberFormat.format(targetFileStore.getUsableSpace());
			long difference = usableSize - targetFileStore.getInitialRemainingSpace();
			if (difference > 0) {
				spaceEvolution = "+" + numberFormat.format(difference);
			} else {
				spaceEvolution = numberFormat.format(difference);
			}
		} catch (IOException e) {
			usableSize = 0;
			usableSpace = IN_ERROR;
			spaceEvolution = IN_ERROR;
			logger.log(Level.SEVERE, "IOEXCEPTION getting usable space for " + targetFileStoreName, e);
		}
		if (complete) {
			String totalSpace = numberFormat.format(targetFileStore.getTotalSpace());
			long potentialChange = targetFileStore.getPotentialSizeChange();
			String potentialSizeChange = numberFormat.format(potentialChange);
			long warningThresholdForRemainingSpace = targetFileStore.getWarningThresholdForRemainingSpace();
			String usableSpaceBeginCellTag;
			if (usableSize < warningThresholdForRemainingSpace) {
				usableSpaceBeginCellTag = RIGHT_ALIGNED_RED_CELL_BEGIN;
			} else {
				usableSpaceBeginCellTag = RIGHT_ALIGNED_CELL_BEGIN;
			}
			String potentialSizeChangeCellTag;
			if (usableSize - potentialChange < warningThresholdForRemainingSpace) {
				potentialSizeChangeCellTag = RIGHT_ALIGNED_RED_CELL_BEGIN;
			} else {
				potentialSizeChangeCellTag = RIGHT_ALIGNED_CELL_BEGIN;
			}
			res.append(ROW_BEGIN)
				.append(CELL_BEGIN)
				.append(targetFileStoreName)
				.append(CELL_END)
				.append(RIGHT_ALIGNED_CELL_BEGIN)
				.append(totalSpace)
				.append(CELL_END)
				.append(usableSpaceBeginCellTag)
				.append(usableSpace)
				.append(CELL_END)
				.append(RIGHT_ALIGNED_CELL_BEGIN)
				.append(spaceEvolution)
				.append(CELL_END)
				.append(potentialSizeChangeCellTag)
				.append(potentialSizeChange)
				.append(CELL_END)
				.append(ROW_END);
		} else {
			res.append("- ").append(targetFileStoreName).append(" = ")
				.append(usableSpace).append(" ( ")
				.append(spaceEvolution).append(" ) bytes")
				.append("\n");
		}
	}
}
