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

package org.fl.backupFiles;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.logging.Logger;

public class BackUpTask {

	private static final Logger bLog = Config.getLogger();
	
	private final Path source;
	private final Path target;
	
	private boolean compareContent;
	private boolean compareContentOnAmbiguous;
	
	private final static String warning1  = "  Attention : les chemins source et cible n'existent pas";
	private final static String warning2  = "  Attention : le chemin source n'existe pas";
	private final static String warning3  = "  Attention : le chemin cible n'existe pas";
	private final static String noWarning = "" ;
	
	// A back up task is a source directory or file to back up to a destination directory or file
	public BackUpTask(Path src, Path tgt) {
		
		source = src;
		target = tgt;
		
		if (source == null) {
			bLog.severe("source path null when creating back up task") ;
		}		
		compareContent  = false ;
		compareContentOnAmbiguous = true;
	}

	public Path getSource() {
		return source;
	}

	public Path getTarget() {
		return target;
	}

	public String toString() {
		String toString ;
		if ((source != null) && (target != null)) {
			toString = source.toString() + " ==> " + target.toString() ; 
		} else {
			toString = null ;
		}
		return toString ;
	}
	
	public String eventualWarning() {
		
		String warning = null ;
		if ((source != null) && (target != null)) {
			boolean sourceExists = Files.exists(source) ;
			boolean targetExists = Files.exists(target) ;
			if (!sourceExists && !targetExists) {
				warning = warning1 ;
			} else if (!sourceExists) {
				warning = warning2 ;
			} else if (!targetExists) {
				warning = warning3 ;
			} else {
				warning = noWarning ;
			}
		}
		return warning ;
	}

	public boolean compareContent() {
		return compareContent;
	}

	public void setCompareContent(boolean compareContent) {
		this.compareContent = compareContent;
	}

	public boolean compareContentOnAmbiguous() {
		return compareContentOnAmbiguous;
	}

	public void setCompareContentOnAmbiguous(boolean compareContentOnAmbiguous) {
		this.compareContentOnAmbiguous = compareContentOnAmbiguous;
	}
	
	// Hashcode with lazy init
	private int hashcode = 0 ;
	
	@Override
	public int hashCode() {
		
		int result = hashcode ;
		
		if (result == 0) {
			final int prime = 31 ;
			result = 1 ;
			result = prime * result + ((source == null) ? 0 : source.hashCode()) ;
			result = prime * result + ((target == null) ? 0 : target.hashCode()) ;
		}
		return result ;
	}

	@Override
	public boolean equals(Object obj) {
		
		if (this == obj) 				  	return true  ;
		if (obj == null) 				  	return false ;		
		if (!(obj instanceof BackUpTask)) 	return false ;
		
		BackUpTask other = (BackUpTask) obj;
		if (source == null) {
			if (other.source != null) 		return false ;
		} else if (!source.equals(other.source)) {
			return false;
		}
		if (target == null) {
			if (other.target != null)		return false ;
		} else if (!target.equals(other.target)) {
			return false;
		}
		return true;
	}

}
