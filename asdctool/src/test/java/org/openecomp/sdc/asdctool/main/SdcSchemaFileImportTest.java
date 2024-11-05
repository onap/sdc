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

import org.junit.jupiter.api.Test;
import org.openecomp.sdc.asdctool.enums.SchemaZipFileEnum;

import java.nio.file.NoSuchFileException;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SdcSchemaFileImportTest {

	private SdcSchemaFileImport createTestSubject() {
		return new SdcSchemaFileImport();
	}

	@Test
	public void testCreateAndSaveNodeSchemaFile() {
        assertThrows(NoSuchFileException.class, () ->

            // default test
            SdcSchemaFileImport.createAndSaveNodeSchemaFile(""));
    }

	@Test
	public void testCreateAndSaveNodeSchemaFileOnap() {
        assertThrows(NoSuchFileException.class, () ->

            // default test
            SdcSchemaFileImport.createAndSaveNodeSchemaFile("onap"));
    }

	@Test
	public void testCreateAndSaveSchemaFileYaml() {
        assertThrows(NullPointerException.class, () -> {
            SchemaZipFileEnum schemaZipFileEnum = null;
            Object content = null;

            // default test
            SdcSchemaFileImport.createAndSaveSchemaFileYaml(schemaZipFileEnum, content);
        });
    }

	@Test
	public void testCreateAndSaveSchemaFileYaml_1() {
        assertThrows(IllegalArgumentException.class, () -> {
            String fileName = "";
            String[] importFileList = new String[]{""};
            String collectionTitle = "";
            Object content = null;

            // default test
            SdcSchemaFileImport.createAndSaveSchemaFileYaml(fileName, importFileList, collectionTitle, content);
        });
    }
}
