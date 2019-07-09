/*-
 * ============LICENSE_START=======================================================
 * SDC
 * ================================================================================
 * Copyright (C) 2019 AT&T Intellectual Property. All rights reserved.
 * ================================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * ============LICENSE_END=========================================================
 */

package org.openecomp.sdc.asdctool.enums;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SchemaZipFileEnumTest {

	private SchemaZipFileEnum testSubject;
	private String result;

	@Before
	public void setUp() {
		testSubject = createTestSubject();
	}

	@After
	public void tearDown() {
		testSubject = null;
		result = null;
	}


	private SchemaZipFileEnum createTestSubject() {
		return SchemaZipFileEnum.DATA;
	}

	@Test
	public void testGetFileName() throws Exception {
		SchemaZipFileEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFileName();
	}

	@Test
	public void testGetSourceFolderName() throws Exception {
		SchemaZipFileEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSourceFolderName();
	}


	@Test
	public void testGetSourceFileName() throws Exception {
		SchemaZipFileEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSourceFileName();
	}

	@Test
	public void testGetCollectionTitle() throws Exception {
		SchemaZipFileEnum testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCollectionTitle();
	}

	@Test
	public void testGetImportFileList() throws Exception {
		SchemaZipFileEnum testSubject;
		String[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getImportFileList();
	}

}
