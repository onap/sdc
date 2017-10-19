package org.openecomp.sdc.common.api;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckComponent;
import org.openecomp.sdc.common.api.HealthCheckInfo.HealthCheckStatus;


public class HealthCheckInfoTest {

	private HealthCheckInfo createTestSubject() {
		return new HealthCheckInfo(null, null, "", "");
	}

	
	@Test
	public void testGetHealthCheckComponent() throws Exception {
		HealthCheckInfo testSubject;
		HealthCheckComponent result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHealthCheckComponent();
	}

	
	@Test
	public void testGetHealthCheckStatus() throws Exception {
		HealthCheckInfo testSubject;
		HealthCheckStatus result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHealthCheckStatus();
	}

	
	@Test
	public void testGetComponentsInfo() throws Exception {
		HealthCheckInfo testSubject;
		List<HealthCheckInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getComponentsInfo();
	}

	
	@Test
	public void testSetComponentsInfo() throws Exception {
		HealthCheckInfo testSubject;
		List<HealthCheckInfo> componentsInfo = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setComponentsInfo(componentsInfo);
	}

	
	@Test
	public void testGetVersion() throws Exception {
		HealthCheckInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getVersion();
	}

	
	@Test
	public void testSetVersion() throws Exception {
		HealthCheckInfo testSubject;
		String version = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setVersion(version);
	}

	
	@Test
	public void testGetDescription() throws Exception {
		HealthCheckInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	
	@Test
	public void testToString() throws Exception {
		HealthCheckInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	
	@Test
	public void testMain() throws Exception {
		String[] args = new String[] { "" };

		// default test
		HealthCheckInfo.main(args);
	}
}