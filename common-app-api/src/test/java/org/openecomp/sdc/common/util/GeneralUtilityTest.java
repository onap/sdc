package org.openecomp.sdc.common.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class GeneralUtilityTest {

	private GeneralUtility createTestSubject() {
		return new GeneralUtility();
	}

	
	@Test
	public void testGenerateTextFile() throws Exception {
		String fileName = "";
		String fileData = "";
		boolean result;

		// default test
		result = GeneralUtility.generateTextFile(fileName, fileData);
	}

	
	@Test
	public void testIsBase64Encoded() throws Exception {
		byte[] data = new byte[] { ' ' };
		boolean result;

		// default test
		result = GeneralUtility.isBase64Encoded(data);
	}

	
	@Test
	public void testIsBase64Encoded_1() throws Exception {
		String str = "";
		boolean result;

		// default test
		result = GeneralUtility.isBase64Encoded(str);
	}

	
	@Test
	public void testIsExceedingLimit() throws Exception {
		String str = "";
		int limit = 0;
		boolean result;

		// test 1
		str = null;
		result = GeneralUtility.isExceedingLimit(str, limit);
		Assert.assertEquals(false, result);

		// test 2
		str = "";
		result = GeneralUtility.isExceedingLimit(str, limit);
		Assert.assertEquals(false, result);
	}

	
	@Test
	public void testIsExceedingLimit_1() throws Exception {
		List<String> strList = null;
		int limit = 0;
		int delimiterLength = 0;
		boolean result;

		// test 1
		strList = null;
		result = GeneralUtility.isExceedingLimit(strList, limit, delimiterLength);
		Assert.assertEquals(false, result);
	}

	
	@Test
	public void testGetFilenameExtension() throws Exception {
		String fileName = "";
		String result;

		// test 1
		fileName = null;
		result = GeneralUtility.getFilenameExtension(fileName);
		Assert.assertEquals("", result);

		// test 2
		fileName = "";
		result = GeneralUtility.getFilenameExtension(fileName);
		Assert.assertEquals("", result);
	}

	
	@Test
	public void testCalculateMD5Base64EncodedByByteArray() throws Exception {
		byte[] payload = new byte[] { ' ' };
		String result;

		// default test
		result = GeneralUtility.calculateMD5Base64EncodedByByteArray(payload);
	}

	
	@Test
	public void testCalculateMD5Base64EncodedByString() throws Exception {
		String data = "";
		String result;

		// default test
		result = GeneralUtility.calculateMD5Base64EncodedByString(data);
	}

	
	@Test
	public void testIsEmptyString() throws Exception {
		String str = "";
		boolean result;

		// default test
		result = GeneralUtility.isEmptyString(str);
	}
}