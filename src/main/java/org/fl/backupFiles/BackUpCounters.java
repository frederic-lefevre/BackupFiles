package org.fl.backupFiles;

public class BackUpCounters {

	public long copyNewNb ;
	public long copyReplaceNb ;
	public long copyTreeNb ;
	public long deleteNb ;
	public long deleteDirNb ;
	public long ambiguousNb ;
	public long contentDifferentNb ;
	
	public long nbSourceFilesProcessed ;
	public long nbTargetFilesProcessed ;
	public long nbSourceFilesFailed ;
	public long nbTargetFilesFailed ;
	public long backupWithSizeAboveThreshold ;
	public long nbHighPermanencePath ;
	public long nbMediumPermanencePath ;
	public long totalSizeDifference ;
	
	private final static String COPY_NEW_LABEL 		= "  Copier nouveau:     " ;
	private final static String COPY_REPLACE_LABEL  = "  Remplacer:          " ;
	private final static String COPY_TREE_LABEL 	= "  Copier arbre:       " ;
	private final static String DELETE_LABEL 		= "  Effacer:            " ;
	private final static String DELETE_DIR_LABEL 	= "  Effacer arbre:      " ;
	private final static String AMBIGUOUS_LABEL 	= "  Ambigu:             " ;
	
	public final static String SOURCE_OK_LABEL 	= "  Eléments source traités:   " ;
	public final static String SOURCE_KO_LABEL 	= "  Eléments source en erreur: " ;
	public final static String TARGET_OK_LABEL 	= "  Eléments target traités:   " ;
	public final static String TARGET_KO_LABEL 	= "  Eléments target en erreur: " ;
	
	private final static String CONTENT_DIFFERENT_LABEL 	= "  Fichiers avec contenu différent:  " ;
	private final static String SIZE_ABOVE_LIMIT_LABEL		= "  Fichiers avec tailles importantes: " ;
	private final static String HIGH_PERMANENCE_LABEL		= "  Fichiers à haute permanence: " ;
	private final static String MEDIUM_PERMANENCE_LABEL		= "  Fichiers à moyenne permanence: " ;
	private final static String TOTAL_SIZE_DIFF_LABEL		= "  Différence totale de taille: " ;
	
	public BackUpCounters() {
		reset() ;
	}

	public void reset() {
		copyNewNb 					 = 0 ;
		copyReplaceNb 				 = 0 ;
		copyTreeNb 					 = 0 ;
		deleteNb					 = 0 ;
		deleteDirNb 				 = 0 ;
		ambiguousNb 				 = 0 ; 
		contentDifferentNb			 = 0 ;
		nbSourceFilesProcessed 		 = 0 ;
		nbTargetFilesProcessed 		 = 0 ;
		nbSourceFilesFailed 		 = 0 ;
		nbTargetFilesFailed 		 = 0 ;
		backupWithSizeAboveThreshold = 0 ;
		nbHighPermanencePath		 = 0 ;
		nbMediumPermanencePath		 = 0 ;
		totalSizeDifference			 = 0 ;
	}
	
	@Override
	public String toString() {
		
		StringBuilder res = new StringBuilder() ;
		
		res.append(COPY_NEW_LABEL	 ).append(copyNewNb	   ).append(DELETE_LABEL	).append(deleteNb	).append("\n") ;
		res.append(COPY_REPLACE_LABEL).append(copyReplaceNb).append(DELETE_DIR_LABEL).append(deleteDirNb).append("\n") ;
		res.append(COPY_TREE_LABEL	 ).append(copyTreeNb   ).append(AMBIGUOUS_LABEL	).append(ambiguousNb).append("\n") ;
		
		res.append(SOURCE_OK_LABEL).append(nbSourceFilesProcessed).append(SOURCE_KO_LABEL).append(nbSourceFilesFailed).append("\n") ;
		res.append(TARGET_OK_LABEL).append(nbTargetFilesProcessed).append(TARGET_KO_LABEL).append(nbTargetFilesFailed).append("\n") ;
		
		res.append(SIZE_ABOVE_LIMIT_LABEL).append(backupWithSizeAboveThreshold).append("\n") ;
		res.append(HIGH_PERMANENCE_LABEL).append(nbHighPermanencePath).append(MEDIUM_PERMANENCE_LABEL).append(nbMediumPermanencePath).append("\n") ;
		
		if (contentDifferentNb != 0) {
			res.append(CONTENT_DIFFERENT_LABEL).append(contentDifferentNb) ;
		}
		res.append(TOTAL_SIZE_DIFF_LABEL).append(totalSizeDifference).append("\n") ;
		return res.toString() ;
	}
	
	public void appendHtmlFragment(StringBuilder res) {
		
		res.append("<table><tr>") ;
		appendCellCouple(res, COPY_TREE_LABEL, copyTreeNb, null) ;
		appendCellCouple(res, COPY_NEW_LABEL, copyNewNb, null) ;
		appendCellCouple(res, COPY_REPLACE_LABEL, copyReplaceNb, null) ;
		
		res.append(NEW_ROW) ;
		appendCellCouple(res, DELETE_LABEL, deleteNb, null) ;
		appendCellCouple(res, DELETE_DIR_LABEL, deleteDirNb, null) ;
		appendCellCouple(res, AMBIGUOUS_LABEL, ambiguousNb, "red") ;
		
		res.append(NEW_ROW) ;		
		appendCellCouple(res, SOURCE_OK_LABEL, nbSourceFilesProcessed, null) ;
		appendCellCouple(res, SOURCE_KO_LABEL, nbSourceFilesFailed,  "red") ;
		
		res.append(NEW_ROW) ;
		appendCellCouple(res, TARGET_OK_LABEL, nbTargetFilesProcessed, null) ;
		appendCellCouple(res, TARGET_KO_LABEL, nbTargetFilesFailed,  "red") ;
		
		res.append(NEW_ROW) ;
		appendCellCouple(res, TOTAL_SIZE_DIFF_LABEL, totalSizeDifference, null) ;
		appendCellCouple(res, SIZE_ABOVE_LIMIT_LABEL, backupWithSizeAboveThreshold, "red") ;
		
		res.append(NEW_ROW) ;
		appendCellCouple(res, HIGH_PERMANENCE_LABEL, nbHighPermanencePath, "red") ;
		appendCellCouple(res, MEDIUM_PERMANENCE_LABEL, nbMediumPermanencePath,  "#ff8f00") ;
		
		if (contentDifferentNb != 0) {
			res.append(NEW_ROW) ;
			appendCellCouple(res, CONTENT_DIFFERENT_LABEL, contentDifferentNb, null) ;
		}
		res.append("</tr></table>") ;
				
	}
	
	private static final String NEW_ROW    = "</tr><tr>" ;
	private static final String HTML_BEGIN = "<html><body>" ;
	private static final String HTML_END   = "</body></html>" ;
	
	public String toHtmlString() {
		StringBuilder res = new StringBuilder() ;
		res.append(HTML_BEGIN) ;
		appendHtmlFragment(res) ;
		res.append(HTML_END) ;
		return res.toString() ;
	}
	
	private void appendCellCouple(StringBuilder res, String label, long value, String color) {
		
		boolean colorPresent = (color != null) && (! color.isEmpty()) && (value > 0) ;
		if (label == null) {
			res.append("<td></td><td></td>") ;
		} else if (colorPresent) {
			res.append("<td bgcolor=")
				.append(color)
				.append(">")
				.append(label)
				.append("</td><td bgcolor=")
				.append(color)
				.append(">")
				.append(value)
				.append("</td>");
		} else {
			res.append("<td>")
				.append(label)
				.append("</td><td>")
				.append(value)
				.append("</td>");
		}
	}
	
	public void add(BackUpCounters counters)  {
		
		copyNewNb 					 = copyNewNb 			 		+ counters.copyNewNb ;
		copyReplaceNb 				 = copyReplaceNb 		 		+ counters.copyReplaceNb ;
		copyTreeNb 					 = copyTreeNb 			 		+ counters.copyTreeNb ;
		deleteNb					 = deleteNb 				 	+ counters.deleteNb ;
		deleteDirNb 				 = deleteDirNb 			 		+ counters.deleteDirNb ;
		ambiguousNb 				 = ambiguousNb 			 		+ counters.ambiguousNb ; 
		contentDifferentNb 			 = contentDifferentNb	 		+ counters.contentDifferentNb ; 
		nbSourceFilesProcessed 		 = nbSourceFilesProcessed 		+ counters.nbSourceFilesProcessed ;
		nbTargetFilesProcessed 		 = nbTargetFilesProcessed 		+ counters.nbTargetFilesProcessed ;
		nbSourceFilesFailed 		 = nbSourceFilesFailed 	 		+ counters.nbSourceFilesFailed ;
		nbTargetFilesFailed 		 = nbTargetFilesFailed 	 		+ counters.nbTargetFilesFailed ;
		backupWithSizeAboveThreshold = backupWithSizeAboveThreshold + counters.backupWithSizeAboveThreshold ;
		nbHighPermanencePath		 = nbHighPermanencePath			+ counters.nbHighPermanencePath ;
		nbMediumPermanencePath		 = nbMediumPermanencePath		+ counters.nbMediumPermanencePath ;
		totalSizeDifference			 = totalSizeDifference			+ counters.totalSizeDifference ;
	}
}
