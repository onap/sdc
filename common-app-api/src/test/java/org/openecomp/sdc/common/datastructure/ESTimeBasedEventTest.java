package org.openecomp.sdc.common.datastructure;

import java.util.Map;

import javax.annotation.Generated;

import org.junit.Test;


public class ESTimeBasedEventTest {

	private ESTimeBasedEvent createTestSubject() {
		return new ESTimeBasedEvent();
	}

	

	
	@Test
	public void testCalculateYearIndexSuffix() throws Exception {
		ESTimeBasedEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.calculateYearIndexSuffix();
	}

	
	@Test
	public void testCalculateMonthIndexSuffix() throws Exception {
		ESTimeBasedEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.calculateMonthIndexSuffix();
	}

	
	@Test
	public void testCalculateDayIndexSuffix() throws Exception {
		ESTimeBasedEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.calculateDayIndexSuffix();
	}

	
	@Test
	public void testCalculateHourIndexSuffix() throws Exception {
		ESTimeBasedEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.calculateHourIndexSuffix();
	}

	
	@Test
	public void testCalculateMinuteIndexSuffix() throws Exception {
		ESTimeBasedEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.calculateMinuteIndexSuffix();
	}

	


	
	@Test
	public void testGetTimestamp() throws Exception {
		ESTimeBasedEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp();
	}

	
	@Test
	public void testSetTimestamp() throws Exception {
		ESTimeBasedEvent testSubject;
		String timestamp = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp(timestamp);
	}

	
	@Test
	public void testGetFields() throws Exception {
		ESTimeBasedEvent testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFields();
	}

	
	@Test
	public void testSetFields() throws Exception {
		ESTimeBasedEvent testSubject;
		Map<String, Object> fields = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setFields(fields);
	}
}