package org.openecomp.sdc.be.resources.data.auditing;

import java.util.Date;
import java.util.UUID;

import javax.annotation.Generated;

import org.junit.Test;


public class CategoryEventTest {

	private CategoryEvent createTestSubject() {
		return new CategoryEvent();
	}

	
	@Test
	public void testFillFields() throws Exception {
		CategoryEvent testSubject;

		// default test
		testSubject = createTestSubject();
		testSubject.fillFields();
	}

	
	@Test
	public void testGetTimebaseduuid() throws Exception {
		CategoryEvent testSubject;
		UUID result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimebaseduuid();
	}

	
	@Test
	public void testSetTimebaseduuid() throws Exception {
		CategoryEvent testSubject;
		UUID timebaseduuid = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimebaseduuid(timebaseduuid);
	}

	
	@Test
	public void testGetAction() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getAction();
	}

	
	@Test
	public void testSetAction() throws Exception {
		CategoryEvent testSubject;
		String action = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setAction(action);
	}

	
	@Test
	public void testGetStatus() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getStatus();
	}

	
	@Test
	public void testSetStatus() throws Exception {
		CategoryEvent testSubject;
		String status = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setStatus(status);
	}

	
	@Test
	public void testGetDesc() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDesc();
	}

	
	@Test
	public void testSetDesc() throws Exception {
		CategoryEvent testSubject;
		String desc = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setDesc(desc);
	}

	
	@Test
	public void testGetCategoryName() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getCategoryName();
	}

	
	@Test
	public void testSetCategoryName() throws Exception {
		CategoryEvent testSubject;
		String categoryName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setCategoryName(categoryName);
	}

	
	@Test
	public void testGetSubCategoryName() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getSubCategoryName();
	}

	
	@Test
	public void testSetSubCategoryName() throws Exception {
		CategoryEvent testSubject;
		String subCategoryName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setSubCategoryName(subCategoryName);
	}

	
	@Test
	public void testGetGroupingName() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getGroupingName();
	}

	
	@Test
	public void testSetGroupingName() throws Exception {
		CategoryEvent testSubject;
		String groupingName = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setGroupingName(groupingName);
	}

	
	@Test
	public void testGetTimestamp1() throws Exception {
		CategoryEvent testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTimestamp1();
	}

	
	@Test
	public void testSetTimestamp1() throws Exception {
		CategoryEvent testSubject;
		Date timestamp1 = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTimestamp1(timestamp1);
	}

	
	@Test
	public void testGetModifier() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModifier();
	}

	
	@Test
	public void testSetModifier() throws Exception {
		CategoryEvent testSubject;
		String modifier = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setModifier(modifier);
	}

	
	@Test
	public void testGetServiceInstanceId() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getServiceInstanceId();
	}

	
	@Test
	public void testSetServiceInstanceId() throws Exception {
		CategoryEvent testSubject;
		String serviceInstanceId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setServiceInstanceId(serviceInstanceId);
	}

	
	@Test
	public void testGetResourceType() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getResourceType();
	}

	
	@Test
	public void testSetResourceType() throws Exception {
		CategoryEvent testSubject;
		String resourceType = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setResourceType(resourceType);
	}

	
	@Test
	public void testGetRequestId() throws Exception {
		CategoryEvent testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getRequestId();
	}

	
	@Test
	public void testSetRequestId() throws Exception {
		CategoryEvent testSubject;
		String requestId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setRequestId(requestId);
	}
}