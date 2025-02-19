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

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class BackUpJobInformation {

	private final String jobTitle;
	private final String jobEnd;
	private final String jobResult;

	// Comparaison ou Sauvegarde
	private final String jobOperation;

	// To buffer or to target
	private final String jobDirection;

	private static final String dateFrancePattern = " EEEE dd MMMM uuuu à HH:mm:ss";
	
	public BackUpJobInformation(String jt, long je, String jr, String jo, String jd) {
		super();
		
		LocalDateTime date = LocalDateTime.ofInstant(Instant.ofEpochMilli(je), ZoneId.systemDefault());
		jobEnd = date.format(DateTimeFormatter.ofPattern(dateFrancePattern));
		jobResult = jr;
		jobOperation = jo;
		jobDirection = jd;
		jobTitle = jt;
	}

	public String getJobTitle() {
		return jobTitle;
	}

	public String getJobEnd() {
		return jobEnd;
	}

	public String getJobResult() {
		return jobResult;
	}

	public String getJobOperation() {
		return jobOperation;
	}

	public String getJobDirection() {
		return jobDirection;
	}
}
