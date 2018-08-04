package org.openecomp.sdc.common.monitoring;

import org.junit.Test;


public class MonitoringEventTest {

	private MonitoringEvent createTestSubject() {
		return new MonitoringEvent();
	}

	
	@Test
	public void testGetHostid() throws Exception {
		MonitoringEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHostid();
	}

	
	@Test
	public void testSetHostid() throws Exception {
		MonitoringEvent testSubject;
		String hostid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setHostid(hostid);
	}

	
	@Test
	public void testGetHostcpu() throws Exception {
		MonitoringEvent testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHostcpu();
	}

	
	@Test
	public void testSetHostcpu() throws Exception {
		MonitoringEvent testSubject;
		Long hostcpu = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setHostcpu(hostcpu);
	}

	
	@Test
	public void testGetHostmem() throws Exception {
		MonitoringEvent testSubject;
		Double result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHostmem();
	}

	
	@Test
	public void testSetHostmem() throws Exception {
		MonitoringEvent testSubject;
		Double hostmem = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setHostmem(hostmem);
	}

	
	@Test
	public void testGetHostdisk() throws Exception {
		MonitoringEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getHostdisk();
	}

	
	@Test
	public void testSetHostdisk() throws Exception {
		MonitoringEvent testSubject;
		String hostdisk = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setHostdisk(hostdisk);
	}

	
	@Test
	public void testGetJvmid() throws Exception {
		MonitoringEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getJvmid();
	}

	
	@Test
	public void testSetJvmid() throws Exception {
		MonitoringEvent testSubject;
		String jvmid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setJvmid(jvmid);
	}

	
	@Test
	public void testGetJvmcpu() throws Exception {
		MonitoringEvent testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getJvmcpu();
	}

	
	@Test
	public void testSetJvmcpu() throws Exception {
		MonitoringEvent testSubject;
		Long jvmcpu = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setJvmcpu(jvmcpu);
	}

	
	@Test
	public void testGetJvmmem() throws Exception {
		MonitoringEvent testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getJvmmem();
	}

	
	@Test
	public void testSetJvmmem() throws Exception {
		MonitoringEvent testSubject;
		Long jvmmem = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setJvmmem(jvmmem);
	}

	
	@Test
	public void testGetJvmtnum() throws Exception {
		MonitoringEvent testSubject;
		Integer result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getJvmtnum();
	}

	
	@Test
	public void testSetJvmtnum() throws Exception {
		MonitoringEvent testSubject;
		Integer jvmtnum = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setJvmtnum(jvmtnum);
	}

	
	@Test
	public void testGetAppid() throws Exception {
		MonitoringEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAppid();
	}

	
	@Test
	public void testSetAppid() throws Exception {
		MonitoringEvent testSubject;
		String appid = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAppid(appid);
	}

	
	@Test
	public void testGetAppstat() throws Exception {
		MonitoringEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAppstat();
	}

	
	@Test
	public void testSetAppstat() throws Exception {
		MonitoringEvent testSubject;
		String appstat = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAppstat(appstat);
	}

	
	@Test
	public void testToString() throws Exception {
		MonitoringEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}