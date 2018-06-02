package org.openecomp.sdc.be.dao.utils;

import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Assert;
import org.junit.Test;

public class DaoUtilsTest {

	@Test
	public void testConvertToJson() throws Exception {
		Object object = new Object();
		String result;

		// test 1
		result = DaoUtils.convertToJson(object);
		Assert.assertEquals("{}", result);
		
		try {
			result = DaoUtils.convertToJson(null);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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