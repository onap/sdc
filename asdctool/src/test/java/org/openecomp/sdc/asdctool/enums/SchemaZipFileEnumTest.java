package org.openecomp.sdc.asdctool.enums;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


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
		receivedImportFileList =  testSubject.getImportFileList();
		assertNotNull(receivedImportFileList);
		assertEquals("File1", receivedImportFileList[0]);
		assertEquals("File2", receivedImportFileList[1]);
	}

	private SchemaZipFileEnum createTestSubject() {
		return  SchemaZipFileEnum.DATA;
	}
}