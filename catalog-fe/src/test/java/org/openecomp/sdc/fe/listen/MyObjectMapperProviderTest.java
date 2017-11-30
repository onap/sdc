package org.openecomp.sdc.fe.listen;

import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;


public class MyObjectMapperProviderTest {

	private MyObjectMapperProvider createTestSubject() {
		return new MyObjectMapperProvider();
	}

	
	@Test
	public void testGetContext() throws Exception {
		MyObjectMapperProvider testSubject;
		Class<?> type = null;
		ObjectMapper result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getContext(type);
	}

	

}