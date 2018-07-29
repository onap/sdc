package org.openecomp.sdc.be.resources.data;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class GraphNodeLockTest {

	private GraphNodeLock createTestSubject() {
		return new GraphNodeLock();
	}

	@Test
	public void testCtor() throws Exception {
		new GraphNodeLock(new HashMap<>());
		new GraphNodeLock("mock");
	}
	
	@Test
	public void testGetUniqueId() throws Exception {
		GraphNodeLock testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getUniqueId();
	}

	@Test
	public void testToGraphMap() throws Exception {
		GraphNodeLock testSubject;
		Map<String, Object> result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.toGraphMap();
	}

	@Test
	public void testSetUniqueId() throws Exception {
		GraphNodeLock testSubject;
		String uniqueId = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setUniqueId(uniqueId);
	}

	@Test
	public void testGetTime() throws Exception {
		GraphNodeLock testSubject;
		Long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTime();
	}

	@Test
	public void testSetTime() throws Exception {
		GraphNodeLock testSubject;
		Long time = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setTime(time);
	}
}