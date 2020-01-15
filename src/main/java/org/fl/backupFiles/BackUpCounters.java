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
		return res.toString() ;
	}
	
	public void appendHtmlFragment(StringBuilder res) {
		
		res.append("<table>") ;
		appendRow(res, COPY_NEW_LABEL, 	   copyNewNb, 	  		   DELETE_LABEL, 	 deleteNb, 	  		  null, null) ;
		appendRow(res, COPY_REPLACE_LABEL, copyReplaceNb, 		   DELETE_DIR_LABEL, deleteDirNb, 		  null, null) ;
		appendRow(res, COPY_TREE_LABEL,    copyTreeNb, 	  		   AMBIGUOUS_LABEL,  ambiguousNb, 		  null, null) ;
		appendRow(res, SOURCE_OK_LABEL,    nbSourceFilesProcessed, SOURCE_KO_LABEL,  nbSourceFilesFailed, null, "red") ;
		appendRow(res, TARGET_OK_LABEL,    nbTargetFilesProcessed, TARGET_KO_LABEL,  nbTargetFilesFailed, null, "red") ;
		
		appendRow(res, SIZE_ABOVE_LIMIT_LABEL, backupWithSizeAboveThreshold, "", 0, "red", null) ;
					
		appendRow(res, HIGH_PERMANENCE_LABEL, nbHighPermanencePath, MEDIUM_PERMANENCE_LABEL, nbMediumPermanencePath, "red", "#ff8f00") ;
		
		if (contentDifferentNb != 0) {
			appendRow(res, CONTENT_DIFFERENT_LABEL, contentDifferentNb, "", 0, null, null) ;
		} 
		res.append("</table>") ;				
	}
	
	private void appendRow(StringBuilder res, String label1, long value1, String label2, long value2, String color1, String color2) {
		
		boolean color1present = (color1 != null) && (! color1.isEmpty()) && (value1 > 0) ;
		boolean color2present = (color2 != null) && (! color2.isEmpty()) && (value2 > 0) ;
		
		res.append("<tr><td") ;
		if (color1present) res.append(" bgcolor=").append(color1) ;
		res.append(">").append(label1).append("</td><td") ;
		if (color1present) res.append(" bgcolor=").append(color1) ;
		res.append(">").append(value1).append("</td><td") ;
		if (color2present) res.append(" bgcolor=").append(color2) ;
		res.append(">") ;
		if (label2 != null) res.append(label2) ;
		res.append("</td><td") ;
		if (color2present) res.append(" bgcolor=").append(color2) ;
		res.append(">") ;
		if (label2 != null) res.append(value2) ;
		res.append("</td></tr>") ;
		
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
	}
}
