package org.openecomp.sdc.be.datatypes.elements;

import javax.annotation.Generated;

import org.junit.Assert;
import org.junit.Test;


public class ConsumerDataDefinitionTest {

	private ConsumerDataDefinition createTestSubject() {
		return new ConsumerDataDefinition();
	}

	
	@Test
	public void testGetConsumerName() throws Exception {
		ConsumerDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerName();
	}

	
	@Test
	public void testSetConsumerName() throws Exception {
		ConsumerDataDefinition testSubject;
		String consumerName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setConsumerName(consumerName);
	}

	
	@Test
	public void testGetConsumerPassword() throws Exception {
		ConsumerDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerPassword();
	}

	
	@Test
	public void testSetConsumerPassword() throws Exception {
		ConsumerDataDefinition testSubject;
		String consumerPassword = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setConsumerPassword(consumerPassword);
	}

	
	@Test
	public void testGetConsumerSalt() throws Exception {
		ConsumerDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerSalt();
	}

	
	@Test
	public void testSetConsumerSalt() throws Exception {
		ConsumerDataDefinition testSubject;
		String consumerSalt = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setConsumerSalt(consumerSalt);
	}

	
	@Test
	public void testGetConsumerLastAuthenticationTime() throws Exception {
		ConsumerDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerLastAuthenticationTime();
	}

	
	@Test
	public void testSetConsumerLastAuthenticationTime() throws Exception {
		ConsumerDataDefinition testSubject;
		Long consumerLastAuthenticationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setConsumerLastAuthenticationTime(consumerLastAuthenticationTime);
	}

	
	@Test
	public void testGetConsumerDetailsLastupdatedtime() throws Exception {
		ConsumerDataDefinition testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerDetailsLastupdatedtime();
	}

	
	@Test
	public void testSetConsumerDetailsLastupdatedtime() throws Exception {
		ConsumerDataDefinition testSubject;
		Long consumerDetailsLastupdatedtime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setConsumerDetailsLastupdatedtime(consumerDetailsLastupdatedtime);
	}

	
	@Test
	public void testGetLastModfierAtuid() throws Exception {
		ConsumerDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getLastModfierAtuid();
	}

	
	@Test
	public void testSetLastModfierAtuid() throws Exception {
		ConsumerDataDefinition testSubject;
		String lastModfierAtuid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setLastModfierAtuid(lastModfierAtuid);
	}

	
	@Test
	public void testToString() throws Exception {
		ConsumerDataDefinition testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testHashCode() throws Exception {
		ConsumerDataDefinition testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	
	@Test
	public void testEquals() throws Exception {
		ConsumerDataDefinition testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
	}
}