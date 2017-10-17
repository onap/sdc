package org.openecomp.sdc.be.info;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class ServiceInfoTest {

	private ServiceInfo createTestSubject() {
		return new ServiceInfo("", null);
	}

	
	@Test
	public void testGetName() throws Exception {
		ServiceInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	
	@Test
	public void testSetName() throws Exception {
		ServiceInfo testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	
	@Test
	public void testGetVersions() throws Exception {
		ServiceInfo testSubject;
		List<ServiceVersionInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersions();
	}

	
	@Test
	public void testSetVersions() throws Exception {
		ServiceInfo testSubject;
		List<ServiceVersionInfo> versions = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setVersions(versions);
	}
}