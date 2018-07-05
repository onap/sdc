package org.openecomp.sdc.common.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;


public class GeneralUtilityTest {

	@Test
	public void testIsExceedingLimit() {
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
	public void testIsExceedingLimit_1() {
		List<String> strList;
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
}