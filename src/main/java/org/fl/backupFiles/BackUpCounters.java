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
	
	public final static String SOURCE_FILE_PROCESSED_LABEL 	= "  Eléments source traités:   " ;
	public final static String SOURCE_FILE_FAILED_LABEL 	= "  Eléments source en erreur: " ;
	public final static String TARGET_FILE_PROCESSED_LABEL 	= "  Eléments target traités:   " ;
	public final static String TARGET_FILE_FAILED_LABEL 	= "  Eléments target en erreur: " ;
	
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
		
		res.append(SOURCE_FILE_PROCESSED_LABEL).append(nbSourceFilesProcessed).append(SOURCE_FILE_FAILED_LABEL).append(nbSourceFilesFailed).append("\n") ;
		res.append(TARGET_FILE_PROCESSED_LABEL).append(nbTargetFilesProcessed).append(TARGET_FILE_FAILED_LABEL).append(nbTargetFilesFailed).append("\n") ;
		
		res.append(SIZE_ABOVE_LIMIT_LABEL).append(backupWithSizeAboveThreshold).append("\n") ;
		res.append(HIGH_PERMANENCE_LABEL).append(nbHighPermanencePath).append(MEDIUM_PERMANENCE_LABEL).append(nbMediumPermanencePath).append("\n") ;
		
		if (contentDifferentNb != 0) {
			res.append(CONTENT_DIFFERENT_LABEL).append(contentDifferentNb) ;
		}
		return res.toString() ;
	}
	
	public void appendHtmlFragment(StringBuilder res) {
		
		res.append("<table><tr><td>") ;
		res.append(COPY_NEW_LABEL).append("</td><td>").append(copyNewNb).append("</td><td>").append(DELETE_LABEL).append("</td><td>").append(deleteNb).append("</td></tr>") ;
		res.append("<tr><td>").append(COPY_REPLACE_LABEL).append("</td><td>").append(copyReplaceNb).append("</td><td>").append(DELETE_DIR_LABEL).append("</td><td>").append(deleteDirNb).append("</td></tr>") ;
		res.append("<tr><td>").append(COPY_TREE_LABEL).append("</td><td>").append(copyTreeNb).append("</td><td>").append(AMBIGUOUS_LABEL).append("</td><td>").append(ambiguousNb).append("</td></tr>") ;
		
		res.append("<tr><td>").append(SOURCE_FILE_PROCESSED_LABEL).append("</td><td>").append(nbSourceFilesProcessed).append("</td><td>").append(SOURCE_FILE_FAILED_LABEL).append("</td><td>").append(nbSourceFilesFailed).append("</td></tr>") ;
		res.append("<tr><td>").append(TARGET_FILE_PROCESSED_LABEL).append("</td><td>").append(nbTargetFilesProcessed).append("</td><td>").append(TARGET_FILE_FAILED_LABEL).append("</td><td>").append(nbTargetFilesFailed).append("</td></tr>") ;
		
		String rowStart ;
		String cellStart ;
		if (backupWithSizeAboveThreshold > 0) {
			rowStart = "<tr><td bgcolor=red>" ;
			cellStart = "</td><td bgcolor=red>" ;
		} else {
			rowStart = "<tr><td>" ;
			cellStart = "</td><td>" ;
		}
		res.append(rowStart).append(SIZE_ABOVE_LIMIT_LABEL).append(cellStart).append(backupWithSizeAboveThreshold).append("</td><td></td><td></td></tr>") ;
				
		if (nbHighPermanencePath > 0) {
			rowStart = "<tr><td bgcolor=red>" ;
			cellStart = "</td><td bgcolor=red>" ;
		} else {
			rowStart = "<tr><td>" ;
			cellStart = "</td><td>" ;
		}
		res.append(rowStart).append(HIGH_PERMANENCE_LABEL).append(cellStart).append(nbHighPermanencePath) ;
		
		if (nbMediumPermanencePath > 0) {
			cellStart = "</td><td bgcolor=#ff8f00>" ;
		} else {
			cellStart = "</td><td>" ;
		}
		res.append(cellStart).append(MEDIUM_PERMANENCE_LABEL).append(cellStart).append(nbMediumPermanencePath).append("</td></tr>") ;		
		
		if (contentDifferentNb != 0) {
			res.append("<tr><td>").append(CONTENT_DIFFERENT_LABEL).append("</td><td>").append(contentDifferentNb).append("</td><td></td><td></td></tr></table>") ;
		} else {
			res.append("</table>") ;
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
	}
}
