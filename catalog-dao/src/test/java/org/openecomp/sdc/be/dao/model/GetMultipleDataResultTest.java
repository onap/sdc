package org.openecomp.sdc.be.dao.model;

import org.apache.tinkerpop.gremlin.structure.T;
import org.junit.Test;

public class GetMultipleDataResultTest {

	private GetMultipleDataResult createTestSubject() {
		return new GetMultipleDataResult<>();
	}

	@Test
	public void testCtor() throws Exception {
		new GetMultipleDataResult<>(new String [1], new Object[1]);
		new GetMultipleDataResult<>(new String [1], new String [1], 0L, 0L, 1, 1);
	}
	
	@Test
	public void testGetTypes() throws Exception {
		GetMultipleDataResult testSubject;
		String[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTypes();
	}

	@Test
	public void testGetData() throws Exception {
		GetMultipleDataResult testSubject;
		Object[] result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getData();
	}

	@Test
	public void testSetTypes() throws Exception {
		GetMultipleDataResult testSubject;
		String[] types = new String[] { "" };

		// default test
		testSubject = createTestSubject();
		testSubject.setTypes(types);
	}

	@Test
	public void testSetData() throws Exception {
		GetMultipleDataResult testSubject;
		T[] data = new T[] { null };

		// default test
		testSubject = createTestSubject();
		testSubject.setData(data);
	}

	@Test
	public void testSetQueryDuration() throws Exception {
		GetMultipleDataResult testSubject;
		long queryDuration = 0L;

		// default test
		testSubject = createTestSubject();
		testSubject.setQueryDuration(queryDuration);
	}

	@Test
	public void testSetTotalResults() throws Exception {
		GetMultipleDataResult testSubject;
		long totalResults = 0L;

		// default test
		testSubject = createTestSubject();
		testSubject.setTotalResults(totalResults);
	}

	@Test
	public void testSetFrom() throws Exception {
		GetMultipleDataResult testSubject;
		int from = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setFrom(from);
	}

	@Test
	public void testSetTo() throws Exception {
		GetMultipleDataResult testSubject;
		int to = 0;

		// default test
		testSubject = createTestSubject();
		testSubject.setTo(to);
	}

	@Test
	public void testGetQueryDuration() throws Exception {
		GetMultipleDataResult testSubject;
		long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getQueryDuration();
	}

	@Test
	public void testGetTotalResults() throws Exception {
		GetMultipleDataResult testSubject;
		long result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTotalResults();
	}

	@Test
	public void testGetFrom() throws Exception {
		GetMultipleDataResult testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getFrom();
	}

	@Test
	public void testGetTo() throws Exception {
		GetMultipleDataResult testSubject;
		int result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getTo();
	}
}