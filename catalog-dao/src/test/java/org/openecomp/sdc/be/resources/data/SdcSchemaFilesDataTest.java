package org.openecomp.sdc.be.resources.data;

import java.util.Date;

import javax.annotation.Generated;

import org.junit.Test;


public class SdcSchemaFilesDataTest {

	private SdcSchemaFilesData createTestSubject() {
		return new SdcSchemaFilesData();
	}

	
	@Test
	public void testGetSdcReleaseNum() throws Exception {
		SdcSchemaFilesData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSdcReleaseNum();
	}

	
	@Test
	public void testSetSdcReleaseNum() throws Exception {
		SdcSchemaFilesData testSubject;
		String sdcReleaseNum = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSdcReleaseNum(sdcReleaseNum);
	}

	
	@Test
	public void testGetConformanceLevel() throws Exception {
		SdcSchemaFilesData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConformanceLevel();
	}

	
	@Test
	public void testSetConformanceLevel() throws Exception {
		SdcSchemaFilesData testSubject;
		String conformanceLevel = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setConformanceLevel(conformanceLevel);
	}

	
	@Test
	public void testGetFileName() throws Exception {
		SdcSchemaFilesData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFileName();
	}

	
	@Test
	public void testSetFileName() throws Exception {
		SdcSchemaFilesData testSubject;
		String fileName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setFileName(fileName);
	}

	


	
	@Test
	public void testSetPayloadAsArray() throws Exception {
		SdcSchemaFilesData testSubject;
		byte[] payload = new byte[] { ' ' };

		// test 1
		testSubject = createTestSubject();
		payload = null;
		testSubject.setPayloadAsArray(payload);

		// test 2
		testSubject = createTestSubject();
		payload = new byte[] { ' ' };
		testSubject.setPayloadAsArray(payload);
	}

	
	@Test
	public void testGetPayloadAsArray() throws Exception {
		SdcSchemaFilesData testSubject;
		byte[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getPayloadAsArray();
	}

	
	@Test
	public void testGetTimestamp() throws Exception {
		SdcSchemaFilesData testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp();
	}

	
	@Test
	public void testSetTimestamp() throws Exception {
		SdcSchemaFilesData testSubject;
		Date timestamp = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp(timestamp);
	}

	
	@Test
	public void testGetChecksum() throws Exception {
		SdcSchemaFilesData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getChecksum();
	}

	
	@Test
	public void testSetChecksum() throws Exception {
		SdcSchemaFilesData testSubject;
		String checksum = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setChecksum(checksum);
	}

	
	@Test
	public void testToString() throws Exception {
		SdcSchemaFilesData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}