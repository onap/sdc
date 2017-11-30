package org.openecomp.sdc.be.info;

import java.util.List;

import org.junit.Test;

public class DistributionStatusListResponseTest {

	private DistributionStatusListResponse createTestSubject() {
		return new DistributionStatusListResponse();
	}

	
	@Test
	public void testGetDistributionStatusList() throws Exception {
		DistributionStatusListResponse testSubject;
		List<DistributionStatusInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionStatusList();
	}

	
	@Test
	public void testSetDistributionStatusList() throws Exception {
		DistributionStatusListResponse testSubject;
		List<DistributionStatusInfo> distribStatusInfoList = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionStatusList(distribStatusInfoList);
	}
}