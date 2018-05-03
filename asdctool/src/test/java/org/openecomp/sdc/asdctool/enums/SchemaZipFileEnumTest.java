package org.openecomp.sdc.asdctool.enums;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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

	@Test
	public void setGetFileName_shouldSetCustomFileName() {
		String fileName = "customFileName";
		testSubject.setFileName(fileName);
		assertEquals(fileName, testSubject.getFileName());
	}

	@Test
	public void setGetSourceFolderName_shouldSetCustomSourceFolderName() {
		String sourceFolderName = "customSourceFolderName";
		testSubject.setSourceFolderName(sourceFolderName);
		assertEquals(sourceFolderName, testSubject.getSourceFolderName());
	}

	@Test
	public void setGetSourceFileName_shouldSetCustomSourceFileName() {
		String sourceFileName = "customSourceFileName";
		testSubject.setSourceFileName(sourceFileName);
		assertEquals(sourceFileName, testSubject.getSourceFileName());
	}

	@Test
	public void setGetCollectionTitle_shouldSetCustomCollectionTitle() {
		String collectionTitle = "customCollectionTitle";
		testSubject.setCollectionTitle(collectionTitle);
		assertEquals(collectionTitle, testSubject.getCollectionTitle());
	}

	@Test
	public void setGetImportFileList_shouldSetGetFile1File2() {
		String[] importFileList = new String[] { "File1", "File2" };
		String[] receivedImportFileList;
		testSubject.setImportFileList(importFileList);
		receivedImportFileList = testSubject.getImportFileList();
		assertNotNull(receivedImportFileList);
		assertEquals("File1", receivedImportFileList[0]);
		assertEquals("File2", receivedImportFileList[1]);
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