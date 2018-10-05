package org.openecomp.sdc.be.datamodel;

import org.junit.Test;

public class NameIdPairWrapperTest {

	private NameIdPairWrapper createTestSubject() {
		return new NameIdPairWrapper();
	}

	@Test
	public void testInit() throws Exception {
		NameIdPairWrapper testSubject;
		NameIdPair nameIdPair = new NameIdPair("mock", "mock");

		// default test
		testSubject = createTestSubject();
		
		testSubject.init(nameIdPair);
	}

	@Test
	public void testInitWithObject() throws Exception {
		NameIdPairWrapper testSubject;
		NameIdPair nameIdPair = new NameIdPair("mock", "mock");

		// default test
		testSubject = new NameIdPairWrapper(nameIdPair);
	}

	@Test
	public void testGetId() throws Exception {
		NameIdPairWrapper testSubject;
		String result;

		// default test
		testSubject = createTestSubject();
		testSubject.setId("mock");
		result = testSubject.getId();
	}

	@Test
	public void testSetId() throws Exception {
		NameIdPairWrapper testSubject;
		String id = "";

		// default test
		testSubject = createTestSubject();
		testSubject.setId(id);
	}

	@Test
	public void testGetData() throws Exception {
		NameIdPairWrapper testSubject;
		NameIdPair result;

		// default test
		testSubject = createTestSubject();
		result = testSubject.getData();
	}

	@Test
	public void testSetData() throws Exception {
		NameIdPairWrapper testSubject;
		NameIdPair data = null;

		// default test
		testSubject = createTestSubject();
		testSubject.setData(data);
	}
	
	@Test
	public void testGetNameIdPair() throws Exception {
		NameIdPairWrapper testSubject;
		NameIdPair nameIdPair = new NameIdPair("mock", "mock");
		
		// default test
		testSubject = createTestSubject();
		testSubject.init(nameIdPair);
		testSubject.getNameIdPair();
	}
}