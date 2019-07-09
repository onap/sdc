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

package org.openecomp.sdc.asdctool.main;

import org.junit.Test;
import org.openecomp.sdc.asdctool.enums.SchemaZipFileEnum;

import java.nio.file.NoSuchFileException;

public class SdcSchemaFileImportTest {

	private SdcSchemaFileImport createTestSubject() {
		return new SdcSchemaFileImport();
	}

	@Test(expected=NoSuchFileException.class)
	public void testCreateAndSaveNodeSchemaFile() throws Exception {

		// default test
		SdcSchemaFileImport.createAndSaveNodeSchemaFile("");
	}

	@Test(expected=NoSuchFileException.class)
	public void testCreateAndSaveNodeSchemaFileOnap() throws Exception {

		// default test
		SdcSchemaFileImport.createAndSaveNodeSchemaFile("onap");
	}

	@Test(expected=NullPointerException.class)
	public void testCreateAndSaveSchemaFileYaml() throws Exception {
		SchemaZipFileEnum schemaZipFileEnum = null;
		Object content = null;

		// default test
		SdcSchemaFileImport.createAndSaveSchemaFileYaml(schemaZipFileEnum, content);
	}

	@Test(expected=IllegalArgumentException.class)
	public void testCreateAndSaveSchemaFileYaml_1() throws Exception {
		String fileName = "";
		String[] importFileList = new String[] { "" };
		String collectionTitle = "";
		Object content = null;

		// default test
		SdcSchemaFileImport.createAndSaveSchemaFileYaml(fileName, importFileList, collectionTitle, content);
	}
}
