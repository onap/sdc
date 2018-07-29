package org.openecomp.sdc.be.resources.data;

import org.junit.Assert;
import org.junit.Test;
import org.openecomp.sdc.be.datatypes.enums.NodeTypeEnum;

import java.util.HashMap;
import java.util.Map;

public class TagDataTest {

	private TagData createTestSubject() {
		return new TagData();
	}

	@Test
	public void testCtor() throws Exception {
		new TagData(new HashMap<>());
		new TagData("mock");
		new TagData(NodeTypeEnum.Tag);
	}
	
	@Test
	public void testToGraphMap() throws Exception {
		TagData testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	@Test
	public void testGetName() throws Exception {
		TagData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getName();
	}

	@Test
	public void testSetName() throws Exception {
		TagData testSubject;
		String name = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setName(name);
	}

	@Test
	public void testToString() throws Exception {
		TagData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toString();
	}

	@Test
	public void testHashCode() throws Exception {
		TagData testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.hashCode();
	}

	@Test
	public void testEquals() throws Exception {
		TagData testSubject;
		Object obj = null;
		boolean result;

		// test 1
		testSubject = createTestSubject();
		obj = null;
		result = testSubject.equals(obj);
		Assert.assertEquals(false, result);
	}

	@Test
	public void testGetUniqueIdKey() throws Exception {
		TagData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueIdKey();
	}

	@Test
	public void testGetUniqueId() throws Exception {
		TagData testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}
}