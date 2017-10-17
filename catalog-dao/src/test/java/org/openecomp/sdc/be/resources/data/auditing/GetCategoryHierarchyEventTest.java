package org.openecomp.sdc.be.resources.data.auditing;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Generated;

import org.junit.Test;


public class GetCategoryHierarchyEventTest {

	private GetCategoryHierarchyEvent createTestSubject() {
		return new GetCategoryHierarchyEvent();
	}

	
	@Test
	public void testFillFields() throws Exception {
		GetCategoryHierarchyEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}

	
	@Test
	public void testGetTimebaseduuid() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		UUID result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimebaseduuid();
	}

	
	@Test
	public void testSetTimebaseduuid() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		UUID timebaseduuid = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimebaseduuid(timebaseduuid);
	}

	
	@Test
	public void testGetTimestamp1() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp1();
	}

	
	@Test
	public void testSetTimestamp1() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		Date timestamp1 = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp1(timestamp1);
	}

	
	@Test
	public void testGetRequestId() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	
	@Test
	public void testSetRequestId() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		String requestId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestId(requestId);
	}

	
	@Test
	public void testGetAction() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	
	@Test
	public void testSetAction() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	
	@Test
	public void testGetStatus() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	
	@Test
	public void testGetDesc() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	
	@Test
	public void testSetDesc() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	
	@Test
	public void testGetModifier() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModifier();
	}

	
	@Test
	public void testSetModifier() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		String modifier = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setModifier(modifier);
	}

	
	@Test
	public void testGetDetails() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDetails();
	}

	
	@Test
	public void testSetDetails() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		String details = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDetails(details);
	}

	
	@Test
	public void testToString() throws Exception {
		GetCategoryHierarchyEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}
}