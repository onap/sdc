package org.openecomp.sdc.be.info;

import java.util.List;

import javax.annotation.Generated;

import org.junit.Test;


public class DistributionStatusOfServiceListResponceTest {

	private DistributionStatusOfServiceListResponce createTestSubject() {
		return new DistributionStatusOfServiceListResponce();
	}

	
	@Test
	public void testGetDistributionStatusOfServiceList() throws Exception {
		DistributionStatusOfServiceListResponce testSubject;
		List<DistributionStatusOfServiceInfo> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDistributionStatusOfServiceList();
	}

	
	@Test
	public void testSetDistributionStatusOfServiceList() throws Exception {
		DistributionStatusOfServiceListResponce testSubject;
		List<DistributionStatusOfServiceInfo> distribStatusOfServiceInfoList = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setDistributionStatusOfServiceList(distribStatusOfServiceInfoList);
	}
}