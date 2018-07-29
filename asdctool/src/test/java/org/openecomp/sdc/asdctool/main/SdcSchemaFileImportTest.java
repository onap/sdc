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