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

package org.fl.backupFiles.scanner;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.fl.backupFiles.TestUtils;
import org.junit.jupiter.api.Test;

public class PathPairBasicAttributesTest {

	@Test
	void testNullPaths() {
		
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(null, null);
		
		assertThat(pathPairBasicAttributes).isNotNull();
		
		assertThat(pathPairBasicAttributes.noSourcePath()).isTrue();
		assertThat(pathPairBasicAttributes.noTargetPath()).isTrue();
		
		assertThat(pathPairBasicAttributes.getSourcePath()).isNull();
		assertThat(pathPairBasicAttributes.getTargetPath()).isNull();
		
		assertThat(pathPairBasicAttributes.getSourceBasicAttributes()).isNull();
		assertThat(pathPairBasicAttributes.getTargetBasicAttributes()).isNull();
		assertThat(pathPairBasicAttributes.sourceExists()).isFalse();
		assertThat(pathPairBasicAttributes.sourceIsDirectory()).isFalse();
		assertThat(pathPairBasicAttributes.targetExists()).isFalse();
		assertThat(pathPairBasicAttributes.targetIsDirectory()).isFalse();
		
		assertThatNullPointerException().isThrownBy(() -> pathPairBasicAttributes.getSourceSize());
		assertThatNullPointerException().isThrownBy(() -> pathPairBasicAttributes.getTargetSize());
	}
	
	@Test
	void testNullSourcePath() {
		
		Path targetPath = Paths.get("");
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(null, targetPath);
		
		assertThat(pathPairBasicAttributes).isNotNull();
		assertThat(pathPairBasicAttributes.getSourcePath()).isNull();
		assertThat(pathPairBasicAttributes.noSourcePath()).isTrue();
		
		assertThat(pathPairBasicAttributes.noTargetPath()).isFalse();
		
		assertThat(pathPairBasicAttributes.getSourcePath()).isNull();
		assertThat(pathPairBasicAttributes.getTargetPath()).isEqualTo(targetPath);
		
		assertThat(pathPairBasicAttributes.getSourceBasicAttributes()).isNull();
		assertThat(pathPairBasicAttributes.getTargetBasicAttributes()).isNotNull();
		assertThat(pathPairBasicAttributes.sourceExists()).isFalse();
		assertThat(pathPairBasicAttributes.sourceIsDirectory()).isFalse();
		
		assertThatNullPointerException().isThrownBy(() -> pathPairBasicAttributes.getSourceSize());

		assertThat(pathPairBasicAttributes.getTargetSize()).isPositive();
		assertThat(pathPairBasicAttributes.targetExists()).isTrue();
		assertThat(pathPairBasicAttributes.targetIsDirectory()).isTrue();
	}
	
	@Test
	void testNullTargetPath() {
		
		Path sourcePath = Paths.get("");
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(sourcePath, null);
		
		assertThat(pathPairBasicAttributes).isNotNull();
		
		assertThat(pathPairBasicAttributes.noTargetPath()).isTrue();
		
		assertThat(pathPairBasicAttributes.getSourcePath()).isEqualTo(sourcePath);
		assertThat(pathPairBasicAttributes.getTargetPath()).isNull();
		
		// All other operation on target should raise a NPE
		assertThat(pathPairBasicAttributes.getSourceBasicAttributes()).isNotNull();
		assertThat(pathPairBasicAttributes.getTargetBasicAttributes()).isNull();
		
		assertThat(pathPairBasicAttributes.getSourceSize()).isPositive();
		assertThat(pathPairBasicAttributes.sourceExists()).isTrue();
		assertThat(pathPairBasicAttributes.sourceIsDirectory()).isTrue();
		
		assertThat(pathPairBasicAttributes.targetExists()).isFalse();
		assertThat(pathPairBasicAttributes.targetIsDirectory()).isFalse();
		assertThatNullPointerException().isThrownBy(() -> pathPairBasicAttributes.getTargetSize());
	}
	
	@Test
	void testEmptyPaths() {

		Path sourcePath = Paths.get("");
		Path targetPath = Paths.get("");
		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(sourcePath, targetPath);
		
		assertThat(pathPairBasicAttributes).isNotNull();
		
		assertThat(pathPairBasicAttributes.getSourcePath()).isEqualTo(sourcePath);
		assertThat(pathPairBasicAttributes.getTargetPath()).isEqualTo(targetPath);
		
		assertThat(pathPairBasicAttributes.noTargetPath()).isFalse();
		
		assertThat(pathPairBasicAttributes.sourceExists()).isTrue();
		assertThat(pathPairBasicAttributes.targetExists()).isTrue();
		
		assertThat(pathPairBasicAttributes.sourceIsDirectory()).isTrue();
		assertThat(pathPairBasicAttributes.targetIsDirectory()).isTrue();
		
		assertThat(pathPairBasicAttributes.getSourceSize()).isPositive();
		assertThat(pathPairBasicAttributes.getTargetSize()).isPositive();
	}
	
	private static final String EXISTANT_FOLDER = "file:///ForTests/BackUpFiles/TestDir1/";
	private static final String EXISTANT_FILE = EXISTANT_FOLDER + "File1.pdf";
	private static final String UNEXISTANT_FOLDER = "file:///ForTests/BackUpFiles/doesNotExists";
	private static final String UNEXISTANT_FILE = EXISTANT_FOLDER + "doesNotExists.pdf";

	private static final Path EXISTANT_FOLDER_PATH = TestUtils.getPathFromUriString(EXISTANT_FOLDER);
	private static final Path EXISTANT_FILE_PATH = TestUtils.getPathFromUriString(EXISTANT_FILE);
	private static final Path UNEXISTANT_FOLDER_PATH = TestUtils.getPathFromUriString(UNEXISTANT_FOLDER);
	private static final Path UNEXISTANT_FILE_PATH = TestUtils.getPathFromUriString(UNEXISTANT_FILE);

	
	@Test
	void testUnexistantTargetFile() {

		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(EXISTANT_FILE_PATH, UNEXISTANT_FILE_PATH);
		
		assertThat(pathPairBasicAttributes).isNotNull();
		
		assertThat(pathPairBasicAttributes.getSourcePath()).isEqualTo(EXISTANT_FILE_PATH);
		assertThat(pathPairBasicAttributes.getTargetPath()).isEqualTo(UNEXISTANT_FILE_PATH);
		
		assertThat(pathPairBasicAttributes.noTargetPath()).isFalse();
		
		assertThat(pathPairBasicAttributes.getSourceBasicAttributes()).isNotNull();
		assertThat(pathPairBasicAttributes.getTargetBasicAttributes()).isNull();
		
		assertThat(pathPairBasicAttributes.sourceExists()).isTrue();
		assertThat(pathPairBasicAttributes.targetExists()).isFalse();
		
		assertThat(pathPairBasicAttributes.sourceIsDirectory()).isFalse();
		assertThat(pathPairBasicAttributes.targetIsDirectory()).isFalse();
		
		assertThat(pathPairBasicAttributes.getSourceSize()).isPositive();
		
		// NPE on getSize if it does not exists
		assertThatNullPointerException().isThrownBy(() -> pathPairBasicAttributes.getTargetSize());
	}
	
	@Test
	void testUnexistantSourceFolder() {

		PathPairBasicAttributes pathPairBasicAttributes = new PathPairBasicAttributes(UNEXISTANT_FOLDER_PATH, EXISTANT_FOLDER_PATH);
		
		assertThat(pathPairBasicAttributes).isNotNull();
		
		assertThat(pathPairBasicAttributes.getSourcePath()).isEqualTo(UNEXISTANT_FOLDER_PATH);
		assertThat(pathPairBasicAttributes.getTargetPath()).isEqualTo(EXISTANT_FOLDER_PATH);
		
		assertThat(pathPairBasicAttributes.noTargetPath()).isFalse();
		
		assertThat(pathPairBasicAttributes.getSourceBasicAttributes()).isNull();
		assertThat(pathPairBasicAttributes.getTargetBasicAttributes()).isNotNull();
		
		assertThat(pathPairBasicAttributes.sourceExists()).isFalse();
		assertThat(pathPairBasicAttributes.targetExists()).isTrue();
		
		assertThat(pathPairBasicAttributes.sourceIsDirectory()).isFalse();
		assertThat(pathPairBasicAttributes.targetIsDirectory()).isTrue();
		
		// NPE on getSize if it does not exists
		assertThatNullPointerException().isThrownBy(() -> pathPairBasicAttributes.getSourceSize());
		
		assertThat(pathPairBasicAttributes.getTargetSize()).isZero();
	}
}
