package org.openecomp.sdc.be.dao.utils;

import org.junit.Assert;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class DaoUtilsTest {

	@Test
	public void testConvertToJson() throws Exception {
		Object object = new Object();
		String result;

		// test 1
		result = DaoUtils.convertToJson(object);
		Assert.assertEquals("{}", result);
		
		assertThatThrownBy(()->DaoUtils.convertToJson(null)).isInstanceOf(RuntimeException.class);
	}

	@Test
	public void testConvertFromJson() throws Exception {
		Class clazz = Object.class;
		String json = null;
		Object result;

		// default test
		result = DaoUtils.convertFromJson(clazz, json);
		Assert.assertEquals(null, result);
		
		try {
			result = DaoUtils.convertFromJson(null, json);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}