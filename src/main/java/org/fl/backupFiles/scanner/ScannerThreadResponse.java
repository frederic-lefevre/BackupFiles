/*
 * MIT License

Copyright (c) 2017, 2023 Frederic Lefevre

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

package org.fl.backupFiles.scanner;

import java.nio.file.Path;
import java.util.List;

import org.fl.backupFiles.BackUpCounters;
import org.fl.backupFiles.BackUpItemList;
import org.fl.backupFiles.BackUpTask;

public class ScannerThreadResponse {

	private final BackUpTask backUpTask;
	private final List<Path> filesVisitFailed;

	private final BackUpItemList backUpItemList;
	private final BackUpCounters backUpCounters;
	private final String status;

	private boolean hasNotBeenProcessed;

	public ScannerThreadResponse(BackUpTask but, BackUpItemList bil, BackUpCounters buc, List<Path> fvf, String st) {

		backUpTask = but;
		backUpItemList = bil;
		backUpCounters = buc;
		filesVisitFailed = fvf;
		status = st;

		hasNotBeenProcessed = true;
	}

	public BackUpTask getBackUpTask() {
		return backUpTask;
	}

	public List<Path> getFilesVisitFailed() {
		return filesVisitFailed;
	}

	public BackUpItemList getBackUpItemList() {
		return backUpItemList;
	}

	public BackUpCounters getBackUpCounters() {
		return backUpCounters;
	}

	public String getStatus() {
		return status;
	}

	public boolean hasNotBeenProcessed() {
		return hasNotBeenProcessed;
	}

	public void setHasNotBeenProcessed(boolean hasNotBeenProcessed) {
		this.hasNotBeenProcessed = hasNotBeenProcessed;
	}
}
