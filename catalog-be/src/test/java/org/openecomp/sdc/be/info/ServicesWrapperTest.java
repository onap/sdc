package org.openecomp.sdc.be.info;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class ServicesWrapperTest {

	private ServicesWrapper createTestSubject() {
		return new ServicesWrapper();
	}

	
	@Test
	public void testGetServices() throws Exception {
		ServicesWrapper testSubject;
		List<ServiceInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServices();
	}

	
	@Test
	public void testSetServices() throws Exception {
		ServicesWrapper testSubject;
		List<ServiceInfo> services = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setServices(services);
	}
}