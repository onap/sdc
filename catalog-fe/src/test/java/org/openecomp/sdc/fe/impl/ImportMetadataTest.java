package org.openecomp.sdc.fe.impl;

import org.junit.Test;

public class ImportMetadataTest {

	private ImportMetadata createTestSubject() {
		return new ImportMetadata("", 1234567, "", "", "");
	}

	@Test
	public void testGetName() throws Exception {
		ImportMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		ImportMetadata testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testGetSize() throws Exception {
		ImportMetadata testSubject;
		long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSize();
	}

	@Test
	public void testSetSize() throws Exception {
		ImportMetadata testSubject;
		long size = 1234567;

		// default test
		testSubject = createTestSubject();
		testSubject.setSize(size);
	}

	@Test
	public void testGetMime() throws Exception {
		ImportMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMime();
	}

	@Test
	public void testSetMime() throws Exception {
		ImportMetadata testSubject;
		String mime = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setMime(mime);
	}

	@Test
	public void testGetCreator() throws Exception {
		ImportMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCreator();
	}

	@Test
	public void testSetCreator() throws Exception {
		ImportMetadata testSubject;
		String creator = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCreator(creator);
	}

	@Test
	public void testGetMd5Checksum() throws Exception {
		ImportMetadata testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getMd5Checksum();
	}

	@Test
	public void testSetMd5Checksum() throws Exception {
		ImportMetadata testSubject;
		String md5Checksum = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setMd5Checksum(md5Checksum);
	}
}