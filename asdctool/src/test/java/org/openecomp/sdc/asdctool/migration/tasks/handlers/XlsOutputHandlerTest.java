package org.openecomp.sdc.asdctool.migration.tasks.handlers;

import org.junit.Test;

public class XlsOutputHandlerTest {

	private XlsOutputHandler createTestSubject() {
		return new XlsOutputHandler(new Object());
	}

	@Test
	public void testInitiate() throws Exception {
		XlsOutputHandler testSubject;
		Object[] title = new Object[] { null };

		// default test
		testSubject = createTestSubject();
		testSubject.initiate(title);
	}

	@Test
	public void testAddRecord() throws Exception {
		XlsOutputHandler testSubject;
		Object[] record = new Object[] { null };

		// default test
		testSubject = createTestSubject();
		testSubject.addRecord(record);
	}

	@Test
	public void testWriteOutput() throws Exception {
		XlsOutputHandler testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.writeOutput();
	}
}