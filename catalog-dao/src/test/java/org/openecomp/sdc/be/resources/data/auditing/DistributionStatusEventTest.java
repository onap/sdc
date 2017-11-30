package org.openecomp.sdc.be.resources.data.auditing;

import java.util.Date;
import java.util.UUID;

import org.junit.Test;


public class DistributionStatusEventTest {

	private DistributionStatusEvent createTestSubject() {
		return new DistributionStatusEvent();
	}

	
	@Test
	public void testFillFields() throws Exception {
		DistributionStatusEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}

	
	@Test
	public void testGetDid() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDid();
	}

	
	@Test
	public void testSetDid() throws Exception {
		DistributionStatusEvent testSubject;
		String did = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDid(did);
	}

	
	@Test
	public void testGetConsumerId() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getConsumerId();
	}

	
	@Test
	public void testSetConsumerId() throws Exception {
		DistributionStatusEvent testSubject;
		String consumerId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setConsumerId(consumerId);
	}

	
	@Test
	public void testGetTopicName() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTopicName();
	}

	
	@Test
	public void testSetTopicName() throws Exception {
		DistributionStatusEvent testSubject;
		String topicName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTopicName(topicName);
	}

	
	@Test
	public void testGetResoureURL() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResoureURL();
	}

	
	@Test
	public void testSetResoureURL() throws Exception {
		DistributionStatusEvent testSubject;
		String resoureURL = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResoureURL(resoureURL);
	}

	
	@Test
	public void testGetRequestId() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	
	@Test
	public void testSetRequestId() throws Exception {
		DistributionStatusEvent testSubject;
		String requestId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestId(requestId);
	}

	
	@Test
	public void testGetServiceInstanceId() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	
	@Test
	public void testSetServiceInstanceId() throws Exception {
		DistributionStatusEvent testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	
	@Test
	public void testGetAction() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	
	@Test
	public void testSetAction() throws Exception {
		DistributionStatusEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	
	@Test
	public void testGetStatus() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		DistributionStatusEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	
	@Test
	public void testGetDesc() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	
	@Test
	public void testSetDesc() throws Exception {
		DistributionStatusEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	
	@Test
	public void testGetTimebaseduuid() throws Exception {
		DistributionStatusEvent testSubject;
		UUID result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimebaseduuid();
	}

	
	@Test
	public void testSetTimebaseduuid() throws Exception {
		DistributionStatusEvent testSubject;
		UUID timebaseduuid = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimebaseduuid(timebaseduuid);
	}

	
	@Test
	public void testGetTimestamp1() throws Exception {
		DistributionStatusEvent testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp1();
	}

	
	@Test
	public void testSetTimestamp1() throws Exception {
		DistributionStatusEvent testSubject;
		Date timestamp = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp1(timestamp);
	}

	
	@Test
	public void testGetStatusTime() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatusTime();
	}

	
	@Test
	public void testSetStatusTime() throws Exception {
		DistributionStatusEvent testSubject;
		String statusTime = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatusTime(statusTime);
	}

	
	@Test
	public void testToString() throws Exception {
		DistributionStatusEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}