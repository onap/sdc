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