package org.openecomp.sdc.asdctool.enums;

import javax.annotation.Generated;

import org.junit.Test;


public class SchemaZipFileEnumTest {

	private SchemaZipFileEnum createTestSubject() {
		return  SchemaZipFileEnum.DATA;
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
	public void testSetFileName() throws Exception {
		SchemaZipFileEnum testSubject;
		String fileName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setFileName(fileName);
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
	public void testSetSourceFolderName() throws Exception {
		SchemaZipFileEnum testSubject;
		String sourceFolderName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSourceFolderName(sourceFolderName);
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
	public void testSetSourceFileName() throws Exception {
		SchemaZipFileEnum testSubject;
		String sourceFileName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSourceFileName(sourceFileName);
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
	public void testSetCollectionTitle() throws Exception {
		SchemaZipFileEnum testSubject;
		String collectionTitle = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCollectionTitle(collectionTitle);
	}

	
	@Test
	public void testGetImportFileList() throws Exception {
		SchemaZipFileEnum testSubject;
		String[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getImportFileList();
	}

	
	@Test
	public void testSetImportFileList() throws Exception {
		SchemaZipFileEnum testSubject;
		String[] importFileList = new String[] { "" };

		// default test
		testSubject = createTestSubject();
		testSubject.setImportFileList(importFileList);
	}
}