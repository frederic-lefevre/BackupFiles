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
	
	public BackUpCounters() {
		reset() ;
	}

	public void reset() {
		copyNewNb 				= 0 ;
		copyReplaceNb 			= 0 ;
		copyTreeNb 				= 0 ;
		deleteNb				= 0 ;
		deleteDirNb 			= 0 ;
		ambiguousNb 			= 0 ; 
		contentDifferentNb		= 0 ;
		nbSourceFilesProcessed 	= 0 ;
		nbTargetFilesProcessed 	= 0 ;
		nbSourceFilesFailed 	= 0 ;
		nbTargetFilesFailed 	= 0 ;
	}
	
	public String toString() {
		
		StringBuilder res = new StringBuilder() ;
		res.append(COPY_NEW_LABEL).append(copyNewNb).append(DELETE_LABEL).append(deleteNb).append("\n") ;
		res.append(COPY_REPLACE_LABEL).append(copyReplaceNb).append(DELETE_DIR_LABEL).append(deleteDirNb).append("\n") ;
		res.append(COPY_TREE_LABEL).append(copyTreeNb).append(AMBIGUOUS_LABEL).append(ambiguousNb).append("\n") ;
		
		res.append(SOURCE_FILE_PROCESSED_LABEL).append(nbSourceFilesProcessed).append(SOURCE_FILE_FAILED_LABEL).append(nbSourceFilesFailed).append("\n") ;
		res.append(TARGET_FILE_PROCESSED_LABEL).append(nbTargetFilesProcessed).append(TARGET_FILE_FAILED_LABEL).append(nbTargetFilesFailed).append("\n") ;
		
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
		
		if (contentDifferentNb != 0) {
			res.append("<tr><td>").append(CONTENT_DIFFERENT_LABEL).append("</td><td>").append(contentDifferentNb).append("</td><td></td><td></td></tr></table>") ;
		} else {
			res.append("</table>") ;
		}
		
	}
	
	public void add(BackUpCounters counters)  {
		
		copyNewNb 				= copyNewNb 			 + counters.copyNewNb ;
		copyReplaceNb 			= copyReplaceNb 		 + counters.copyReplaceNb ;
		copyTreeNb 				= copyTreeNb 			 + counters.copyTreeNb ;
		deleteNb				= deleteNb 				 + counters.deleteNb ;
		deleteDirNb 			= deleteDirNb 			 + counters.deleteDirNb ;
		ambiguousNb 			= ambiguousNb 			 + counters.ambiguousNb ; 
		contentDifferentNb 		= contentDifferentNb	 + counters.contentDifferentNb ; 
		nbSourceFilesProcessed 	= nbSourceFilesProcessed + counters.nbSourceFilesProcessed ;
		nbTargetFilesProcessed 	= nbTargetFilesProcessed + counters.nbTargetFilesProcessed ;
		nbSourceFilesFailed 	= nbSourceFilesFailed 	 + counters.nbSourceFilesFailed ;
		nbTargetFilesFailed 	= nbTargetFilesFailed 	 + counters.nbTargetFilesFailed ;
	}
}
