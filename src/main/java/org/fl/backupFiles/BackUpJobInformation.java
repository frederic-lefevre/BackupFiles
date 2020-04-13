package org.fl.backupFiles;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BackUpJobInformation {

	private final String jobTitle ;
	private final String jobEnd ;
	private final String jobResult ;
	
	// Comparaison ou Sauvegarde
	private final String jobOperation ;
	
	// To buffer or to target
	private final String jobDirection ;
	
	private static final String dateFrancePattern = " EEEE dd MMMM uuuu à HH:mm:ss" ;
	
	public BackUpJobInformation(String jt, long je, String jr, String jo, String jd) {
		super();
		
		LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(je), ZoneId.systemDefault());
		jobEnd 		 = date.format(DateTimeFormatter.ofPattern(dateFrancePattern)) ;
		jobResult 	 = jr ;
		jobOperation = jo ;
		jobDirection = jd ;
		jobTitle 	 = jt ;
	}

	public String getJobTitle() 	{ return jobTitle;		}
	public String getJobEnd() 		{ return jobEnd;		}
	public String getJobResult() 	{ return jobResult;		}
	public String getJobOperation() { return jobOperation;	}
	public String getJobDirection() { return jobDirection;	}
}
