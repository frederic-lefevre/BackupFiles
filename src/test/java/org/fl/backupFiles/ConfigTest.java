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

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.fl.util.RunningContext;
import org.junit.jupiter.api.Test;

class ConfigTest {

	@Test
	void testRunningContext() throws URISyntaxException {
		
		RunningContext runningContext =Config.getRunningContext();
		
		assertThat(runningContext).isNotNull();
		
		assertThat(runningContext.getName()).isEqualTo(ConfigTest.class.getPackageName());
		
		URL propertyFileLocation = runningContext.getPropertiesLocation();
		assertThat(propertyFileLocation).isNotNull();

		URI propertyFileUri = propertyFileLocation.toURI();
		assertThat(propertyFileUri.isAbsolute()).isTrue();
		Path propertyFilePath = Paths.get(propertyFileUri);
		
		assertThat(propertyFilePath).exists().isRegularFile();
	}
}
