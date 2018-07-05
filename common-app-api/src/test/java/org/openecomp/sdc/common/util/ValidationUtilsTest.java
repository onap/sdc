package org.openecomp.sdc.common.util;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;


public class ValidationUtilsTest {

	@Test
	public void testNormalizeCategoryName4Display() {
		String str = "";
		String result;

		// test 1
		str = "123";
		result = ValidationUtils.normalizeCategoryName4Display(str);
		Assert.assertEquals("123", result);

		// test 2
		str = "123#123";
		result = ValidationUtils.normalizeCategoryName4Display(str);
		Assert.assertEquals("123#123", result);
	}

	@Test
	public void testValidateStringNotEmpty() {
		String value = "";
		boolean result;

		// test 1
		value = null;
		result = ValidationUtils.validateStringNotEmpty(value);
		Assert.assertEquals(false, result);

		// test 2
		value = "";
		result = ValidationUtils.validateStringNotEmpty(value);
		Assert.assertEquals(false, result);
	}

	
	@Test
	public void testValidateListNotEmpty() {
		List<?> list = null;
		boolean result;

		// test 1
		list = null;
		result = ValidationUtils.validateListNotEmpty(list);
		Assert.assertEquals(false, result);
	}
}