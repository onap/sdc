package org.openecomp.sdc.be.resources.data;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.be.datatypes.elements.ConsumerDataDefinition;


public class ConsumerDataTest {

	private ConsumerData createTestSubject() {
		return new ConsumerData();
	}

	
	@Test
	public void testGetUniqueIdKey() throws Exception {
		ConsumerData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueIdKey();
	}

	
	@Test
	public void testGetUniqueId() throws Exception {
		ConsumerData testSubject;
		Object result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	
	@Test
	public void testGetConsumerDataDefinition() throws Exception {
		ConsumerData testSubject;
		ConsumerDataDefinition result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerDataDefinition();
	}

	
	@Test
	public void testToGraphMap() throws Exception {
		ConsumerData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	
	@Test
	public void testToString() throws Exception {
		ConsumerData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}