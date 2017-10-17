package org.openecomp.sdc.be.resources.data;

import java.util.Date;

import javax.annotation.Generated;

import org.junit.Test;


public class ComponentCacheDataTest {

	private ComponentCacheData createTestSubject() {
		return new ComponentCacheData();
	}

	
	@Test
	public void testGetDataAsArray() throws Exception {
		ComponentCacheData testSubject;
		byte[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getDataAsArray();
	}

	
	@Test
	public void testSetDataAsArray() throws Exception {
		ComponentCacheData testSubject;
		byte[] data = new byte[] { ' ' };

		// test 1
		testSubject = createTestSubject();
		data = null;
		testSubject.setDataAsArray(data);

		// test 2
		testSubject = createTestSubject();
		data = new byte[] { ' ' };
		testSubject.setDataAsArray(data);
	}

	


	
	@Test
	public void testGetId() throws Exception {
		ComponentCacheData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getId();
	}

	
	@Test
	public void testSetId() throws Exception {
		ComponentCacheData testSubject;
		String id = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setId(id);
	}

	
	@Test
	public void testGetModificationTime() throws Exception {
		ComponentCacheData testSubject;
		Date result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getModificationTime();
	}

	
	@Test
	public void testSetModificationTime() throws Exception {
		ComponentCacheData testSubject;
		Date modificationTime = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setModificationTime(modificationTime);
	}

	
	@Test
	public void testGetType() throws Exception {
		ComponentCacheData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getType();
	}

	
	@Test
	public void testSetType() throws Exception {
		ComponentCacheData testSubject;
		String type = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setType(type);
	}

	
	@Test
	public void testGetIsDirty() throws Exception {
		ComponentCacheData testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsDirty();
	}

	
	@Test
	public void testSetIsDirty() throws Exception {
		ComponentCacheData testSubject;
		boolean isDirty = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIsDirty(isDirty);
	}

	
	@Test
	public void testGetIsZipped() throws Exception {
		ComponentCacheData testSubject;
		boolean result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getIsZipped();
	}

	
	@Test
	public void testSetIsZipped() throws Exception {
		ComponentCacheData testSubject;
		boolean isZipped = false;

		// default test
		testSubject = createTestSubject();
		testSubject.setIsZipped(isZipped);
	}
}