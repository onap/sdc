package org.openecomp.sdc.asdctool.main;

import java.nio.file.NoSuchFileException;

import org.junit.Test;
import org.openecomp.sdc.asdctool.enums.SchemaZipFileEnum;

public class SdcSchemaFileImportTest {

	private SdcSchemaFileImport createTestSubject() {
		return new SdcSchemaFileImport();
	}

	@Test(expected=NoSuchFileException.class)
	public void testCreateAndSaveNodeSchemaFile() throws Exception {

		// default test
		SdcSchemaFileImport.createAndSaveNodeSchemaFile();
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