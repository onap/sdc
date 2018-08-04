package org.openecomp.sdc.common.util;

import org.junit.Test;

import com.google.gson.Gson;


public class GsonFactoryTest {

	private GsonFactory createTestSubject() {
		return new GsonFactory();
	}

	
	@Test
	public void testGetGson() throws Exception {
		Gson result;

		// default test
		result = GsonFactory.getGson();
	}
}