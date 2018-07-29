package org.openecomp.sdc.common.config;

import org.junit.Test;

public class EcompErrorInfoTest {

	private EcompErrorInfo createTestSubject() {
		return new EcompErrorInfo();
	}

	@Test
	public void testGetType() throws Exception {
		EcompErrorInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	@Test
	public void testSetType() throws Exception {
		EcompErrorInfo testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	@Test
	public void testGetCode() throws Exception {
		EcompErrorInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCode();
	}

	@Test
	public void testSetCode() throws Exception {
		EcompErrorInfo testSubject;
		String code = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCode(code);
	}

	@Test
	public void testGetSeverity() throws Exception {
		EcompErrorInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSeverity();
	}

	@Test
	public void testSetSeverity() throws Exception {
		EcompErrorInfo testSubject;
		String severity = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSeverity(severity);
	}

	@Test
	public void testGetDescription() throws Exception {
		EcompErrorInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDescription();
	}

	@Test
	public void testSetDescription() throws Exception {
		EcompErrorInfo testSubject;
		String description = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDescription(description);
	}

	@Test
	public void testGetAlarmSeverity() throws Exception {
		EcompErrorInfo testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAlarmSeverity();
	}

	@Test
	public void testSetAlarmSeverity() throws Exception {
		EcompErrorInfo testSubject;
		String alarmSeverity = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAlarmSeverity(alarmSeverity);
	}

}